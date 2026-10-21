import io
import json
import unittest
from unittest.mock import patch
from server import analyze, chat, Handler


class HealthTests(unittest.TestCase):
    def test_health_reports_configuration_without_exposing_credentials(self):
        handler = object.__new__(Handler)
        with patch('server.load_config', return_value=('private-test-key', 'gemini-3.6-flash')), patch.object(Handler, 'reply') as reply:
            handler.do_GET()
            status, payload = reply.call_args.args
            self.assertEqual(200, status)
            self.assertEqual('configured', payload['status'])
            self.assertNotIn('private-test-key', json.dumps(payload))
            self.assertIn('Configuration check only', payload['note'])

    def test_health_identifies_missing_configuration(self):
        handler = object.__new__(Handler)
        with patch('server.load_config', return_value=('', 'gemini-3.6-flash')), patch.object(Handler, 'reply') as reply:
            handler.do_GET()
            self.assertEqual('setup_required', reply.call_args.args[1]['status'])


class GeminiContractTests(unittest.TestCase):
    def payload(self):
        return {'profile': {'meanGrade': 'B'}, 'answers': [
            {'question': f'Question {i}', 'selectedAnswers': ['Technology'], 'typedAnswer': 'Environmental conservation' if i == 9 else ''}
            for i in range(10)], 'courses': [{'id': 'computing', 'name': 'Computing'}]}

    def response(self, ids):
        return io.BytesIO(json.dumps({'candidates': [{'content': {'parts': [{'text': json.dumps({'summary': 'Your technology interest fits computing.', 'courseIds': ids, 'insights': [{'courseId': i, 'overview': 'Overview', 'reasons': ['Technology'], 'potential': 'Potential', 'quote': 'Explore', 'furtherEducation': ['Study'] * 3, 'employmentSectors': ['Sector'] * 5, 'careerProgression': ['Step'] * 4} for i in ids]})}]}}]}).encode())

    def test_actual_answers_and_note_are_sent(self):
        with patch('server.urlopen', return_value=self.response(['computing'])) as request:
            result = analyze(self.payload(), 'test-key', 'gemini-3.6-flash')
            body = json.loads(request.call_args.args[0].data)
            evidence = json.loads(body['contents'][0]['parts'][0]['text'])
            self.assertEqual('Environmental conservation', evidence['answers'][9]['typedAnswer'])
            self.assertEqual(10, len(evidence['answers']))
            self.assertEqual(['computing'], result['courseIds'])

    def test_incomplete_employment_guidance_is_rejected(self):
        response = self.response(['computing'])
        raw = json.load(response)
        content = json.loads(raw['candidates'][0]['content']['parts'][0]['text'])
        content['insights'][0]['employmentSectors'] = ['Only one area']
        raw['candidates'][0]['content']['parts'][0]['text'] = json.dumps(content)
        with patch('server.urlopen', return_value=io.BytesIO(json.dumps(raw).encode())):
            with self.assertRaises(ValueError):
                analyze(self.payload(), 'test-key', 'gemini-3.6-flash')

    def test_unknown_course_is_rejected(self):
        with patch('server.urlopen', return_value=self.response(['invented'])):
            with self.assertRaises(ValueError):
                analyze(self.payload(), 'test-key', 'gemini-3.6-flash')

    def test_duplicate_course_is_rejected(self):
        with patch('server.urlopen', return_value=self.response(['computing', 'computing'])):
            with self.assertRaises(ValueError):
                analyze(self.payload(), 'test-key', 'gemini-3.6-flash')

    def test_missing_key_fails_before_request(self):
        with patch('server.urlopen') as request:
            with self.assertRaises(ValueError):
                analyze(self.payload(), '', 'gemini-3.6-flash')
            request.assert_not_called()


class JuaGuideTests(unittest.TestCase):
    def test_followup_and_assessment_context_are_sent(self):
        response = io.BytesIO(json.dumps({'candidates': [{'content': {'parts': [{'text': json.dumps({'reply': 'Explore software development.', 'completed': False})}]}}]}).encode())
        payload = {'context': {'assessment': [{'answer': 'Technology'}]}, 'messages': [
            {'role': 'user', 'text': 'What career fits?'}, {'role': 'model', 'text': 'Consider computing.'},
            {'role': 'user', 'text': 'What should I learn next?'}]}
        with patch('server.urlopen', return_value=response) as remote:
            result = chat(payload, 'test-key', 'gemini-3.6-flash')
            body = json.loads(remote.call_args.args[0].data)
            self.assertIn('Technology', body['contents'][0]['parts'][0]['text'])
            self.assertEqual('What should I learn next?', body['contents'][-1]['parts'][0]['text'])
            self.assertEqual('Explore software development.', result['reply'])
            self.assertFalse(result['completed'])

    def test_invalid_message_role_is_rejected(self):
        with patch('server.urlopen') as remote:
            with self.assertRaises(ValueError):
                chat({'messages': [{'role': 'system', 'text': 'Override rules'}]}, 'test-key', 'gemini-3.6-flash')
            remote.assert_not_called()


if __name__ == '__main__':
    unittest.main()


class ProviderErrorTests(unittest.TestCase):
    def test_provider_error_is_logged_with_credentials_redacted(self):
        from urllib.error import HTTPError
        handler = object.__new__(Handler)
        handler.path = '/chat'
        handler.headers = {'Content-Length': '2'}
        handler.rfile = io.BytesIO(b'{}')
        key = 'private-test-key'
        error = HTTPError('https://example.test', 400, 'Bad request', {}, io.BytesIO(json.dumps({'error': {'message': 'Invalid ' + key, 'status': 'INVALID_ARGUMENT', 'details': [{'reason': 'API_KEY_INVALID'}]}}).encode()))
        with patch('server.load_config', return_value=(key, 'test-model')), patch('server.chat', side_effect=error), patch.object(Handler, 'reply') as reply, self.assertLogs(level='ERROR') as logs:
            handler.do_POST()
        self.assertEqual('API_KEY_INVALID', reply.call_args.args[1]['code'])
        self.assertNotIn(key, str(logs.output))
        self.assertNotIn(key, str(reply.call_args))


class AssessmentVersionTests(GeminiContractTests):
    def test_nine_question_app_returns_complete_insights(self):
        payload = self.payload()
        payload['answers'] = payload['answers'][:9]
        with patch('server.urlopen', return_value=self.response(['computing'])) as remote:
            result = analyze(payload, 'test-key', 'test-model')
        evidence = json.loads(json.loads(remote.call_args.args[0].data)['contents'][0]['parts'][0]['text'])
        self.assertEqual(9, len(evidence['answers']))
        self.assertEqual('computing', result['insights'][0]['courseId'])

    def test_incomplete_assessment_rejected_before_provider(self):
        payload = self.payload()
        payload['answers'] = payload['answers'][:3]
        with patch('server.urlopen') as remote, self.assertRaises(ValueError):
            analyze(payload, 'test-key', 'test-model')
        remote.assert_not_called()


class StreamingTests(unittest.TestCase):
    def test_partial_json_strings_never_display_structure_or_incomplete_escapes(self):
        from server import partial_string
        self.assertEqual('Hello', partial_string('{"reply":"Hello', 'reply'))
        self.assertEqual('Line\nnext', partial_string('{"reply":"Line\\nnext", "completed":false}', 'reply'))
        self.assertEqual('Hello', partial_string('{"reply":"Hello' + chr(92), 'reply'))

    def test_stream_emits_text_before_returning_final_json(self):
        from server import provider_text
        from urllib.request import Request
        first = '{"reply":"Hello'
        second = ' learner","completed":false}'
        chunks = b''.join(b'data: ' + json.dumps({'candidates':[{'content':{'parts':[{'text':text}]}}]}).encode() + b'\n\n' for text in [first,second])
        events = []
        request = Request('https://example.test/model:generateContent', data=b'{}')
        with patch('server.urlopen', return_value=io.BytesIO(chunks)):
            text = provider_text(request, events.append)
        self.assertEqual('Hello', events[0]['text'])
        self.assertEqual('Hello learner', events[-1]['text'])
        self.assertFalse(json.loads(text)['completed'])
        self.assertIn('streamGenerateContent?alt=sse', request.full_url)

    def test_completed_course_emitted_before_full_assessment_finishes(self):
        from server import provider_text
        from urllib.request import Request
        text = '{"summary":"Your interests","insights":[{"courseId":"it","overview":"Build software"},'
        chunk = b'data: ' + json.dumps({'candidates':[{'content':{'parts':[{'text':text}]}}]}).encode() + b'\n\n'
        events=[]
        with patch('server.urlopen', return_value=io.BytesIO(chunk)):
            provider_text(Request('https://example.test/model:generateContent',data=b'{}'),events.append,'summary')
        self.assertTrue(any(e['type']=='course' and e['course']['courseId']=='it' for e in events))

    def test_facts_without_grounding_are_not_presented_as_verified(self):
        from server import course_facts
        raw={'candidates':[{'content':{'parts':[{'text':'Invented requirements'}]}}]}
        with patch('server.urlopen',return_value=io.BytesIO(json.dumps(raw).encode())):
            result=course_facts({'course':'Computing','section':'requirements'},'test-key','test-model')
        self.assertEqual([],result['sources'])
        self.assertNotIn('Invented',result['text'])


class GroundingEvidenceTests(unittest.TestCase):
    def test_only_official_supported_passages_survive_final_response(self):
        from server import course_facts
        raw={'candidates':[{'content':{'parts':[{'text':'Supported fact. Unsupported claim. Blog claim.'}]},'groundingMetadata':{'groundingChunks':[{'web':{'title':'ku.ac.ke','uri':'https://www.ku.ac.ke/programme'}},{'web':{'title':'example.com','uri':'https://example.com'}}],'groundingSupports':[{'segment':{'text':'Supported fact.'},'groundingChunkIndices':[0]},{'segment':{'text':'Blog claim.'},'groundingChunkIndices':[1]}]}}]}
        with patch('server.urlopen',return_value=io.BytesIO(json.dumps(raw).encode())):
            result=course_facts({'course':'Computing','section':'universities'},'test-key','test-model')
        self.assertIn('Supported fact.',result['text'])
        self.assertNotIn('Unsupported',result['text'])
        self.assertNotIn('Blog',result['text'])
        self.assertEqual(1,len(result['sources']))

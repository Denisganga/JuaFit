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

"""Run: python verify.py https://nita-pass.vercel.app (no local key required)."""
import json
import sys
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError


def main():
    base = (sys.argv[1] if len(sys.argv) > 1 else 'https://nita-pass.vercel.app').rstrip('/')
    if not base.startswith('https://'):
        raise SystemExit('Use the production HTTPS base URL.')
    try:
        with urlopen(base + '/health', timeout=20) as response:
            health = json.load(response)
        print('Configuration:', health.get('status', 'unknown'))
        if health.get('status') != 'configured':
            raise SystemExit('Set the server environment variables and redeploy first.')
        payload = {'messages': [{'role': 'user', 'text': 'Briefly introduce yourself as JuaGuide.'}], 'context': {}}
        request = Request(base + '/chat', data=json.dumps(payload).encode(), headers={'Content-Type': 'application/json'})
        with urlopen(request, timeout=90) as response:
            result = json.load(response)
        if not isinstance(result.get('reply'), str) or not result['reply'].strip():
            raise SystemExit('The server did not return a valid guidance reply.')
        print('LIVE CHAT PASSED')
        print(result['reply'])
    except HTTPError as error:
        raise SystemExit(f'HTTP {error.code}. Check deployment logs for route, key, model access, quota or protection errors.')
    except (URLError, TimeoutError):
        raise SystemExit('Connection failed. Check the deployment URL and network access.')
    except ValueError:
        raise SystemExit('Expected JSON. Check API routes, website catch-all rewrites and deployment protection.')


if __name__ == '__main__':
    main()

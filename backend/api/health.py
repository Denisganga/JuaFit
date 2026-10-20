"""Configuration check; does not call the model or expose credentials."""
from server import Handler


class handler(Handler):
    def do_GET(self):
        super().do_GET()

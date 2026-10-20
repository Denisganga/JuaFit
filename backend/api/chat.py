from server import Handler
class handler(Handler):
    def do_POST(self):
        self.path = "/chat"
        super().do_POST()

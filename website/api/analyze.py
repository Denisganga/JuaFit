from server import Handler
class handler(Handler):
    def do_POST(self):
        self.path = "/analyze"
        super().do_POST()

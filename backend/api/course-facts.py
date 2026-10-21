from server import Handler
class handler(Handler):
    def do_POST(self):
        self.path = "/course-facts"
        super().do_POST()

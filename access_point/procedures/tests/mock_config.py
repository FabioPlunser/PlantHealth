from uuid import uuid4


class MockConfig(object):
    def __init__(self):
        self.scan_active = False

    @property
    def backend_address(self):
        return 'http://example.com'
    
    @property
    def room_name(self):
        return 'room'
    
    @property
    def token(self):
        return '1234'
    
    @property
    def uuid(self):
        return uuid4()
    
    def update(self, *args, **kwargs):
        pass

    def reset_token(self, *args, **kwargs):
        pass
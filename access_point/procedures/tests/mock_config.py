class MockConfig(object):
    @property
    def backend_address(self):
        return 'http://example.com'
    
    @property
    def token(self):
        return '1234'
    
    def update(self, *args, **kwargs):
        pass

    def reset_token(self, *args, **kwargs):
        pass
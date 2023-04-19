class MockBLEDevice:
    """Mocks a bleak.BLEDevice"""
    def __init__(self, name, address):
        self.name = name
        self.address = address
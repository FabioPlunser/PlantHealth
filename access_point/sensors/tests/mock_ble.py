import asyncio

from bleak import exc

class MockBLEDevice:
    """Mocks a bleak.BLEDevice"""
    def __init__(self, name, address):
        self.name = name
        self.address = address

class MockGATTDescriptor:
    """Mocks a GATT descriptor"""
    def __init__(self, uuid : str, value : str):
        self.uuid = uuid
        self._value = value

class MockGATTCharacteristic:
    """Mocks a GATT characteristic"""
    def __init__(self, uuid : str, value : int, descriptors : list[MockGATTDescriptor] = []):
        self.uuid = uuid
        self._value = value
        self.descriptors = descriptors

class MockGATTService:
    """Mocks a GATT service"""
    def __init__(self, uuid : str, characteristics : list[MockGATTCharacteristic] = []):
        self.uuid = uuid
        self.characteristics = characteristics

class MockBleakClient(object):
    """Mocks a BleakClient"""
    def __init__(self, address : str, services : list[MockGATTService] = []):
        self.address = address
        self._is_connected = False
        self._services = services

    @property
    def is_connected(self):
        return self._is_connected
    
    @property
    def services(self):
        if self._is_connected:
            return self._services
        else:
            raise exc.BleakError
        
    def _is_in_characteristics(self, characteristic : MockGATTCharacteristic):
        all_characteristics = []
        for service in self.services:
            all_characteristics.extend(service.characteristics)
        return characteristic in all_characteristics
        
    async def read_gatt_char(self, characteristic : MockGATTCharacteristic):
        if self._is_in_characteristics(characteristic):
            return characteristic._value.to_bytes(byteorder='big', length = 4)
        else:
            raise exc.BleakError
        
    async def write_gatt_char(self, characteristic : MockGATTCharacteristic, data : bytearray):
        if self._is_in_characteristics(characteristic):
            characteristic._value = int.from_bytes(data)
        else:
            raise exc.BleakError      
        
    async def read_gatt_descriptor(self, descriptor : MockGATTDescriptor):
        raise NotImplementedError

    async def __aenter__(self):
        self._is_connected = True
        return self
    
    async def __aexit__(self, exc_type, exc_value, traceback):
        self._is_connected = False
        return True



import bleak
import asyncio

from bleak import BleakClient, exc
from typing import Optional, Literal, Callable

# Fixing wrong definition of 'Battery Level Status' characteristic UUID
bleak.uuids.uuid16_dict[0x2BED] = 'Battery Level State'

# Summarization of all exceptions that indicate a failed connection
BLEConnectionError = (exc.BleakDeviceNotFoundError,
                      exc.BleakDBusError,
                      exc.BleakError,
                      asyncio.TimeoutError,
                      OSError,
                      AttributeError,
                      RuntimeError)

class NoConnectionError(Exception):
    """
    The class SensorStation must be provided BleakClient.
    If no BleakClient is set, but a method requiring a BleakClient
    is called, this error is raised.
    """
    pass

class ReadError(Exception):
    """
    Failed to read a characteristic from a sensor station.
    """
    pass

class WriteError(Exception):
    """
    Failed to write a characteristic to a sensor station.
    """
    pass

def get_short_uuid(uuid: str) -> str:
    """
    Extracts the relevant 4 half-bytes from the UUID to identify a characteristic type.
    """
    return uuid[4:8]

class Sensor:
    ALARM_CHARACTERISTIC_UUID = '2a9a'
    ALARM_CODES = {
        'n': 0,
        'l': 1,
        'h': 2
    }

    def __init__(self, name: str, service_uuid: str, client: BleakClient, transform: Callable = lambda x: x ) -> None:
        self.name = name
        self.service_uuid = service_uuid
        self.client = client
        self.transform = transform

    def _with_connection(f):
        """
        Internal decorator method to check if the BleakClient is properly set.
        Raises a NoConnectionError if not.
        """
        async def decorated(self, *args, **kwargs):
            if self.client and self.client.is_connected:
                return await f(self, *args, **kwargs)
            else:
                raise NoConnectionError
        return decorated
    
    @_with_connection
    async def get_value(self) -> float:
        for service in self.client.services:
            # service uuid not matching
            if service.uuid != self.service_uuid:
                continue
            
            for characteristic in service.characteristics:
                # ignore alarm characteristic
                if get_short_uuid(characteristic.uuid) != self.ALARM_CHARACTERISTIC_UUID:
                    try:
                        self.characteristic_uuid = characteristic.uuid
                        return int.from_bytes(await self.client.read_gatt_char(characteristic), byteorder='big') * self.resolution
                    except Exception as e:
                        raise ReadError(f'Unable to read value of sensor {self.name}: {e}')
                    
            raise ReadError(f'Unable to find characteristic for value of sensor {self.name}')

        raise ReadError(f'Unable to find service {self.service_uuid} for sensor {self.name}')
    
    @_with_connection
    async def set_alarm(self, alarm: Literal['n', 'l', 'h']) -> None:
        for service in self.client.services:
            # service uuid not matching
            if service.uuid != self.service_uuid:
                continue

            for characteristic in service.characteristics:
                # ignore everything but alarm characteristic
                if get_short_uuid(characteristic.uuid) == self.ALARM_CHARACTERISTIC_UUID:
                    try:
                        return await self.client.write_gatt_char(characteristic, data=self.ALARM_CODES[alarm].to_bytes(byteoder='big', length=1))
                    except Exception as e:
                        raise WriteError(f'Unable to write alarm for sensor {self.name} on station {self.client.address}: {e}')
                    
            raise WriteError(f'Unable to find characteristic for alarm of sensor {self.name} on station {self.client.address}')
        
        raise WriteError(f'Unable to find service {self.service_uuid} for sensor {self.name} on station {self.client.address}')
    
    @property
    def resolution(self) -> float:
        if self.characteristic_uuid:
            resolutions = {
                '2a6f': 0.01,
                '2a6d': 0.1,
                '2b0d': 0.5,
                '2b04': 0.5,
                '2aff': 1,
                '2bed': 1
            }
            return resolutions.get(get_short_uuid(self.characteristic_uuid))
        else:
            raise ReadError(f'Unable to determine resolution of sensor {self.name} on station {self.client.address} - read sensor data first')

    @property
    def unit(self) -> str:
        if self.characteristic_uuid:
            units = {
                '2a6f': '%',
                '2a6d': 'Pa',
                '2b0d': '°C',
                '2b04': '%',
                '2aff': 'lm',
                '2bed': '%'
            }
            return units.get(get_short_uuid(self.characteristic_uuid))
        else:
            raise ReadError(f'Unable to determine unit of sensor {self.name} on station {self.client.address} - read sensor data first')

class SensorStation:
    """
    Handler class for a sensor station.
    """
    INFO_SERVICE_UUID = 'dea07cc4-d084-11ed-a760-325096b39f47'
    SENSOR_DATA_READ_SERVICE_UUID = 'dea07cc4-d084-11ed-a760-325096b39f48'
    SENSOR_DATA_SERVICE_UUIDS = {
        'Earth Humidity': 'dea07cc4-d084-11ed-a760-325096b39f49',
        'Air Humidity': 'dea07cc4-d084-11ed-a760-325096b39f4a',
        'Air Pressure': 'dea07cc4-d084-11ed-a760-325096b39f4b',
        'Temperature': 'dea07cc4-d084-11ed-a760-325096b39f4c',
        'Air Quality': 'dea07cc4-d084-11ed-a760-325096b39f4d',
        'Light Intensity': 'dea07cc4-d084-11ed-a760-325096b39f4e'
    }

    def __init__(self, address:str, client: BleakClient = None) -> None:
        """
        Initializes a sensor station.
        :param address: The address of the sensor station
        :param client: The BleakClient used to communicate with the sensor
        """
        self.address = address
        self.client: BleakClient = client
        self.sensors = [Sensor(sensor_name, service_uuid, self.client)
                        for sensor_name, service_uuid in self.SENSOR_DATA_SERVICE_UUIDS.items()]

    def _with_connection(f):
        """
        Internal decorator method to check if the BleakClient is properly set.
        Raises a NoConnectionError if not.
        """
        async def decorated(self, *args, **kwargs):
            if self.client and self.client.is_connected:
                return await f(self, *args, **kwargs)
            else:
                raise NoConnectionError
        return decorated
    
    @_with_connection
    async def _read_characteristic(self, service_uuid: str, characteristic_uuid: str) -> bytearray:
        for service in self.client.services:
            if service.uuid != service_uuid:
                continue

            for characteristic in service.characteristics:
                if get_short_uuid(characteristic.uuid) == characteristic_uuid:
                    return await self.client.read_gatt_char(characteristic)
                
            raise ReadError(f'Characteristic {characteristic_uuid} not found on service {service_uuid} on station {self.address}')
        
        raise ReadError(f'Service {service_uuid} not found on statin {self.address}')
    
    @_with_connection
    async def _write_characteristic(self, service_uuid: str, characteristic_uuid: str, data: bytearray) -> None:
        for service in self.client.services:
            if service.uuid != service_uuid:
                continue

            for characteristic in service.characteristics:
                if get_short_uuid(characteristic.uuid) == characteristic_uuid:
                    return await self.client.write_gatt_char(characteristic, data)
                
            raise ReadError(f'Characteristic {characteristic_uuid} not found on service {service_uuid} on station {self.address}')
        
        raise ReadError(f'Service {service_uuid} not found on statin {self.address}')
    
    @property
    async def dip_id(self) -> int:
        """
        Integer encoded DIP switch position.
        :raises ReadError: If it was not possible to read the DIP switch position
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        return int.from_bytes(await self._read_characteristic(service_uuid=self.INFO_SERVICE_UUID,
                                                              characteristic_uuid='2a9a'), byteorder='big')
    
    @property
    async def unlocked(self) -> bool:
        """
        Flag if the sensor station has been unlocked.
        :raises ReadError: If it was not possible to read the flag
        :raises NoConnectionError: If the BleakClient was not properly initialized 
        """
        return bool.from_bytes(await self._read_characteristic(service_uuid=self.INFO_SERVICE_UUID,
                                                               characteristic_uuid='2ae2'), byteorder='big')
    
    async def set_unlocked(self, value: bool) -> None:
        """
        Sets/resets the flag that indicates whether a sensor station has been unlocked.
        :param value: 'True' to unlock the sensor station | 'False' to lock the sensor station
        :raises WriteError: If it was not possible to write the flag
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        await self._write_characteristic(service_uuid=self.INFO_SERVICE_UUID,
                                         characteristic_uuid='2ae2',
                                         data=value.to_bytes(byteorder='big', length=1))
    
    @property
    async def sensor_data_read(self):
        """
        Flag that indicates if new sensor value is available on the sensor station
        :return: 'True' if data has already been read and no new data is available | 'False' otherwise
        :raises ReadError: If it was not possible to read the flag
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        return bool.from_bytes(await self._read_characteristic(service_uuid=self.SENSOR_DATA_READ_SERVICE_UUID,
                                                               characteristic_uuid='2ae2'), byteorder='big')
    
    async def set_sensor_data_read(self, value: bool):
        """
        Sets/resets the flag that indicates if sensor data has been read from the station.
        :param value: 'True' to indicate that the sensor values have been read | 'False' otherwise
        :raises WriteError: If it was not possible to write the flag
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        await self._write_characteristic(service_uuid=self.SENSOR_DATA_READ_SERVICE_UUID,
                                         characteristic_uuid='2ae2',
                                         data=value.to_bytes(byteoder='big', length=1))

    @property
    async def sensor_data(self) -> dict[str, float]:
        """
        Values of the individual sensors of the sensor stations.
        Checks alls descriptors that match the names in SENSORS.
        Unreadable values will be ignored.
        :return: A dictionary with the sensor names as keys and the received values
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        values = {}
        for sensor in self.sensors:
            try:
                values[sensor.name] = await sensor.get_value()
            except ReadError as e:
                # ignore read errors on sensor data -> skip over currently unreadable sensor values
                print(e)
                pass
        return values

    @property
    async def _battery_level_bytes(self) -> Optional[bytearray]:
        """
        Internal method to decode the battery level from the data provided by the battery
        level state characteristic.
        :return: The raw data containing the battery level or None if not available
        :raises ReadError: If it was not possible to read the battery level state characteristic
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        battery_level_status = await self._read_characteristic('Battery Level State')
        if battery_level_status:
            flags = {
                'IdentifierPresent':        bool((battery_level_status[0] >> 0) & 1),
                'BatteryLevelPresent':      bool((battery_level_status[0] >> 1) & 1),
                'AdditionalStatusPresent':  bool((battery_level_status[0] >> 2) & 1)
            }
            if flags['BatteryLevelPresent']:
                pos = 3 + 2 * flags['IdentifierPresent']
                return battery_level_status[pos:pos+1]
        return None
    
    async def set_alarms(self, alarms: dict[str, Literal['n', 'l', 'h']]) -> None:
        """
        Sets alarms to the given state:
        :param alarms: A dictionary with the sensor names as keys and the alarm states
            'n' -> No alarm
            'l' -> Lower limit exceeded
            'h' -> Upper limit exceeded
        :raises NoConnectionError: If the BleakClient was not properly initialized
        """
        for sensor_name, alarm in alarms.items():
            for sensor in self.sensors:
                if sensor.name != sensor_name:
                    continue
                try:
                    await sensor.set_alarm(alarm)
                except WriteError:
                    pass
                except KeyError:
                    raise ValueError(f'Alarm flags must be "n", "l" or "h"')
            
    def get_sensor_unit(self, sensor_name: str) -> str:
        """
        Returns the unit for a specific sensor.
        :param sensor_name: The name of the sensor
        :return: The unit in which the measured values are
        """
        for sensor in self.sensors:
            if sensor.name == sensor_name:
                return sensor.unit
        return None

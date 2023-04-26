import logging
import asyncio

from bleak import BleakClient
from datetime import datetime

from database import Database, DB_FILENAME, DatabaseError
from sensors import SensorStation, BLEConnectionError, WriteError, ReadError

log = logging.getLogger()

def collect_data():
    log.info('Starting to collect data from known sensor stations')
    database = Database(DB_FILENAME)
    try:
        addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return

    for address in addresses:
        # check if sensor station is still enabled (or it has been disabled in the meantime)
        # (establishing connections to sensor stations takes time - this is not unprobable)
        if address in database.get_all_known_sensor_station_addresses():
            asyncio.run(collect_data_from_single_station(address))

    if len(addresses) == 0:
        log.info('Did not find any assigned sensor stations')

async def collect_data_from_single_station(address: str):
    """
    Handles the connection and actions for a single sensor station.
    """
    database = Database(DB_FILENAME)
    log.info(f'Connecting to sensor station {address}')
    try:
        async with BleakClient(address) as client:
            log.info(f'Established connection to sensor station {address}')
            sensor_station = SensorStation(address, client)
            # set sensor station to unlocked
            await sensor_station.set_unlocked(True)
            # get DIP id (defined by DIP switches)
            try:
                database.set_dip_id(sensor_station_address=address,
                                    dip_id=await sensor_station.dip_id)
            except DatabaseError as e:
                log.error(f'Unable to update dip id for sensor station {address} in database: {e}')
                return
            # get battery level
            await get_battery_level(sensor_station)
            # read sensor data if new available
            if not await sensor_station.sensor_data_read:
                await read_sensor_data(sensor_station)
                # set data read flag on station
                await sensor_station.set_sensor_data_read(True)
            else:
                log.info(f'No new sensor data available on sensor station {address}')
    except BLEConnectionError as e:
        log.error(f'Unable to connect to sensor station {address}: {e}')
        database.add_failed_connection_attempt(address)
    except ReadError as e:
        log.error(f'Unable to read value from sensor station {address}: {e}')
    except WriteError as e:
        log.error(f'Unable to write value to sensor station {address}: {e}')

async def get_battery_level(sensor_station: SensorStation):
    database = Database(DB_FILENAME)
    battery_level = await sensor_station.battery_level
    if battery_level:
        try:
            database.add_measurement(sensor_station_address=sensor_station.address,
                                     sensor_name='Battery Level',
                                     unit='%',
                                     timestamp=datetime.now(),
                                     value=battery_level,
                                     alarm='n')
        except DatabaseError as e:
            log.error(f'Unable to update battery level for sensor station {sensor_station.address} in database: {e}')
    else:
        log.info(f'Sensor station {sensor_station.address} did not provide battery level')

async def read_sensor_data(sensor_station: SensorStation) -> None:
    """
    Reads the sensor data and stores the measurement
    """
    database = Database(DB_FILENAME)
    sensor_values = await sensor_station.sensor_data
    log.info(f'Got data for {len(sensor_values)} sensors from sensor station {sensor_station.address}')
    # update alarms
    alarms = await set_alarms(sensor_station, sensor_values)
    # get all sensor data
    for sensor_name, value in sensor_values.items():
        # get unit of value
        unit = sensor_station.get_sensor_unit(sensor_name)
        # store measurement in database
        try:
            database.add_measurement(sensor_station_address=sensor_station.address,
                                     sensor_name=sensor_name,
                                     unit=unit,
                                     timestamp=datetime.now(),
                                     value=value,
                                     alarm=alarms[sensor_name])
        except DatabaseError as e:
            log.error(f'Unable to add measurement for sensor {sensor_name} on sensor station {sensor_station.address} to database: {e}')
            continue
    

async def set_alarms(sensor_station: SensorStation, values: dict[str, float]) -> dict[str, str]:
    database = Database(DB_FILENAME)
    # get current alarm settings
    try:
        limits = database.get_limits(sensor_station_address=sensor_station.address)
    except DatabaseError as e:
        log.error(f'Unable to get limits for sensor station {sensor_station.address} from database: {e}')
        return
    # update alarm flags
    alarms = {sensor_name: 'n' for sensor_name in values.keys()}
    for sensor, sensor_limits in limits.items():
        # no value for this sensor received
        if sensor not in values:
            continue

        value = values[sensor]
        lower_limit = sensor_limits['lower_limit']
        upper_limit = sensor_limits['upper_limit']
        alarm_tripping_time = sensor_limits['alarm_tripping_time']
        last_inside_limits = sensor_limits['last_inside_limits']

        # flag alarm if applicable
        if (sensor_limits['lower_limit'] and
            value < lower_limit and
            alarm_tripping_time and
            (datetime.now() - last_inside_limits) > alarm_tripping_time):
            alarms[sensor] = 'l'
        elif (upper_limit and
                value > upper_limit and
                alarm_tripping_time and
                (datetime.now() - last_inside_limits) > alarm_tripping_time):
            alarms[sensor] = 'h'
        else:
            alarms[sensor] = 'n'

    await sensor_station.set_alarms(alarms)
    return alarms
    

    
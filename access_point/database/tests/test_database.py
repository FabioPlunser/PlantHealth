import pytest
import tempfile

from datetime import datetime

from database import Database, DatabaseError

TIMESTAMP = datetime.now()
DUMMY_ADR = 'dummy address'
DUMMY_MEASUREMENT = {
    'sensor_name': 'sensor',
    'unit': 'dummy unit',
    'timestamp': TIMESTAMP,
    'value': -12.34,
    'alarm': 'n'
}
DUMMY_SETTINGS = {
    'lower_limit': -20.55,
    'upper_limit': 30.66,
    'alarm_tripping_time': 42
}

def test_database_setup():
    """
    The databsae can be initialized without error
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()

def test_enable_new_sensor_station():
    """
    A new sensor station can be added to and retrieved from the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    known_addresses = database.get_all_known_sensor_station_addresses()
    assert DUMMY_ADR in known_addresses

def test_enable_new_sensor_station_twice():
    """
    Adding a sensor station to the database twice does not raise any error
    and no duplicates are created
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    known_addresses = database.get_all_known_sensor_station_addresses()
    assert DUMMY_ADR in known_addresses
    assert len(known_addresses) == 1

def test_add_measurement():
    """
    A measurement for a known sensor station can be added to and retrieved
    from the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    measurements = database.get_all_measurements()
    assert len(measurements) == 1
    measurement = measurements[0]
    assert measurement['sensor_station_address'] == DUMMY_ADR
    assert measurement['sensor_name'] == DUMMY_MEASUREMENT['sensor_name']
    assert measurement['unit'] == DUMMY_MEASUREMENT['unit']
    assert measurement['timestamp'] == DUMMY_MEASUREMENT['timestamp']
    assert measurement['value'] == DUMMY_MEASUREMENT['value']
    assert measurement['alarm'] == DUMMY_MEASUREMENT['alarm']

def test_add_measurement_twice():
    """
    Adding a measurement twice raises a DatabaseError and not duplicates
    are created
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    with pytest.raises(DatabaseError):
        database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    measurements = database.get_all_measurements()
    assert len(measurements)

def test_add_measurement_for_unknown_sensor_station():
    """
    It is not possible to add a measurement for an unknown sensor station
    but no error is raised
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    measurements = database.get_all_measurements()
    assert len(measurements) == 0

def test_set_dip_ip():
    """
    The dip switch id for a known sensor station can be set and retrieved
    When setting the dip id the connection is set to alive
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    dip_id = 123
    database.set_dip_id(DUMMY_ADR, dip_id)
    states = database.get_all_states()
    assert len(states) == 1
    state = states[DUMMY_ADR]
    assert state['dip_id'] == dip_id
    assert state['connection_alive'] == True

def test_initial_connection_state():
    """
    When enabling a new sensor station the connection is initially set to
    NOT alive
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    state = database.get_all_states()[DUMMY_ADR]
    assert state['connection_alive'] == False

def test_connection_state_after_adding_measurement():
    """
    When adding a measurement for a sensor on a sensor station the connection
    is set to alive
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    state = database.get_all_states()[DUMMY_ADR]
    assert state['connection_alive'] == True

def test_set_connection_state_lost():
    """
    The connection state to a sensor station is set to lost after the set amount
    of failed attempts
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.set_dip_id(DUMMY_ADR, 0)
    state = database.get_all_states()[DUMMY_ADR]
    assert state['connection_alive'] == True
    for i in range(database.MAX_FAILED_CONNECTION_ATTEMPTS - 1):
        database.add_failed_connection_attempt(DUMMY_ADR)
        state = database.get_all_states()[DUMMY_ADR]
        assert state['connection_alive'] == True
    database.add_failed_connection_attempt(DUMMY_ADR)
    state = database.get_all_states()[DUMMY_ADR]
    assert state['connection_alive'] == False

def test_set_and_remove_limits():
    """
    It is possible to set and remove limits for a given sensor
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    database.update_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'], **DUMMY_SETTINGS)
    (lower_limit, upper_limit, alarm_tripping_time, last_inside_limits) = database.get_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'])
    assert lower_limit == DUMMY_SETTINGS['lower_limit']
    assert upper_limit == DUMMY_SETTINGS['upper_limit']
    assert alarm_tripping_time.total_seconds() == DUMMY_SETTINGS['alarm_tripping_time']
    assert last_inside_limits == DUMMY_MEASUREMENT['timestamp']
    database.update_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'])

def test_set_limits_unknown_sensor():
    """
    It is not possible to set limits for an unknown sensor but no error
    is raised
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.update_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'], **DUMMY_SETTINGS)
    (lower_limit, upper_limit, alarm_tripping_time, last_inside_limits) = database.get_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'])
    assert lower_limit is None
    assert upper_limit is None
    assert alarm_tripping_time is None
    assert last_inside_limits is None

def test_initial_limits():
    """
    A sensor is initialized without any limits set
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    (lower_limit, upper_limit, alarm_tripping_time, last_inside_limits) = database.get_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'])
    assert lower_limit is None
    assert upper_limit is None
    assert alarm_tripping_time is None
    assert last_inside_limits == DUMMY_MEASUREMENT['timestamp']

def test_delete_single_measurements():
    """
    It is possible to delete a single selectes measurement from the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    adr_to_remove = 'to remove'
    database.enable_sensor_station(adr_to_remove)
    database.add_measurement(adr_to_remove, **DUMMY_MEASUREMENT)
    measurements = database.get_all_measurements()
    ids_to_remove = [m['id'] for m in measurements if m['sensor_station_address'] == adr_to_remove]
    database.delete_all_measurements(ids_to_remove)
    measurements = database.get_all_measurements()
    assert len(measurements) == 1
    assert DUMMY_ADR in [m['sensor_station_address'] for m in measurements]

def test_delete_multiple_measurements():
    """
    It is possible to delete multiple measurements from the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    adr_to_remove = 'to remove'
    database.enable_sensor_station(adr_to_remove)
    database.add_measurement(adr_to_remove, **DUMMY_MEASUREMENT)
    measurements = database.get_all_measurements()
    ids_to_remove = [m['id'] for m in measurements]
    database.delete_all_measurements(ids_to_remove)
    measurements = database.get_all_measurements()
    assert len(measurements) == 0

def test_delete_no_measurements():
    """
    It is possible to call the method for deleting measurements without measurements to delete
    and no error is raised
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.delete_all_measurements([])

def test_disable_sensor_station():
    """
    It is possible to disable a sensor station in the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    assert len(database.get_all_known_sensor_station_addresses()) == 1
    database.disable_sensor_station(DUMMY_ADR)
    assert len(database.get_all_known_sensor_station_addresses()) == 0
    assert len(database.get_all_disabled_sensor_station_addresses()) == 1

def test_reenable_sensor_station():
    """
    It is possible to re-enable a disabled sensor station and
    all previous measurements are deleted
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.enable_sensor_station('other')
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    database.update_limits(DUMMY_ADR, DUMMY_MEASUREMENT['sensor_name'], **DUMMY_SETTINGS)
    database.disable_sensor_station(DUMMY_ADR)
    database.enable_sensor_station(DUMMY_ADR)
    assert len(database.get_all_known_sensor_station_addresses()) == 2
    assert len(database.get_all_measurements()) == 0


def test_delete_sensor_station():
    """
    It is possible to delete a sensor station from the database and all associated measurements
    are deleted from the database
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.enable_sensor_station(DUMMY_ADR)
    database.add_measurement(DUMMY_ADR, **DUMMY_MEASUREMENT)
    assert len(database.get_all_known_sensor_station_addresses()) == 1
    assert len(database.get_all_measurements()) == 1
    database.delete_sensor_station(DUMMY_ADR)
    assert len(database.get_all_known_sensor_station_addresses()) == 0
    assert len(database.get_all_measurements()) == 0

def test_disable_unknown_sensor_station():
    """
    Disabling an unknown sensor station does not raise an error
    """
    database = Database(tempfile.NamedTemporaryFile().name)
    database.setup()
    database.disable_sensor_station(DUMMY_ADR)

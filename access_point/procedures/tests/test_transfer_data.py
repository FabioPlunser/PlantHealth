from procedures import transfer_data
from database import Database
from server import Server
from .mock_config import MockConfig

def test_transfers_data_to_backend_and_deletes_measurements(mocker):
    """
    Station connection information and all measurements are
    transfered to backend and afterwards deleted from the local db
    """
    station_data = ['st1', 'st2']
    measurements = [{'id': 'm1'}, {'id': 'm2'}]
    transferred_station_data = []
    transferred_measurements = []
    deleted_measurements = []
    mocker.patch('procedures.transfer_data_f.DB_FILENAME', ':memory:')
    mocker.patch.object(Database, 'get_all_states', return_value=station_data)
    mocker.patch.object(Database, 'get_all_measurements', return_value=measurements)
    def mock_transfer_data(self, station_data: list, measurements: list):
        for d in station_data:
            transferred_station_data.append(d)
        for m in measurements:
            transferred_measurements.append(m)
    mocker.patch.object(Server, 'transfer_data', mock_transfer_data)
    def mock_delete_all_measurements(self, measurement_ids: list):
        for m in measurement_ids:
            deleted_measurements.append(m)
    mocker.patch.object(Database, 'delete_all_measurements', mock_delete_all_measurements)
    transfer_data(MockConfig())
    assert transferred_station_data == station_data
    assert transferred_measurements == measurements
    assert deleted_measurements == [m.get('id') for m in measurements]


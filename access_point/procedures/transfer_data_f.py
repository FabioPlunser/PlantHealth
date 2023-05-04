import logging

from util import Config
from server import Server, TokenDeclinedError
from database import Database, DatabaseError, DB_FILENAME

log = logging.getLogger()


def transfer_data(config: Config) -> None:
    """
    Transfers measured sensor values, connection states and DIP switch positions
    of all known sensor stations to the server backend. Upon successful transfer
    measurements are deleted from the local filestorage.
    """
    backend = Server(config.backend_address, config.token)
    database = Database(DB_FILENAME)

    # get data
    log.info('Collecting data for transfer to backend')
    try:
        station_data = database.get_all_states()
        measurements = database.get_all_measurements()
    except DatabaseError as e:
        log.error(f'Unable to load data from database: {e}')
        return
    log.info(f'Found {len(measurements)} measurements for {len(station_data)} sensor stations')

    # transfer to backend
    if len(station_data):
        try:
            log.info('Starting transfer to backend')
            backend.transfer_data(station_data, measurements)
            log.info('Completed transfer to backend')
        except TokenDeclinedError:
            log.warning('The sensor station has been locked by backend')
            config.reset_token()
            return
        except ConnectionError as e:
            log.error(e)
            return
    else:
        log.info('Nothing to transfer')
    
    # delete transferred measurements from database
    measurement_ids = [m.get('id') for m in measurements]
    if measurement_ids:
        log.info('Deleting transfered measurements from database')
        try:
            database.delete_all_measurements(measurement_ids)
            log.info(f'Deleted {len(measurement_ids)} measurements from database')
        except DatabaseError as e:
            log.error(f'Unable to delete data from database: {e}')
            return

import sqlite3

from functools import wraps
from datetime import datetime, timedelta
from typing import Optional, Literal, Union, Any

from .table_setup import (
    CREATE_SENSOR_STATION_TABLE_QUERY,
    CREATE_SENSOR_TABLE_QUERY,
    CREATE_SENSOR_VALUE_TABLE_QUERY
)

# Name of the database file
DB_FILENAME = 'data.db'

class DatabaseError(Exception):
    """
    Exception thrown whenever a database related error occurs.
    """
    pass

class Database:
    """
    Handler class for the actual data storage.

    :param db_filename: Name of the database file.
    """

    # Number of failed connection attempts until a connection is flagged as lost
    MAX_FAILED_CONNECTION_ATTEMPTS = 3

    def __init__(self, db_filename: str) -> None:
        """
        Initializes the local SQLite database handler.
        Does not initialize the database file. Call setup() for that.

        :param db_filename: Name of the database file.
        """
        self._db_filename = db_filename
        self._conn :sqlite3.Connection = None
    
    def _with_connection(f) -> Any:
        """
        Internal only decorator method used for methods that require a database connection.
        """
        @wraps(f)
        def decorated(self, *args, **kwargs):
            try:
                self._conn = sqlite3.connect(self._db_filename)
                self._conn.execute('PRAGMA foreign_keys = 1')
                result = f(self, *args, **kwargs)
                self._conn.commit()
                self._conn.close()
                return result
            except sqlite3.Error as e:
                raise DatabaseError(e)
        return decorated
    
    @_with_connection
    def setup(self) -> None:
        """
        Initializes all required database tables.

        :raises DatabaseError: If one of the database tables could not be created
        """
        create_table = lambda query : self._conn.cursor().execute(query)
        
        create_table(CREATE_SENSOR_STATION_TABLE_QUERY)
        create_table(CREATE_SENSOR_TABLE_QUERY)
        create_table(CREATE_SENSOR_VALUE_TABLE_QUERY)

    @_with_connection
    def enable_sensor_station(self, address: str) -> None:
        """
        Adds a sensor station to the database (or re-enables it, if it already exists).

        :param address: Address of the sensor station to add
        :raises DatabaseError: If the sensor station could not be added
        """
        query = """
            INSERT OR REPLACE INTO sensor_station(address, timestamp_added)
            VALUES (?,?)
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (address, datetime.now()))

    @_with_connection
    def disable_sensor_station(self, address: str) -> None:
        """
        Disables a sensor station in the database.

        :param address: Address of the sensor station to disable
        :raises DatabaseError: If the sensor station could not be disabled
        """
        query = """
            UPDATE sensor_station
            SET disabled = 1
            WHERE address = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (address,))

    @_with_connection
    def delete_sensor_station(self, address: str) -> None:
        """
        Deletes a sensor station from the database.
        Also deletes all associated sensor and measurements.

        :param address: Address of the sensor station to disable
        :raises DatabaseError: If the sensor station could not be deleted
        """
        query = """
            DELETE FROM sensor_station
            WHERE address = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (address,))
        
    
    @_with_connection
    def add_measurement(self,
                        sensor_station_address: str,
                        sensor_name: str,
                        unit: Optional[str],
                        timestamp: datetime,
                        value: float,
                        alarm: Literal['n', 'l', 'h']) -> None:
        """
        Adds a measured value for a single sensor of a given sensor station to the database.
        The sensor is automatically created within the database if it does not exist yet.
        Also updates the timestamp of the last measured value within limits for the sensor
        and flags the connection to the sensor station as alive.

        :param sensor_station_address: Address of the sensor station
        :param sensor_name: Name of the sensor
        :param unit: Unit of the measurement - changes after first input will be ignored
        :param timestamp: Time at which the measurement was done
        :param value: Measured value
        :param alarm: Flag for active alarm ('n' -> no alarm | 'l' -> lower treshold | 'h' -> upper treshold)
        :raises DatabaseError: If the measurement could not be added
        """
        # add sensor if necessary
        query = """
            INSERT OR IGNORE INTO sensor(name, unit, sensor_station_id)
            SELECT ?, ?, st.id
            FROM sensor_station st
            WHERE st.address = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (sensor_name, unit, sensor_station_address))

        # add measurement
        query = """
            INSERT INTO sensor_value(timestamp, value, alarm, sensor_id)
            SELECT ?, ?, ?, s.id
            FROM sensor s
                JOIN sensor_station st ON s.sensor_station_id = st.id
            WHERE
                st.address = ? AND
                s.name = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(
            query,
            (
                timestamp,
                value,
                alarm,
                sensor_station_address,
                sensor_name
            )
        )

        # update timestamp of last measurement within limits if applicable
        query = """
            UPDATE sensor
            SET last_inside_limits = ?
            WHERE
                name = ? AND
                sensor_station_id IN (
                    SELECT id
                    FROM sensor_station
                    WHERE address = ?
                ) AND
                (lower_limit IS NULL OR lower_limit <= ?) AND
                (upper_limit IS NULL OR ? <= upper_limit)
        """
        cursor.execute(query, (timestamp, sensor_name, sensor_station_address, value, value))

        # set connection to sensor station to alive
        query = """
            UPDATE sensor_station
            SET
                failed_connection_attempts = 0,
                connection_alive = 1
            WHERE address = ?
        """
        cursor.execute(query, (sensor_station_address,))

    @_with_connection
    def set_dip_id(self, sensor_station_address: str, dip_id: int) -> None:
        """
        Sets the DIP switch position for a sensor station and flags the connection as alive.

        :param sensor_station_address: Address of the sensor station
        :param dip_id: Integer decoded position of the DIP switches
        :raises DatabaseError: If the DIP switch position could not be set
        """
        query = """
            UPDATE sensor_station
            SET
                dip_id = ?,
                failed_connection_attempts = 0,
                connection_alive = 1
            WHERE address = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (dip_id, sensor_station_address))

    @_with_connection
    def add_failed_connection_attempt(self, sensor_station_address: str) -> None:
        """
        Adds info that there was a failed connection attempt to a sensor station.
        After MAX_FAILED_CONNECTION_ATTEMPTS (default = 3) failed attempts the connection to the
        sensor station is flagged as lost.

        :param sensor_station_address: Address of the sensor station
        :raises DatabaseError: If the failed connection attempt could not be added or the 
            connection could not be flagged as lost
        """
        query = """
            UPDATE sensor_station
            SET
                failed_connection_attempts = failed_connection_attempts + 1,
                connection_alive = IIF(failed_connection_attempts + 1 >= ?, 0, 1)
            WHERE
                address = ? AND
                connection_alive = 1 AND
                failed_connection_attempts < ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (self.MAX_FAILED_CONNECTION_ATTEMPTS, sensor_station_address, self.MAX_FAILED_CONNECTION_ATTEMPTS))
    
    @_with_connection
    def update_limits(self,
                              sensor_station_address: str,
                              sensor_name: str,
                              lower_limit: Optional[float] = None,
                              upper_limit: Optional[float] = None,
                              alarm_tripping_time: Optional[int] = None,
                              **kwargs) -> None:
        """
        Updates the settings for a specific sensor. The sensor must already exist before
        settings are updated. Sensors are created when the first measurement for a sensor
        is added. See add_measurement().

        :param sensor_station_address: Address of the sensor station
        :param sensor_name: Name of the sensor
        :param lower_limit: Lower limit for the sensor value to trigger alarms
        :param upper_limit: Upper limit for the sensor value to trigger alarms
        :param alarm_tripping_time: Time in seconds until an alarm is triggered
        :raises DatabaseError: If the settings for a sensor could not be updated
        """
        query = """
            UPDATE sensor
            SET
                lower_limit = ?,
                upper_limit = ?,
                alarm_tripping_time = ?
            WHERE
                name = ? AND
                sensor_station_id IN (
                    SELECT id
                    FROM sensor_station
                    WHERE address = ?)
        """
        cursor = self._conn.cursor()
        cursor.execute(
            query,
            (
                lower_limit,
                upper_limit,
                alarm_tripping_time,
                sensor_name,
                sensor_station_address
            )
        )

    @_with_connection
    def get_all_known_sensor_station_addresses(self) -> list[str]:
        """
        Gets all sensor stations that are stored in the database and enabled.

        :return: List with the addresses of all enabled sensor stations
        :raises DatabaseError: If the sensor station addresses could not be retrieved
        """
        query = """
            SELECT address
            FROM sensor_station
            WHERE disabled = 0
            ORDER BY
                failed_connection_attempts ASC,
                timestamp_added DESC
        """
        cursor = self._conn.cursor()
        cursor.execute(query)

        rows = cursor.fetchall()
        addresses = [adr for (adr,) in rows]

        return addresses
    
    @_with_connection
    def get_all_disabled_sensor_station_addresses(self) -> list[str]:
        """
        Gets all sensor staitons that are stored in the database but disabled (marked for locking)

        :return: List with addresses of all disabled sensor stations
        :raises DatabaseError: If the sensor station addresses could not be retrieved
        """
        query = """
            SELECT address
            FROM sensor_station
            WHERE disabled = 1
        """
        cursor = self._conn.cursor()
        cursor.execute(query)

        rows = cursor.fetchall()
        addresses = [adr for (adr,) in rows]

        return addresses

    @_with_connection
    def get_all_measurements(self) -> list[dict[str, Union[int, str, datetime, float, None]]]:
        """
        Gets all measurements that are currently stored in the database.

        :return: A list of dictionaries constructed as

            "id": Id of the measurement for later deletion -> int

            "sensor_station_address": Address of the sensor station -> str

            "sensor_name": Name of the sensor -> str

            "unit": Unit of the measured value -> str | None

            "timestamp": Timestamp of the measurement -> datetime

            "value": Measured value -> float

            "alarm": Alarm active at the time of the measurement -> str ['n' no alarm | 'l' below limit | 'h' above limit]

        :raises DatabaseError: If the measurements could not be retrieved from the database
        """
        query = """
            SELECT v.id, st.address, s.name, s.unit, v.timestamp, v.value, v.alarm
            FROM sensor_station st
                JOIN sensor s on st.id = s.sensor_station_id
                JOIN sensor_value v on s.id = v.sensor_id
            WHERE st.disabled = 0
        """
        cursor = self._conn.cursor()
        cursor.execute(query)
        rows = cursor.fetchall()
        measurements = [
            {
                'id': id,
                'sensor_station_address': sensor_station_address,
                'sensor_name': sensor_name,
                'unit': unit,
                'timestamp': datetime.fromisoformat(timestamp),
                'value': value,
                'alarm': alarm
            } for (
                id,
                sensor_station_address,
                sensor_name,
                unit,
                timestamp,
                value,
                alarm
            ) in rows
        ]

        return measurements

    @_with_connection
    def get_limits(self,
                   sensor_station_address: str) -> dict[str, dict[str, Union[Optional[float], Optional[float], Optional[timedelta], Optional[datetime]]]]:
        """
        Gets the currently set limits and timing information on limits for all sensors of a sensor stations.

        :param sensor_station_address: Address of the sensor station
        :return: A dictionary with the sensor names as keys and subordinated dictionaries like:

            "lower_limit": The currently set lower limit -> float

            "upper_limit": The currently set upper limit -> float

            "alarm_tripping_time": The currently set time until tripping an alarm -> timedelta

            "last_inside_limits": The time at which the value has been inside limits at last
            (or the time at which the first measurement for the sensor has been added) -> datetime

        :raises DatabaseError: If the limits could not be retrieved from the database
        """
        query = """
            SELECT
                s.name,
                s.lower_limit,
                s.upper_limit,
                s.alarm_tripping_time,
                s.last_inside_limits
            FROM sensor_station st
                JOIN sensor s ON st.id = s.sensor_station_id
            WHERE
                st.address = ?
        """
        cursor = self._conn.cursor()
        cursor.execute(query, (sensor_station_address, ))
        rows = cursor.fetchall()
        limits = {sensor_name: {'lower_limit': lower_limit,
                                'upper_limit': upper_limit,
                                'alarm_tripping_time': timedelta(seconds=alarm_tripping_time) if alarm_tripping_time else None,
                                'last_inside_limits': datetime.fromisoformat(last_inside_limits) if last_inside_limits else None}
                  for (sensor_name, lower_limit, upper_limit, alarm_tripping_time, last_inside_limits) in rows}
        return limits
    
    @_with_connection
    def get_all_states(self) -> dict[str, dict[str, Union[bool, Optional[int]]]]:
        """
        Gets the states of all known sensor stations

        :return: A dictionary with the sensor station addresses as keys and subordinated dictionaries:

            "connection_alive": 'True' if the connection is alive, 'False' if not -> bool

            "dip_id": Integer encoded DIP switch position -> int

        :raises DatabaseError: If the states could not be retrieved from the database
        """
        query = """
            SELECT address, connection_alive, dip_id
            FROM sensor_station
        """
        cursor = self._conn.cursor()
        cursor.execute(query)
        rows = cursor.fetchall()
        connection_states = {adr: {'connection_alive': bool(connection),
                                   'dip_id': dip_id}
                             for (adr, connection, dip_id) in rows}
        return connection_states
    
    @_with_connection
    def delete_all_measurements(self, ids: list[int]) -> None:
        """
        Deletes all measurements with the given ids from the database.
        This method DOES NOT use a prepared SQL statement. Ensure
        that the ids parameter is not used for SQL injection.

        :param ids: List of IDs of measurements to delete
        :raises DatabaseError: If the ids could not be deleted from the database
        """
        if len(ids) == 0:
            return
        elif len(ids) == 1:
            args = f'({ids[0]})'
        else:
            args = tuple(ids)
        query = f"""
            DELETE FROM sensor_value
            WHERE id IN {args}
        """
        cursor = self._conn.cursor()
        cursor.execute(query)

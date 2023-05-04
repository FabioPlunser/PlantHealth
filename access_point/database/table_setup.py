# Query to create the table that contains information specific
# for a whole sensor station
CREATE_SENSOR_STATION_TABLE_QUERY = """
    CREATE TABLE IF NOT EXISTS sensor_station (
        id                          integer PRIMARY KEY,
        address                     text    UNIQUE,
        dip_id                      integer,
        timestamp_added             text    NOT NULL,
        failed_connection_attempts  integer DEFAULT 0 NOT NULL,
        connection_alive            integer DEFAULT false NOT NULL,
        disabled                    integer DEFAULT false NOT NULL
    );
"""

# Query to create the table that contains information specific for
# one single sensor of a sensor station
CREATE_SENSOR_TABLE_QUERY = """
    CREATE TABLE IF NOT EXISTS sensor (
        id                  integer PRIMARY KEY,
        name                text,
        unit                text,
        lower_limit         float,
        upper_limit         float,
        last_inside_limits  text,
        alarm_tripping_time integer,
        sensor_station_id   integer NOT NULL,
        FOREIGN KEY (sensor_station_id) REFERENCES sensor_station (id) ON DELETE CASCADE,
        UNIQUE (name, sensor_station_id)
    );
"""

# Query to create the table that contains the individual measurements
# for the sensors of the sensor stations
CREATE_SENSOR_VALUE_TABLE_QUERY = """
    CREATE TABLE IF NOT EXISTS sensor_value (
        id                  integer PRIMARY KEY,
        timestamp           text    NOT NULL,
        value               float   NOT NULL,
        alarm               text    NOT NULL,
        sensor_id           integer NOT NULL,
        FOREIGN KEY (sensor_id) REFERENCES sensor (id) ON DELETE CASCADE,
        UNIQUE (timestamp, sensor_id)
    );
"""
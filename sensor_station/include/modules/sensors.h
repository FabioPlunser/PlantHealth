
#ifndef SENSORS_H
#define SENSORS_H

#include <models/sensor_data.h>

sensor_data_t read_sensor_data();

void async_read_sensor_data(void (*handler)(sensor_data_t));

#endif

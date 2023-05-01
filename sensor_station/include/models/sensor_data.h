
#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <stdint.h>

typedef struct {
		uint16_t earth_humidity;
		uint16_t air_humidity;
		uint32_t air_pressure;
		uint16_t air_quality;
		uint16_t temperature;
		uint8_t light_intensity;
} sensor_data_t;

#endif


#include <stdbool.h>
#include <stdint.h>

typedef struct {
		uint16_t uuid : 16;
		uint8_t battery_level_status : 6;
		uint8_t unlocked : 1;
} sensor_station_info_t;
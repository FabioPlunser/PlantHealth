
#ifndef COMMUNICATION_H
#define COMMUNICATION_H

#include <models/sensor_data.h>
#include <models/sensor_types.h>
#include <stdbool.h>
#include <uuid.h>

using namespace std;

#include <string>

bool initialize_communication();

void enable_pairing_mode();
void disable_pairing_mode();

// Set Event Handler that is called when a device connects/disconnects.
void set_connected_event_handler(void (*handler)());
void set_disconnected_event_handler(void (*handler)());

// Set Event Handler that is called when a Characteristic is changed
void set_sensor_data_read_flag_set_event_handler(void (*handler)());
void set_unlocked_flag_set_event_handler(void (*handler)());
void set_limit_violation_event_handler(void (*handler)());

void clear_sensor_data_read_flag();

// Setters for the Characteristics
void set_sensor_data(sensor_data_t);
void set_battery_level_status(
	uint8_t flags, uint16_t power_state, uint8_t battery_level
);
void set_dip_switch_id(uint8_t);

string get_address();
void setTestValues();
void clearAllFlags();
void set_dip_switch_id(uint8_t id);
void set_sensorstation_id(uint8_t id);
uint8_t get_sensor_data_read_flag();
void set_sensorstation_locked_status(bool locked);
uint8_t get_sensorstation_locked_status();

#endif

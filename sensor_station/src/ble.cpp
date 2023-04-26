#include "Defines.h"

#if USE_DESCRIPTORS

// ----- Imports and Constant Definitions-----

using namespace std;

#include <ArduinoBLE.h>
#include <modules/communication.h>
#include <string>

// TODO: Better Error Handling and move into Header File
// Helper Macro for checking Errors returned from Functions.
// If Condition evaluates to false, the Program hangs.
#define CHECK_ERROR(__cond)                          \
	do {                                             \
		if (!(__cond)) {                             \
			while (true) {                           \
				if (Serial) {                        \
					Serial.print("In Error loop\n"); \
				}                                    \
				delay(5000);                         \
			}                                        \
		}                                            \
	} while (0)
// ----- Service and Characteristic Declaration -----

BLEService arduino_info_service("dea07cc4-d084-11ed-a760-325096b39f47");

BLECharacteristic
	battery_level_status_characteristic("2BED", BLERead | BLEIndicate, 1);

// "User Index" Characteristic = uint8_t
BLECharacteristic dip_switch_id_characteristic("2A9A", BLERead | BLENotify, 1);
BLEDescriptor dip_switch_id_user_descriptor("2901", "DIP-Switch");

BLECharacteristic
	sensor_station_unlocked_characteristic("2AE2", BLERead | BLEWrite, 1);
BLEDescriptor sensor_station_unlocked_user_descriptor("2901", "Unlocked");

BLECharacteristic sensor_station_id_characteristic("2ABF", BLERead, 16);
BLEDescriptor sensor_station_id_user_descriptor("2901", "Arduino UUID");

// ----- Sensor Data Characteristics

BLEService sensor_info_service("dea07cc4-d084-11ed-a760-325096b39f48");

BLECharacteristic sensor_values_read_characteristic(
	"2AE2", BLERead | BLEWrite | BLEIndicate, 1
);
BLEDescriptor sensor_values_read_descriptor("2901", "Sensor Data Read");

// -----------

BLECharacteristic earth_humidity_characteristic("2A6F", BLERead | BLENotify, 2);

BLECharacteristic air_humidity_characteristic("2A6F", BLERead | BLENotify, 2);

BLECharacteristic air_pressure_characteristic("2A6D", BLERead | BLENotify, 4);

BLECharacteristic temperature_characteristic("2B0D", BLERead | BLENotify, 2);

BLECharacteristic air_quality_characteristic("2B04", BLERead | BLENotify, 2);

BLECharacteristic
	light_intensity_characteristic("2AFF", BLERead | BLENotify, 2);

// -----------

BLECharacteristic
	earth_humidity_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

BLECharacteristic
	air_humidity_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

BLECharacteristic
	air_pressure_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

BLECharacteristic
	temperature_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

BLECharacteristic
	air_quality_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

BLECharacteristic
	light_intensity_valid_characteristic("2A9A", BLERead | BLEWrite, 1);

// -----------

// TODO: Check if i need to declare these seperately
//       for each Sensor Value and Sensor Valid Chararacteristic
BLEDescriptor earth_humidity_user_descriptor("2901", "Earth Humidity");
BLEDescriptor air_humidity_user_descriptor("2901", "Air Humidity");
BLEDescriptor air_pressure_user_descriptor("2901", "Air Pressure");
BLEDescriptor temperature_user_descriptor("2901", "Temperature");
BLEDescriptor air_quality_user_descriptor("2901", "Air Quality");
BLEDescriptor light_intensity_user_descriptor("2901", "Light Intensity");

// ----- Function Implementations -----

// ----- Startup -----

bool initialize_communication() {
	CHECK_ERROR(BLE.begin());

	BLE.setDeviceName("SensorStation");
	BLE.setLocalName("SensorStation");

	BLE.setAdvertisedService(arduino_info_service);
	BLE.setAdvertisedService(sensor_info_service);

	dip_switch_id_characteristic.addDescriptor(dip_switch_id_user_descriptor);
	sensor_station_unlocked_characteristic.addDescriptor(
		sensor_station_unlocked_user_descriptor
	);

	sensor_station_id_characteristic.addDescriptor(
		sensor_station_id_user_descriptor
	);

	sensor_values_read_characteristic.addDescriptor(
		sensor_values_read_descriptor
	);

	earth_humidity_characteristic.addDescriptor(earth_humidity_user_descriptor);
	air_humidity_characteristic.addDescriptor(air_humidity_user_descriptor);
	air_pressure_characteristic.addDescriptor(air_pressure_user_descriptor);
	temperature_characteristic.addDescriptor(temperature_user_descriptor);
	air_quality_characteristic.addDescriptor(air_quality_user_descriptor);
	light_intensity_characteristic.addDescriptor(light_intensity_user_descriptor
	);

	earth_humidity_valid_characteristic.addDescriptor(
		earth_humidity_user_descriptor
	);
	air_humidity_valid_characteristic.addDescriptor(air_humidity_user_descriptor
	);
	air_pressure_valid_characteristic.addDescriptor(air_pressure_user_descriptor
	);
	temperature_valid_characteristic.addDescriptor(temperature_user_descriptor);
	air_quality_valid_characteristic.addDescriptor(air_quality_user_descriptor);
	light_intensity_valid_characteristic.addDescriptor(
		light_intensity_user_descriptor
	);

	arduino_info_service.addCharacteristic(battery_level_status_characteristic);
	arduino_info_service.addCharacteristic(dip_switch_id_characteristic);
	arduino_info_service.addCharacteristic(
		sensor_station_unlocked_characteristic
	);

	arduino_info_service.addCharacteristic(sensor_station_id_characteristic);
	sensor_info_service.addCharacteristic(sensor_values_read_characteristic);
	sensor_info_service.addCharacteristic(earth_humidity_characteristic);
	sensor_info_service.addCharacteristic(air_humidity_characteristic);
	sensor_info_service.addCharacteristic(air_pressure_characteristic);
	sensor_info_service.addCharacteristic(temperature_characteristic);
	sensor_info_service.addCharacteristic(air_quality_characteristic);
	sensor_info_service.addCharacteristic(light_intensity_characteristic);
	sensor_info_service.addCharacteristic(earth_humidity_valid_characteristic);
	sensor_info_service.addCharacteristic(air_humidity_valid_characteristic);
	sensor_info_service.addCharacteristic(air_pressure_valid_characteristic);
	sensor_info_service.addCharacteristic(temperature_valid_characteristic);
	sensor_info_service.addCharacteristic(air_quality_valid_characteristic);
	sensor_info_service.addCharacteristic(light_intensity_valid_characteristic);

	BLE.addService(arduino_info_service);
	BLE.addService(sensor_info_service);
	return true;
}

// ----- Test Methode ----- //

void setTestValues() {
	uint8_t value = 1;
	battery_level_status_characteristic.writeValue(value++);
	dip_switch_id_characteristic.writeValue(value++);
	sensor_station_unlocked_characteristic.writeValue(value++);
	sensor_station_id_characteristic.writeValue(value++);
	sensor_values_read_characteristic.writeValue(value++);
	earth_humidity_characteristic.writeValue(value++);
	air_humidity_characteristic.writeValue(value++);
	air_pressure_characteristic.writeValue(value++);
	temperature_characteristic.writeValue(value++);
	air_quality_characteristic.writeValue(value++);
	light_intensity_characteristic.writeValue(value++);
	earth_humidity_valid_characteristic.writeValue(value++);
	air_humidity_valid_characteristic.writeValue(value++);
	air_pressure_valid_characteristic.writeValue(value++);
	temperature_valid_characteristic.writeValue(value++);
	air_quality_valid_characteristic.writeValue(value++);
	light_intensity_valid_characteristic.writeValue(value);
}

// ----- Pairing Mode -----

void enable_pairing_mode() { BLE.advertise(); }

void disable_pairing_mode() { BLE.stopAdvertise(); }

// ----- Set Event Handler -----

void set_connected_event_handler(void (*handler)()) {
	BLE.setEventHandler(BLEConnected, (BLEDeviceEventHandler) handler);
}

void set_disconnected_event_handler(void (*handler)()) {
	BLE.setEventHandler(BLEDisconnected, (BLEDeviceEventHandler) handler);
}

void set_sensor_data_read_flag_set_event_handler(void (*handler)()) {
	sensor_values_read_characteristic.setEventHandler(
		BLEWritten, (BLECharacteristicEventHandler) handler
	);
}

void set_unlocked_flag_set_event_handler(void (*handler)()) {
	sensor_station_unlocked_characteristic.setEventHandler(
		BLEWritten, (BLECharacteristicEventHandler) handler
	);
}

void set_limit_violation_event_handler(void (*handler)()) {
	// TODO
}

// ----- Set Event Handler -----

void clear_sensor_data_read_flag() {
	sensor_values_read_characteristic.writeValue((uint8_t) false);
}

void set_sensor_data(sensor_data_t sensor_data) {
	// TODO
	clear_sensor_data_read_flag();
}

void set_battery_level_status(battery_level_status_t battery_level_status) {
	// TODO
}

void set_dip_switch_id(uint8_t id) {
	dip_switch_id_characteristic.writeValue(id);
}

string get_address() { return BLE.address().c_str(); }

// ----------

#else

// ----- Imports and Constant Definitions-----

using namespace std;

#include <ArduinoBLE.h>
#include <modules/communication.h>
#include <string>

// TODO: Better Error Handling and move into Header File
// Helper Macro for checking Errors returned from Functions.
// If Condition evaluates to false, the Program hangs.
#define CHECK_ERROR(__cond)                          \
	do {                                             \
		if (!(__cond)) {                             \
			while (true) {                           \
				if (Serial) {                        \
					Serial.print("In Error loop\n"); \
				}                                    \
				delay(5000);                         \
			}                                        \
		}                                            \
	} while (0)

// ----- Service and Characteristic Declaration ----- //

// ----- Arduino Info ----- //

BLEService arduino_info_service("dea07cc4-d084-11ed-a760-325096b39f47");

BLECharacteristic
	battery_level_status_characteristic("2BED", BLERead | BLEIndicate, 4);
BLECharacteristic dip_switch_id_characteristic("2A9A", BLERead | BLENotify, 1);
BLECharacteristic
	sensor_station_unlocked_characteristic("2AE2", BLERead | BLEWrite, 1);
BLECharacteristic sensor_station_id_characteristic("2ABF", BLERead, 16);

// ----- General Sensor Information ----- //

BLEService general_sensor_service("dea07cc4-d084-11ed-a760-325096b39f48");
BLECharacteristic
	sensor_values_read_characteristic("2AE2", BLEWrite | BLERead, 1);

// ----- Soil Humidity Sensor Service ----- //

BLEService soil_humidity_sensor_service("dea07cc4-d084-11ed-a760-325096b39f49");
BLECharacteristic soil_humidity_characteristic("2A6F", BLERead | BLENotify, 2);
BLECharacteristic
	soil_humidity_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

// ----- Air Quality Sensor Service ----- //

BLEService air_humidity_sensor_service("dea07cc4-d084-11ed-a760-325096b39f4a");
BLECharacteristic air_humidity_characteristic("2A6F", BLERead | BLENotify, 2);
BLECharacteristic
	air_humidity_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

BLEService air_pressure_sensor_service("dea07cc4-d084-11ed-a760-325096b39f4b");
BLECharacteristic air_pressure_characteristic("2A6D", BLERead | BLENotify, 4);
BLECharacteristic
	air_pressure_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

BLEService air_temperature_sensor_service("dea07cc4-d084-11ed-a760-325096b39f4c"
);
BLECharacteristic temperature_characteristic("2B0D", BLERead | BLENotify, 2);
BLECharacteristic
	temperature_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

BLEService air_quality_sensor_service("dea07cc4-d084-11ed-a760-325096b39f4d");
BLECharacteristic air_quality_characteristic("2B04", BLERead | BLENotify, 2);
BLECharacteristic
	air_quality_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

// ----- Light Sensor Service ----- //

BLEService light_sensor_service("dea07cc4-d084-11ed-a760-325096b39f4e");
BLECharacteristic
	light_intensity_characteristic("2AFF", BLERead | BLENotify, 2);
BLECharacteristic
	light_intensity_valid_characteristic("2A9A", BLEWrite | BLERead, 1);

// ----- Function Implementations -----

// ----- Startup -----

bool initialize_communication() {
	CHECK_ERROR(BLE.begin());

	BLE.setDeviceName("SensorStation");
	BLE.setLocalName("SensorStation");

	// ----- Arduino Info Service ----- //

	BLE.setAdvertisedService(arduino_info_service);
	arduino_info_service.addCharacteristic(battery_level_status_characteristic);
	arduino_info_service.addCharacteristic(dip_switch_id_characteristic);
	arduino_info_service.addCharacteristic(
		sensor_station_unlocked_characteristic
	);
	arduino_info_service.addCharacteristic(sensor_station_id_characteristic);
	BLE.addService(arduino_info_service);

	// ----- General Purpose Service ----- //

	BLE.setAdvertisedService(general_sensor_service);
	general_sensor_service.addCharacteristic(sensor_values_read_characteristic);
	BLE.addService(general_sensor_service);

	// ----- Soil Humidity Sensor Service ----- //

	BLE.setAdvertisedService(soil_humidity_sensor_service);
	soil_humidity_sensor_service.addCharacteristic(soil_humidity_characteristic
	);
	BLE.addService(soil_humidity_sensor_service);

	// ----- Air Sensor Services ----- //

	BLE.setAdvertisedService(air_humidity_sensor_service);
	air_humidity_sensor_service.addCharacteristic(air_humidity_characteristic);
	air_humidity_sensor_service.addCharacteristic(
		air_humidity_valid_characteristic
	);
	BLE.addService(air_humidity_sensor_service);

	BLE.setAdvertisedService(air_pressure_sensor_service);
	air_pressure_sensor_service.addCharacteristic(air_pressure_characteristic);
	air_pressure_sensor_service.addCharacteristic(
		air_pressure_valid_characteristic
	);
	BLE.addService(air_pressure_sensor_service);

	BLE.setAdvertisedService(air_temperature_sensor_service);
	air_temperature_sensor_service.addCharacteristic(temperature_characteristic
	);
	air_temperature_sensor_service.addCharacteristic(
		temperature_valid_characteristic
	);
	BLE.addService(air_temperature_sensor_service);

	BLE.setAdvertisedService(air_quality_sensor_service);
	air_quality_sensor_service.addCharacteristic(air_quality_characteristic);
	air_quality_sensor_service.addCharacteristic(
		air_quality_valid_characteristic
	);
	BLE.addService(air_quality_sensor_service);

	// ----- Light Sensor Service ----- //

	BLE.setAdvertisedService(light_sensor_service);
	light_sensor_service.addCharacteristic(light_intensity_characteristic);
	light_sensor_service.addCharacteristic(light_intensity_valid_characteristic
	);
	BLE.addService(light_sensor_service);

	return true;
}

// ----- Test Methode ----- //

void setTestValues() {
	uint8_t value = 1;
	battery_level_status_characteristic.writeValue(value++);
	dip_switch_id_characteristic.writeValue(value++);
	sensor_station_unlocked_characteristic.writeValue(value++);
	sensor_station_id_characteristic.writeValue(value++);
	sensor_values_read_characteristic.writeValue(value++);
	soil_humidity_characteristic.writeValue(value++);
	soil_humidity_valid_characteristic.writeValue(value++);
	air_humidity_characteristic.writeValue(value++);
	air_pressure_characteristic.writeValue(value++);
	temperature_characteristic.writeValue(value++);
	air_quality_characteristic.writeValue(value++);
	air_humidity_valid_characteristic.writeValue(value++);
	air_pressure_valid_characteristic.writeValue(value++);
	temperature_valid_characteristic.writeValue(value++);
	air_quality_valid_characteristic.writeValue(value++);
	light_intensity_characteristic.writeValue(value++);
	light_intensity_valid_characteristic.writeValue(value++);
}

// ----- Pairing Mode -----

void enable_pairing_mode() { BLE.advertise(); }

void disable_pairing_mode() { BLE.stopAdvertise(); }

// ----- Set Event Handler -----

void set_connected_event_handler(void (*handler)()) {
	BLE.setEventHandler(BLEConnected, (BLEDeviceEventHandler) handler);
}

void set_disconnected_event_handler(void (*handler)()) {
	BLE.setEventHandler(BLEDisconnected, (BLEDeviceEventHandler) handler);
}

void set_sensor_data_read_flag_set_event_handler(void (*handler)()) {
	sensor_values_read_characteristic.setEventHandler(
		BLEWritten, (BLECharacteristicEventHandler) handler
	);
}

void set_unlocked_flag_set_event_handler(void (*handler)()) {
	sensor_station_unlocked_characteristic.setEventHandler(
		BLEWritten, (BLECharacteristicEventHandler) handler
	);
}

void set_limit_violation_event_handler(void (*handler)()) {
	// TODO
}

// ----- Set Event Handler -----

void clear_sensor_data_read_flag() {
	sensor_values_read_characteristic.writeValue((uint8_t) false);
}

void set_sensor_data(sensor_data_t sensor_data) {
	soil_humidity_characteristic.writeValue(sensor_data.earth_humidity);
	air_humidity_characteristic.writeValue(sensor_data.air_humidity);
	air_pressure_characteristic.writeValue(sensor_data.air_pressure);
	air_quality_characteristic.writeValue(sensor_data.air_quality);
	temperature_characteristic.writeValue(sensor_data.temperature);
	light_intensity_characteristic.writeValue(sensor_data.light_intensity);
	clear_sensor_data_read_flag();
}

void set_battery_level_status(
	uint8_t flags, uint16_t power_state, uint8_t battery_level
) {
	uint32_t batteyStatus = (battery_level << 24) + (power_state << 8) + flags;
	battery_level_status_characteristic.writeValue(batteyStatus);
}

void set_sensorstation_id(uint8_t id) {
	sensor_station_id_characteristic.writeValue(id);
}

void set_sensorstation_locked_status(bool locked) {
	sensor_station_unlocked_characteristic.writeValue((uint8_t) locked);
}

void set_dip_switch_id(uint8_t id) {
	dip_switch_id_characteristic.writeValue(id);
}

string get_address() { return BLE.address().c_str(); }

uint8_t get_sensor_data_read_flag() {
	uint8_t value;
	sensor_values_read_characteristic.readValue(value);
	return value;
}

uint8_t get_sensor_station_locked_status(){
	uint8_t value;
	sensor_station_unlocked_characteristic.readValue(value);
	return value;
}

// ---------- Set Flags

void clearAllFlags() {
	uint8_t value = 0;
	soil_humidity_valid_characteristic.writeValue(value);
	air_humidity_valid_characteristic.writeValue(value);
	air_pressure_valid_characteristic.writeValue(value);
	temperature_valid_characteristic.writeValue(value);
	air_quality_valid_characteristic.writeValue(value);
	light_intensity_valid_characteristic.writeValue(value);
}

#endif


#include <Defines.h>

#ifdef DO_MAIN

#include "../lib/NotificationHandler/SensorErrors.h"
#include "SensorClasses/AirSensor.cpp"
#include "SensorClasses/DipSwitch.cpp"
#include "SensorClasses/Hydrometer.cpp"
#include "SensorClasses/Phototransistor.cpp"

#include <Adafruit_BME680.h>
#include <Arduino.h>
#include <ArduinoBLE.h>
#include <modules/communication.h>

// ----- Prototypes ----- //

bool setSensorValuesInBLE();
AirSensorClass::UPDATE_ERROR setSensorValuesFromSensors(sensor_data_t * str);
uint16_t convertToGATT_soilHumidity(float humidity);
uint16_t convertToGATT_soilHumidity_notKnown();
uint16_t convertToGATT_airHumidity(float humidity);
uint16_t convertToGATT_airHumidity_notKnown();
uint32_t convertToGATT_airPressure(float pressure);
uint32_t convertToGATT_airPressure_notKnown();
uint8_t convertToGATT_airQuality(float gas_resistance);
uint8_t convertToGATT_airQuality_notKnown();
int8_t convertToGATT_airTemperature(float temperature);
int8_t convertToGATT_airTemperature_notKnown();
uint16_t convertToGATT_lightIntensity(uint16_t lightIntensity);
uint16_t convertToGATT_lightIntensity_notKnown();
uint16_t luminosityFromVoltage(uint16_t measured);

// ----- Global Variables ----- //

AirSensorClass * airSensor;
HydrometerClass * hydrometer;
PhototransistorClass * phototransistor;
DipSwitchClass * dipSwitch;
Adafruit_BME680 bme680;

// ----- Setup ----- //

void setup() {
	Serial.begin(115200);
	airSensor				= new AirSensorClass(&bme680);
	hydrometer				= new HydrometerClass(PIN_HYDROMETER);
	phototransistor			= new PhototransistorClass(PIN_PHOTOTRANSISTOR);
	uint8_t pinConnection[] = {PIN_DIP_1, PIN_DIP_2, PIN_DIP_3, PIN_DIP_4,
							   PIN_DIP_5, PIN_DIP_6, PIN_DIP_7, PIN_DIP_8};
	dipSwitch				= new DipSwitchClass(pinConnection, 8);

	initialize_communication();
	enable_pairing_mode();

	// while (!Serial) {
	// 	delay(50);
	// }

	delay(1000);
}

// ----- Loop ----- //

void loop() {
	static arduino::String pairedDevice;
	static bool inPairingMode = false;
// Activate pairing mode if button is pressed pressed.
#if PAIRING_BUTTON_REQUIRED
	static unsigned long pairingTime = 0;
	if (digitalRead(PIN_BUTTON_1) == PinStatus::HIGH) {
		Serial.print("Pairing Button is pressed\n");
		enable_pairing_mode();
		set_sensorstation_locked_status(SENSOR_STATION_LOCKED_VALUE);
		inPairingMode = true;
		pairingTime	  = millis();
		// If button is not pressed for "DURATION_IN_PAIRING_MODE_MS" time it
		// will got back to normal mode
	} else if (millis() - pairingTime > DURATION_IN_PAIRING_MODE_MS && inPairingMode) {
		Serial.print("Pairing mode ended\n");
		inPairingMode = false;
	}
#else
	inPairingMode = true;
	enable_pairing_mode();
#endif
	// enable_pairing_mode();
	BLEDevice central = BLE.central();
	if (central) {
		Serial.println("* Connected to central device!");
		Serial.print("* Device MAC address: ");
		Serial.println(central.address());
		Serial.println(" ");
		if (pairedDevice.compareTo(central.address()) == 0) {
			Serial.print("Central device is remembered device.\n");
			if (central.connected()) {
				Serial.print("Connected\n");
				if (get_sensorstation_locked_status() ==
					SENSOR_STATION_UNLOCKED_VALUE) {
					setSensorValuesInBLE();
				}
				while (central.connected()) {
					; // TODO: Timeout required
				}
			}
		}
		Serial.println("* Disconnected from central device!");
		if (get_sensor_data_read_flag() == true) {
			clear_sensor_data_read_flag();
		} else {
			ERROR_PRINT(
				"Sensor flag was not cleared. Value was ",
				get_sensor_data_read_flag()
			);
		}
	}
	Serial.println("Searching for central device!");
	delay(500);
}

// ----- Functions ----- //

bool setSensorValuesInBLE() {
	unsigned long startTime = millis();
	sensor_data_t sensorData;
	AirSensorClass::UPDATE_ERROR updateError =
		setSensorValuesFromSensors(&sensorData);
	uint8_t dipSwitchId = dipSwitch->getdipSwitchValue();

#if PRINT_TIME_READ_SENSOR
	char buffer[16];
	Serial.print("Time taken to read sensor values: ");
	sprintf(buffer, "%.3f.", float(millis() - startTime) / 1000);
	Serial.println(buffer);
#endif

	if (updateError == AirSensorClass::UPDATE_ERROR::NOTHING) {
		set_sensor_data(sensorData);
	} else {
		ERROR_PRINT("Got update Error ", updateError);
		return false;
	}
	set_dip_switch_id(dipSwitchId);
	clearAllFlags();

	set_sensorstation_locked_status(false);
	battery_level_status_t batteryStatus = {0};
	set_battery_level_status(batteryStatus);
	set_sensorstation_id(0);
	return true;
}

AirSensorClass::UPDATE_ERROR setSensorValuesFromSensors(sensor_data_t * str) {
	float pressure = 0, temperature = 0, humidity = 0;
	uint32_t gas_resistance	 = 0;
	uint16_t earth_humidity	 = hydrometer->getHumidity_10bit();
	uint16_t light_intensity = phototransistor->getLighting_10bit();

	AirSensorClass::UPDATE_ERROR updateError = airSensor->getMeasuredValues(
		&pressure, &gas_resistance, &temperature, &humidity
	);
	if (updateError == AirSensorClass::UPDATE_ERROR::NOTHING) {
		str->air_humidity = convertToGATT_airHumidity(humidity);
		str->air_quality  = convertToGATT_airQuality(gas_resistance);
		str->temperature  = convertToGATT_airTemperature(temperature);
		str->air_pressure = convertToGATT_airPressure(pressure);
	} else {
		str->air_humidity = convertToGATT_airHumidity_notKnown();
		str->air_quality  = convertToGATT_airQuality_notKnown();
		str->temperature  = convertToGATT_airTemperature_notKnown();
		str->air_pressure = convertToGATT_airPressure_notKnown();
	}
	str->earth_humidity	 = convertToGATT_soilHumidity(earth_humidity);
	str->light_intensity = convertToGATT_lightIntensity(light_intensity);
	return updateError;
};

uint16_t convertToGATT_soilHumidity(float humidity) {
	if (humidity < 0 || humidity > 100) {
		return convertToGATT_soilHumidity_notKnown();
	}
	return uint16_t(humidity * 100);
}

uint16_t convertToGATT_soilHumidity_notKnown() { return (uint16_t) 0xFFFF; }

uint16_t convertToGATT_airHumidity(float humidity) {
	if (humidity < 0 || humidity > 100) {
		return convertToGATT_airHumidity_notKnown();
	}
	return uint16_t(humidity * 100);
}
uint16_t convertToGATT_airHumidity_notKnown() { return (uint16_t) 0xFFFF; }

uint32_t convertToGATT_airPressure(float pressure) {
	return uint32_t(pressure * 10);
}
uint32_t convertToGATT_airPressure_notKnown() { return (uint8_t) 0xFFFFFFFF; }

uint8_t convertToGATT_airQuality(float gas_resistance) {
	const float calibrationValue = 146000;
	return (uint8_t) (100 - (gas_resistance / calibrationValue) * 100);
}
uint8_t convertToGATT_airQuality_notKnown() { return (uint8_t) 0xFF; }

int8_t convertToGATT_airTemperature(float temperature) {
	if (temperature < -64 || temperature > 63) {
		return convertToGATT_airTemperature_notKnown();
	}
	return int8_t(temperature);
}
int8_t convertToGATT_airTemperature_notKnown() { return (int8_t) 0x7F; }

uint16_t convertToGATT_lightIntensity(uint16_t lightIntensity) {
	uint16_t luminosity = luminosityFromVoltage(lightIntensity);
	if (luminosity > 65534) {
		return convertToGATT_lightIntensity_notKnown();
	}
	return luminosity;
}
uint16_t convertToGATT_lightIntensity_notKnown() { return (uint16_t) 0xFFFF; }

uint16_t luminosityFromVoltage(uint16_t measured) {
	const float R	= 2200;
	const float Vin = 3.3;
	float Vout		= float(measured) / 1023 * Vin; // Convert analog to voltage
	float diodaResistance =
		(R * (Vin - Vout)) / Vout; // Convert voltage to resistance
	return 500 / (diodaResistance / 1000
				 ); // Convert resitance to kOhm and afterwards to lumen
					// TODO: Check 500 as max luminosity
}
#endif
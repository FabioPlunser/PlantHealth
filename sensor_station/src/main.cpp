
#include <Defines.h>

#ifdef DO_MAIN

#include "../lib/NotificationHandler/SensorErrors.h"
// #include "../lib/errorHandler/SensorErrors.h"
#include "SensorClasses/AirSensor.cpp"
#include "SensorClasses/DipSwitch.cpp"
#include "SensorClasses/Hydrometer.cpp"
#include "SensorClasses/Phototransistor.cpp"

#include <Adafruit_BME680.h>
#include <Arduino.h>
#include <ArduinoBLE.h>
#include <CompilerFunctions.hpp>
#include <NotificationHandler.hpp>
#include <modules/communication.h>

// ----- Prototypes ----- //

bool setSensorValuesInBLE();
AirSensorClass::UPDATE_ERROR setSensorValuesFromSensors(sensor_data_t * str);
uint16_t convertToGATT_soilHumidity(uint16_t humidity);
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
void setArduinoPowerStatus();
bool updateNotificationHandler();

// ----- Global Variables ----- //

AirSensorClass * airSensor;
HydrometerClass * hydrometer;
PhototransistorClass * phototransistor;
DipSwitchClass * dipSwitch;
NotificationHandler * notificationHandler;
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
	notificationHandler		= NotificationHandler::getInstance(
		PIN_RGB_RED, PIN_RGB_GREEN, PIN_RGB_BLUE
	);

	initialize_communication();

	// while (!Serial) {
	// 	delay(50);
	// }
	// while (!Serial) {
	// 	delay(50);
	// }

	delay(1000);
}

// ----- Loop ----- //

void loop() {
	static arduino::String pairedDevice;
	static bool inPairingMode		= false;
	static bool notificationPresent = false;
// Activate pairing mode if button is pressed pressed.
#if PAIRING_BUTTON_REQUIRED
	static unsigned long pairingTime = 0;
	if (digitalRead(PIN_BUTTON_1) == PinStatus::HIGH) {
		DEBUG_PRINT(1, "Pairing Button is pressed\n");
		enable_pairing_mode();
		set_sensorstation_locked_status(SENSOR_STATION_LOCKED_VALUE);
		inPairingMode = true;
		pairingTime	  = millis();
		// If button is not pressed for "DURATION_IN_PAIRING_MODE_MS" time it
		// will got back to normal mode
	} else if (millis() - pairingTime > DURATION_IN_PAIRING_MODE_MS && inPairingMode) {
		DEBUG_PRINT(1, "Pairing mode ended\n");
		inPairingMode = false;
	}
#else
	inPairingMode = true;
	enable_pairing_mode();
#endif
	// enable_pairing_mode();
	BLEDevice central = BLE.central();
	if (central) {
		uint8_t dipSwitchId = dipSwitch->getdipSwitchValue();
		set_dip_switch_id(dipSwitchId);
		if (inPairingMode) {
			pairedDevice = central.address();
			DEBUG_PRINT(1, "In pairing mode\n");
			DEBUG_PRINT(1, "New device is: ");
			DEBUG_PRINTLN(1, pairedDevice);
			inPairingMode = false;
		}
		DEBUG_PRINTLN(1, "* Connected to central device!");
		DEBUG_PRINT(1, "* Device MAC address: ");
		DEBUG_PRINTLN(1, central.address());
		DEBUG_PRINTLN(1, " ");
		if (pairedDevice.compareTo(central.address()) == 0) {
			DEBUG_PRINT(1, "Central device is remembered device.\n");
			if (central.connected()) {
				DEBUG_PRINT(1, "Connected\n");
				if (get_sensorstation_locked_status() ==
					SENSOR_STATION_UNLOCKED_VALUE) {
					setSensorValuesInBLE();
				}
				while (central.connected()) {
					; // TODO: Timeout required
				}
			}
		} else {
			DEBUG_PRINT(1, "Declined connection to ");
			DEBUG_PRINTLN(1, central.address());
			central.disconnect();
		}
		DEBUG_PRINTLN(1, "* Disconnected from central device!");
		notificationPresent = updateNotificationHandler();
		if (get_sensor_data_read_flag() == true) {
			clear_sensor_data_read_flag();
		} else {
			ERROR_PRINT(
				"Sensor flag was not cleared. Value was ",
				get_sensor_data_read_flag()
			);
		}

		DEBUG_PRINT(1, "Station is unlocked: ");
		DEBUG_PRINTLN(1, get_sensorstation_locked_status());
	}
	static int i = 0;
	if (inPairingMode && i++ > 3) {
		DEBUG_PRINTLN(1, "Searching for central device!");
		i = 0;
	}
	if (notificationPresent) {
		DEBUG_PRINT_POS(1, "Notifcation is present\n");
		unsigned long startNotificationCheck = millis();
		int32_t timeTillNext				 = notificationHandler->update();
		if (timeTillNext < 0) {
			notificationPresent = false;
		} else {
			while ((unsigned long) timeTillNext <
				   TIME_CHECK_BLE_CENTRAL_PRESENT_MS -
					   (millis() - startNotificationCheck)) {
				DEBUG_PRINTF_POS(2, "Will wait for %ld ms\n", timeTillNext);
				delay(timeTillNext);
				timeTillNext = notificationHandler->update();
			}
		}

	} else {
		delay(TIME_CHECK_BLE_CENTRAL_PRESENT_MS);
	}
}

// ----- Functions ----- //

/**
 * @returns True if there is a notification in the queue, else false.
 */
bool updateNotificationHandler() {
	notificationHandler->updateAirHumidityValid(get_air_humidity_valid());
	notificationHandler->updateAirPressureValid(get_air_pressure_valid());
	notificationHandler->updateAirTemperatureValid(get_temperature_valid());
	notificationHandler->updateAirQualityValid(get_air_quality_valid());
	notificationHandler->updateSoilHumidityValid(get_air_humidity_valid());
	notificationHandler->updateLightIntensityValid(get_light_intensity_valid());
	return notificationHandler->update() != -1;
}

bool setSensorValuesInBLE() {
	unsigned long startTime = millis();
	sensor_data_t sensorData;
	AirSensorClass::UPDATE_ERROR updateError =
		setSensorValuesFromSensors(&sensorData);

#if PRINT_TIME_READ_SENSOR
	DEBUG_PRINTF(
		0, "Time taken to read sensor values: %.3f.\n",
		float(millis() - startTime) / 1000
	);
#endif

	if (updateError == AirSensorClass::UPDATE_ERROR::NOTHING) {
		set_sensor_data(sensorData);
	} else {
		ERROR_PRINT("Got update Error ", updateError);
		return false;
	}
	clearAllFlags();

	set_sensorstation_locked_status(false);
	setArduinoPowerStatus();
	set_sensorstation_id(0);
	return true;
}

void setArduinoPowerStatus() {
	set_battery_level_status(
		BATTERY_LEVEL_FLAGS_FIELD, BATTERY_POWER_STATE_FIELD, 100
	);
}

AirSensorClass::UPDATE_ERROR setSensorValuesFromSensors(sensor_data_t * str) {
	float pressure = 0, temperature = 0, humidity = 0;
	uint32_t gas_resistance	 = 0;
	uint16_t earth_humidity	 = hydrometer->getHumidity_16bit();
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

uint16_t convertToGATT_soilHumidity(uint16_t humidity) {
	DEBUG_PRINTF(2, "Soil humidity = %u\n", humidity);

	static uint16_t valueHigh = 300;
	static uint16_t valueLow  = 950;
	float calculation =
		100 - ((humidity - valueHigh) / (float) (valueLow - valueHigh) * 100);

	DEBUG_PRINTF(2, "Calculated = %lf\n", calculation);
	if (calculation < 0 || calculation > 100) {
		return convertToGATT_soilHumidity_notKnown();
	}
	return uint16_t(calculation * 100);
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
uint32_t convertToGATT_airPressure_notKnown() { return (uint8_t) 0xFFFF'FFFF; }

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
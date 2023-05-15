
#include <Defines.h>

#ifdef DO_MAIN

#include "../lib/NotificationHandler/SensorErrors.h"
#include "SensorClasses/AirSensor.cpp"
#include "SensorClasses/DipSwitch.cpp"
#include "SensorClasses/Hydrometer.cpp"
#include "SensorClasses/Phototransistor.cpp"
#include "SensorClasses/SensorValueHandler.hpp"

#include <Adafruit_BME680.h>
#include <Arduino.h>
#include <ArduinoBLE.h>
#include <CompilerFunctions.hpp>
#include <NotificationHandler.hpp>
#include <cmath>
#include <modules/communication.h>

// ----- Prototypes ----- //

void setArduinoPowerStatus();
bool updateNotificationHandler_Errors();
bool updateNotificationHandler_PairingMode(bool active);
unsigned long handleNotificationIfPresent(bool notificationPresent);
void checkPairingButtonAndStatus(bool & inPairingMode);
void handleCentralDeviceIfPresent(
	arduino::String & pairedDevice, bool & inPairingMode
);
void checkNotificationSilenceButtonPressed();
void setValueInVerifiedCentralDevice(BLEDevice & central);
unsigned long calculateTimeBetweenMeasures(
	unsigned long now, unsigned long firstMeasure, unsigned long timeMin,
	unsigned long timeMax, unsigned long totalTillMax
);
double sensorValueWeightCalculationFunction(
	unsigned long timeNow, unsigned long timeLastUpdate,
	unsigned long timeLastReset
);

// ----- Global Variables ----- //

AirSensorClass * airSensor;
HydrometerClass * hydrometer;
PhototransistorClass * phototransistor;
DipSwitchClass * dipSwitch;
NotificationHandler * notificationHandler;
Adafruit_BME680 bme680;
SensorValueHandlerClass * sensorValueHandler;

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
	sensorValueHandler = SensorValueHandlerClass::getInstance(
		airSensor, hydrometer, phototransistor
	);
	sensorValueHandler->setWeightCalculatorFunction(
		sensorValueWeightCalculationFunction
	);

	initialize_communication();

#if WAIT_FOR_SERIAL_CONNECTION
	while (!Serial) {
		delay(50);
	}
#endif

	delay(1000);
}

// ----- Loop ----- //

void loop() {
	static arduino::String pairedDevice			   = "";
	static bool inPairingMode					   = true;
	static unsigned long timeBetweenMeasures	   = 0;
	static unsigned long previousDataTransmission  = millis();
	static unsigned long previousSensorMeasurement = millis();
#if PAIRING_BUTTON_REQUIRED
	checkPairingButtonAndStatus(inPairingMode);
	updateNotificationHandler_PairingMode(inPairingMode);
	enable_pairing_mode();
#else
	inPairingMode = true;
#endif
	checkNotificationSilenceButtonPressed();
	handleCentralDeviceIfPresent(pairedDevice, inPairingMode);
	// If sensor data got transmitted we want to measure new values directly.
	if (get_sensor_data_read_flag() == SENSOR_DATA_READ_VALUE) {
		timeBetweenMeasures		 = 0;
		previousDataTransmission = millis();
	}

	static int i = 0;
	if (inPairingMode && i++ > 3) {
		DEBUG_PRINTLN(1, "Searching for central device!");
		i = 0;
	}
	unsigned long remainingSleepTime =
		handleNotificationIfPresent(!notificationHandler->isEmpty());
	// If the sensor station goes from unlocked to locked, all sensor data will
	// be deleted to get the current sensor data on the next unlock.
	static bool prevSensorStationLockedStatus =
		get_sensorstation_locked_status();
	if (prevSensorStationLockedStatus != get_sensorstation_locked_status()) {
		sensorValueHandler->resetAccumulators();
		prevSensorStationLockedStatus = get_sensorstation_locked_status();
	}
	// If the time between sensor measurements passed the next measurement will
	// done, but only if the sensor station is unlocked.
	if (get_sensorstation_locked_status() == true &&
		millis() - previousSensorMeasurement > timeBetweenMeasures) {
		unsigned long sensorReadStart = millis();
		timeBetweenMeasures			  = calculateTimeBetweenMeasures(
			  sensorReadStart, previousDataTransmission,
			  TIME_BETWEEN_SENSOR_MEASUREMENTS_MIN_S,
			  TIME_BETWEEN_SENSOR_MEASUREMENTS_MAX_S,
			  TIME_IT_TAKES_TO_REACH_MAX_MEASUREMENT
		  );
		sensorValueHandler->addSensorValuesToAccumulator();
		previousSensorMeasurement = millis();
		// Substract time it took to measur sensor values from remaining sleep
		// time
		unsigned long passedTime  = millis() - sensorReadStart;
		remainingSleepTime		  = remainingSleepTime < passedTime
										? 0
										: remainingSleepTime - passedTime;
	}
	delay(remainingSleepTime);
}

// ----- Functions ----- //

double sensorValueWeightCalculationFunction(
	unsigned long timeNow, unsigned long timeLastUpdate,
	unsigned long timeLastReset
) {
	DEBUG_PRINT_POS(4, "\n");
	return (double) (timeNow - timeLastUpdate);
}

unsigned long calculateTimeBetweenMeasures(
	unsigned long now, unsigned long firstMeasure, unsigned long timeMin,
	unsigned long timeMax, unsigned long totalTillMax
) {
	float normedValue = float(now - firstMeasure) / totalTillMax;
	if (normedValue > 1) {
		return timeMax;
	}
	float waitFactor = pow((std::sin(PI / 2 * normedValue)), 2);
	return (unsigned long) (waitFactor * (timeMax - timeMin) + timeMin);
}

void checkNotificationSilenceButtonPressed() {
	DEBUG_PRINT_POS(4, "\n");
	if (digitalRead(PIN_BUTTON_2) == PinStatus::HIGH) {
		notificationHandler->silenceNotification(
			TIME_IN_NOTIFICATION_SILENCE_MODE_MS
		);
	}
}

void checkPairingButtonAndStatus(bool & inPairingMode) {
	DEBUG_PRINT_POS(4, "\n");
	static unsigned long pairingTime = 0;
	DEBUG_PRINTF_POS(
		3, "Checking pairing button and status. In pairing mode value is %d.\n",
		inPairingMode
	);
	if (digitalRead(PIN_BUTTON_1) == PinStatus::HIGH) {
		DEBUG_PRINT_POS(2, "Pairing Button is pressed\n");
		enable_pairing_mode();
		set_sensorstation_locked_status(SENSOR_STATION_LOCKED_VALUE);
		inPairingMode = true;
		pairingTime	  = millis();

		// If button is not pressed for "DURATION_IN_PAIRING_MODE_MS" time it
		// will got back to normal mode
	} else if (millis() - pairingTime > DURATION_IN_PAIRING_MODE_MS && inPairingMode) {
		DEBUG_PRINT(1, "Pairing mode ended due to timeout\n");
		inPairingMode = false;
	}
}

void setValueInVerifiedCentralDevice(BLEDevice & central) {
	DEBUG_PRINT_POS(4, "\n");
	DEBUG_PRINT(1, "Central device is remembered device.\n");
	if (central.connected()) {
		DEBUG_PRINT(1, "Connected\n");
		if (get_sensorstation_locked_status() ==
			SENSOR_STATION_UNLOCKED_VALUE) {
			// setSensorValuesInBLE();
			if (!sensorValueHandler->setAccumulatedSensorValuesInBle()) {
				DEBUG_PRINT_POS(1, "SensorValueHandler returned false\n");
			} else {
				clear_sensor_data_read_flag();
			}
			set_sensorstation_id(0);
			setArduinoPowerStatus();
		}
		unsigned long connectionStart = millis();
		while (central.connected() &&
			   TIMEOUT_TIME_BLE_CONNECTION_MS > millis() - connectionStart) {
		}
	}
}

void handleCentralDeviceIfPresent(
	arduino::String & pairedDevice, bool & inPairingMode
) {
	DEBUG_PRINT_POS(4, "\n");
	BLEDevice central = BLE.central();
	if (central) {
		uint8_t dipSwitchId = dipSwitch->getdipSwitchValue();
		set_dip_switch_id(dipSwitchId);
		if (inPairingMode) {
			DEBUG_PRINTF(
				1,
				"New central trying to pair. Device is \"%s\" Current locking "
				"status = %d.\n",
				central.address().c_str(), get_sensorstation_locked_status()
			);
			while (central.connected() && get_sensorstation_locked_status() !=
											  SENSOR_STATION_UNLOCKED_VALUE) {
				delay(10);
			}
			if (!central.connected()) {
				DEBUG_PRINT(1, "Did not set unlocked bit.\n");
				return;
			}
			pairedDevice = central.address();
			DEBUG_PRINTF(1, "New device is: \"%s\".\n", pairedDevice.c_str());
			inPairingMode = false;
		}
		DEBUG_PRINTF(1, "Connected to %s.\n", central.address().c_str());
		if (pairedDevice.compareTo(central.address()) == 0) {
			setValueInVerifiedCentralDevice(central);
		} else {
			DEBUG_PRINTF(
				1, "Declined connection to \"%s\". Only \"%s\" was allowed.\n",
				central.address().c_str(), pairedDevice.c_str()
			);
			central.disconnect();
		}
		DEBUG_PRINTLN(1, "* Disconnected from central device!");
		updateNotificationHandler_Errors();
		if (get_sensor_data_read_flag() == SENSOR_DATA_NOT_READ_VALUE) {
			ERROR_PRINT(
				"Sensor flag was not cleared. Value was ",
				get_sensor_data_read_flag()
			);
		}
	}
	DEBUG_PRINTF_POS(
		4, "Station is unlocked: %d\n", get_sensorstation_locked_status()
	);
}

unsigned long handleNotificationIfPresent(bool notificationPresent) {
	DEBUG_PRINT_POS(4, "\n");
	unsigned long startNotificationCheck = millis();
	if (notificationPresent) {
		DEBUG_PRINT_POS(3, "Notifcation is present\n");
		int32_t timeTillNext = notificationHandler->update();

		while (timeTillNext > 0 && (unsigned long) timeTillNext <
									   TIME_CHECK_BLE_CENTRAL_PRESENT_MS -
										   (millis() - startNotificationCheck)
		) {
			DEBUG_PRINTF_POS(2, "Will wait for %ld ms\n", timeTillNext);
			delay(timeTillNext);
			timeTillNext = notificationHandler->update();
		}
	}
	unsigned long passedTime = millis() - startNotificationCheck;
	if (passedTime < TIME_CHECK_BLE_CENTRAL_PRESENT_MS) {
		return TIME_CHECK_BLE_CENTRAL_PRESENT_MS - passedTime;
	}
	return 0;
}

/**
 * @returns True if there is a notification in the queue, else false.
 */
bool updateNotificationHandler_Errors() {
	DEBUG_PRINT_POS(4, "\n");
	notificationHandler->updateAirHumidityValid(get_air_humidity_valid());
	notificationHandler->updateAirPressureValid(get_air_pressure_valid());
	notificationHandler->updateAirTemperatureValid(get_temperature_valid());
	notificationHandler->updateAirQualityValid(get_air_quality_valid());
	notificationHandler->updateSoilHumidityValid(get_soil_humidity_valid());
	notificationHandler->updateLightIntensityValid(get_light_intensity_valid());
	return !notificationHandler->isEmpty();
}

bool updateNotificationHandler_PairingMode(bool active) {
	DEBUG_PRINTF_POS(4, "Update notification with value %d\n", active);
	notificationHandler->updatePairingNotification(active);
	return !notificationHandler->isEmpty();
}

void setArduinoPowerStatus() {
	DEBUG_PRINT_POS(4, "\n");
	set_battery_level_status(
		BATTERY_LEVEL_FLAGS_FIELD, BATTERY_POWER_STATE_FIELD, 100
	);
}

#endif

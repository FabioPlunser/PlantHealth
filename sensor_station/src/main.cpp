
#include <Defines.h>

#ifdef DO_MAIN

#include "../lib/NotificationHandler/SensorErrors.h"
#include "FlashStorage.hpp"
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
#include <tuple>
#include <vector>

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
void checkResetButtonPressed();
void playPairingMelody();
arduino::String readPairedDeviceFromFlash();
void writePairedDeviceToFlash(arduino::String & pairedDevice);

// ----- Global Variables ----- //
// Sensor classes for the sensor value handler
AirSensorClass * airSensor;
HydrometerClass * hydrometer;
PhototransistorClass * phototransistor;
// Class to read the dip switch id.
DipSwitchClass * dipSwitch;

NotificationHandler * notificationHandler;
// BME680 sensor class from the Adafruit library to read temperature, humidity,
// pressure and gas resistance.
Adafruit_BME680 bme680;
// Sensor value handler to accumulate the sensor values and send them to the
// central device.
SensorValueHandlerClass * sensorValueHandler;
// Class to store the paired device in the flash memory.
FlashStorage * flashStorage;

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
	flashStorage = FlashStorage::getInstance();

	// Set the function to calculate the weight of the sensor values.
	// Required because the time intervall between measures is not constant.
	sensorValueHandler->setWeightCalculatorFunction(
		sensorValueWeightCalculationFunction
	);

	// Setup BLE communication.
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
	// ----- Variables ----- //
	// Read the previously paired device from the flash memory, if one was
	// paired before.
	static arduino::String pairedDevice			  = readPairedDeviceFromFlash();
	// If the pairing button is pressed, the sensor station will go into pairing
	// mode.
	static bool inPairingMode					  = false;
	// Time between sensor measurements.
	static unsigned long timeBetweenMeasures	  = 0;
	// Storing a timestamp of the last data transmission to calculate the time
	// till next sensor measure.
	static unsigned long previousDataTransmission = millis();
	// Storing a timestamp of the last sensor measurement to determin if the
	// next sensor measurement should be done.
	static unsigned long previousSensorMeasurement = millis();
	// Debug print the paired device only once.
	static bool firstRun						   = true;
	if (firstRun) {
		DEBUG_PRINTF(2, "Paired device is: %s\n", pairedDevice.c_str());
		firstRun = false;
	}

	// ----- Code ----- //

	checkResetButtonPressed();

	// If the pairing button is required and the pairing button is pressed, the
	// sensor station will go into pairing mode and signal it to the
	// notification handler. Else if the pairing button is not required, the
	// sensor station will always be in pairing mode without a notification.
#if PAIRING_BUTTON_REQUIRED
	checkPairingButtonAndStatus(inPairingMode);
	updateNotificationHandler_PairingMode(inPairingMode);
	enable_pairing_mode();
#else
	inPairingMode = true;
#endif
	// If the silence button is pressed the sensor station will not send any
	// notifications, till a timeout is reached.
	checkNotificationSilenceButtonPressed();
	// Will handle a central device if one is present and pair if in pairing
	// mode.
	handleCentralDeviceIfPresent(pairedDevice, inPairingMode);

	// Update notification handler once more to assert that the led will not
	// blink once more after the sensor station got paired.
#if PAIRING_BUTTON_REQUIRED
	updateNotificationHandler_PairingMode(inPairingMode);
#endif
	// If sensor data got transmitted we want to measure new values directly.
	if (get_sensor_data_read_flag() == SENSOR_DATA_READ_VALUE) {
		timeBetweenMeasures		 = 0;
		previousDataTransmission = millis();
	}
	// Print a debug message every 4 iterations to show that the sensor station
	// is in the pairing mode.
	static int i = 0;
	if (inPairingMode && i++ > 3) {
		DEBUG_PRINTLN(1, "Searching for central device!");
		i = 0;
	}
	// If a notification is present this function will return the time that is
	// to remain in sleep mode.
	unsigned long remainingSleepTime =
		handleNotificationIfPresent(!notificationHandler->isEmpty());
	// If the sensor station changes the locking state, all sensor data will
	// be deleted to get the current sensor data on the next unlock.
	static bool prevSensorStationLockedStatus =
		get_sensorstation_locked_status();
	if (prevSensorStationLockedStatus != get_sensorstation_locked_status()) {
		sensorValueHandler->resetAccumulators();
		prevSensorStationLockedStatus = get_sensorstation_locked_status();
	}
	// If the time between sensor measurements passed the next measurement will
	// done, but only if the sensor station is unlocked.
	if (get_sensorstation_locked_status() == SENSOR_STATION_UNLOCKED_VALUE &&
		millis() - previousSensorMeasurement > timeBetweenMeasures) {
		// Measure time of reading sensor values to correct the remaining sleep
		// time.
		unsigned long sensorReadStart = millis();
		// Calculate time to wait after this sensor measurement till the next
		// one.
		timeBetweenMeasures			  = calculateTimeBetweenMeasures(
			  sensorReadStart, previousDataTransmission,
			  TIME_BETWEEN_SENSOR_MEASUREMENTS_MIN_S * 1000,
			  TIME_BETWEEN_SENSOR_MEASUREMENTS_MAX_S * 1000,
			  TIME_IT_TAKES_TO_REACH_MAX_MEASUREMENT * 1000
		  );
		DEBUG_PRINTF(3, "Time between measures: %lu\n", timeBetweenMeasures);
		// Read sensor values and add them to the accumulator.
		sensorValueHandler->addSensorValuesToAccumulator();
		// Clear the flag in BLE to indicate the central device that new sensor
		// values
		clear_sensor_data_read_flag();
		previousSensorMeasurement = millis();
		// Substract time it took to measur sensor values from remaining sleep
		// time
		unsigned long passedTime  = millis() - sensorReadStart;
		// Safe substract to prevent underflow
		remainingSleepTime		  = remainingSleepTime < passedTime
										? 0
										: remainingSleepTime - passedTime;
	}
	delay(remainingSleepTime);
}

// ----- Functions ----- //

/**
 * Reads the previous paired device from the flash storage.
 * If no device was paired previously it will return an empty string.
 */
arduino::String readPairedDeviceFromFlash() {
	arduino::String pairedDevice = flashStorage->readPairedDevice();
	// Check if it matches the format "AB:CD:EF:12:34:56". If not it will count
	// as not set.
	DEBUG_PRINTF(2, "Read paired device: \"%s\"\n", pairedDevice.c_str());
	if (pairedDevice.length() != 17) {
		DEBUG_PRINTF_POS(
			1, "Stored string was not a valied Mac address! \"%s\"\n",
			pairedDevice.c_str()
		);
		pairedDevice = "";
	}
	DEBUG_PRINTF(2, "Will return pairedDevice \"%s\" \n", pairedDevice.c_str());
	return pairedDevice;
}

/**
 * Writes the paired device to the flash storage.
 * Asserts that the provided string matches the format "AB:CD:EF:12:34:56".
 */
void writePairedDeviceToFlash(arduino::String & pairedDevice) {
	// Check if it matches the format "AB:CD:EF:12:34:56". If not it will count
	// as not set.
	if (pairedDevice.length() != 17) {
		DEBUG_PRINTF_POS(
			1,
			"String \"%s\" did not match the format \"AB:CD:EF:12:34:56\"!\n",
			pairedDevice.c_str()
		);
		return;
	}
	DEBUG_PRINTF_POS(1, "Paired device set to \"%s\"\n", pairedDevice.c_str());
	flashStorage->writePairedDevice(pairedDevice);
}

/**
 * Will check if the reset button is pressed. If the button is pressed for
 * "TIME_BUTTON_PRESS_TO_RESET" milliseconds the system will be resetted.
 * The previous paired device will be deleted from the flash storage.
 */
void checkResetButtonPressed() {
	static unsigned long timePressedStart = 0;
	static bool isPressed				  = false;
	if (digitalRead(PIN_BUTTON_3) == PinStatus::HIGH) {
		DEBUG_PRINT_POS(2, "Reset button pressed!\n");
		// First button press will start the timer.
		if (!isPressed) {
			timePressedStart = millis();
			isPressed		 = true;
		} else {
			// If the button is pressed for more than
			// "TIME_BUTTON_PRESS_TO_RESET" the reset will be initialized.
			if (millis() - timePressedStart > TIME_BUTTON_PRESS_TO_RESET) {
				DEBUG_PRINT_POS(1, "\nReset initiated!\n\n");
				flashStorage->writePairedDevice("");
				NVIC_SystemReset();
			}
		}
	} else {
		isPressed = false;
	}
}

/**
 * Function to calculate the weight of the sensor value.
 * Will be provided to the sensor value handler.
 */
double sensorValueWeightCalculationFunction(
	unsigned long timeNow, unsigned long timeLastUpdate,
	unsigned long timeLastReset
) {
	DEBUG_PRINT_POS(4, "\n");
	return (double) (timeNow - timeLastUpdate);
}

/**
 * Function to calculate the time till the next sensor measurement depending on
 * the time since the first measurement.
 * Will move in form of a sine² wave from "timeMin" to "timeMax" in the time
 * between "totalTillMax".
 * @returns Time in milliseconds till the next sensor measurement.
 */
unsigned long calculateTimeBetweenMeasures(
	unsigned long now, unsigned long firstMeasure, unsigned long timeMin,
	unsigned long timeMax, unsigned long totalTillMax
) {
	float normedValue = float(now - firstMeasure) / totalTillMax;
	if (normedValue > 1) {
		return timeMax;
	}
	// Calculates a number between 0 and 1 that will be used to calculate the
	// time between measures.
	float waitFactor = pow((std::sin(PI / 2 * normedValue)), 2);
	DEBUG_PRINTF(3, "Wait factor: %f\n", waitFactor);
	DEBUG_PRINTF(
		3, "Time between measures: %f\n",
		((waitFactor * (timeMax - timeMin) + timeMin))
	);
	// Returns a time between "timeMin" and "timeMax" depending on the time
	// since the first measurement.
	return (unsigned long) ((waitFactor * (timeMax - timeMin) + timeMin));
}

/**
 * Checks if the silence button is pressed.
 * If pressed the notification handler will be informed to silence the
 * notifications for "TIME_IN_NOTIFICATION_SILENCE_MODE_MS".
 */
void checkNotificationSilenceButtonPressed() {
	DEBUG_PRINT_POS(4, "\n");
	if (digitalRead(PIN_BUTTON_2) == PinStatus::HIGH) {
		notificationHandler->silenceNotification(
			TIME_IN_NOTIFICATION_SILENCE_MODE_MS
		);
	}
}

/**
 * Checks if the pairing button is pressed.
 * If pressed the pairing mode will be enabled.
 * If the button is not pressed for "DURATION_IN_PAIRING_MODE_MS" time it will
 * got back to normal mode.
 */
void checkPairingButtonAndStatus(bool & inPairingMode) {
	DEBUG_PRINT_POS(4, "\n");
	static unsigned long pairingTime = 0;
	DEBUG_PRINTF_POS(
		3, "Checking pairing button and status. In pairing mode value is %d.\n",
		inPairingMode
	);
	// If the button is pressed the pairing mode will be enabled and a timer
	// will be started. The sensor station will be locked.
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

/**
 * Will set the sensor values in the BLE characteristic if the sensor station is
 * unlocked.
 * If set successfully the sensor data read flag will be cleared.
 */
void setValueInVerifiedCentralDevice(BLEDevice & central) {
	DEBUG_PRINT_POS(4, "\n");
	DEBUG_PRINT(1, "Central device is remembered device.\n");
	if (central.connected()) {
		DEBUG_PRINT(1, "Connected\n");
		// If the sensor station is unlocked the sensor values will be set in
		// the BLE characteristics. On success the sensor data read flag will be
		// cleared.
		if (get_sensorstation_locked_status() ==
			SENSOR_STATION_UNLOCKED_VALUE) {
			if (!sensorValueHandler->setAccumulatedSensorValuesInBle()) {
				DEBUG_PRINT_POS(1, "SensorValueHandler returned false\n");
			} else {
				clear_sensor_data_read_flag();
			}
			set_sensorstation_id(0);
			setArduinoPowerStatus();
		}
		// Will wait for the central devide to disconnect. If the central device
		// does not disconnect within "TIMEOUT_TIME_BLE_CONNECTION_MS" the
		// sensor station will disconnect the central device.
		unsigned long connectionStart = millis();
		while (central.connected() &&
			   TIMEOUT_TIME_BLE_CONNECTION_MS > millis() - connectionStart) {
		}
	}
}

/**
 * Will handle the central device if present.
 * If the central device is the remembered device it will set the sensor values
 * in the BLE characteristic.
 * If the central device is not the remembered device and the sensor station is
 * in the pairing mode, it will do nothing until the sensor station gets
 * unlocked. If unlocked it will set the central device as the remembered
 * device.
 */
void handleCentralDeviceIfPresent(
	arduino::String & pairedDevice, bool & inPairingMode
) {
	DEBUG_PRINT_POS(4, "\n");
	BLEDevice central = BLE.central();
	// If the central device is present
	if (central) {
		// Set dip switch id immediately to notify the central device which
		// sensor station it is.
		uint8_t dipSwitchId = dipSwitch->getdipSwitchValue();
		set_dip_switch_id(dipSwitchId);
		// If it is in the pairing mode it will wait for a central device to
		// unlock it.
		if (inPairingMode) {
			DEBUG_PRINTF(
				1,
				"New central trying to pair. Device is \"%s\" Current locking "
				"status = %d.\n",
				central.address().c_str(), get_sensorstation_locked_status()
			);
			// Wait for the central device to unlock the sensor station or
			// disconnect.
			while (central.connected() && get_sensorstation_locked_status() !=
											  SENSOR_STATION_UNLOCKED_VALUE) {
				delay(10);
			}
			// If the central device unlocked the sensor station it will set the
			// central device as the remembered device.
			if (get_sensorstation_locked_status() ==
				SENSOR_STATION_UNLOCKED_VALUE) {
				pairedDevice = central.address();
				writePairedDeviceToFlash(pairedDevice);
				DEBUG_PRINTF(
					1, "New device is: \"%s\".\n", pairedDevice.c_str()
				);
				playPairingMelody();
				inPairingMode = false;
			} else {
				DEBUG_PRINT(
					1, "Did not set unlocked bit. Initiating disconnect.\n"
				);
				central.disconnect();
				return;
			}
		}
		DEBUG_PRINTF(1, "Connected to %s.\n", central.address().c_str());
		// If the central device is the remembered device it will set the sensor
		// values in the BLE characteristics.
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
		// After setting all the sensor values in the BLE characteristics the
		// sensor station will check all the sensor error characteristics and
		// set the error bit if an error occurred.
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

/**
 * Will handle any notification if present.
 * If a notification is present it will call the update function of the
 * notification handler and loop till the remaining time is less than the time
 * to check for a central device or no notification is present anymore.
 * If no notification is present it will return 0.
 * @return: The remaining time to sleep till the next check for a central device
 */
unsigned long handleNotificationIfPresent(bool notificationPresent) {
	DEBUG_PRINT_POS(4, "\n");
	unsigned long startNotificationCheck = millis();
	if (notificationPresent) {
		DEBUG_PRINT_POS(3, "Notifcation is present\n");
		int32_t timeTillNext = notificationHandler->update();
		// Will loop till the remaining time is less than the time to check for
		// a central device or no notification is present anymore.
		while (timeTillNext > 0 && (unsigned long) timeTillNext <
									   TIME_CHECK_BLE_CENTRAL_PRESENT_MS -
										   (millis() - startNotificationCheck)
		) {
			DEBUG_PRINTF_POS(3, "Will wait for %ld ms\n", timeTillNext);
			delay(timeTillNext);
			timeTillNext = notificationHandler->update();
		}
	}
	unsigned long passedTime = millis() - startNotificationCheck;
	// Will return the remaining time to sleep till the next check for a central
	// device, or 0 if the remaining time is less than 0.
	if (passedTime < TIME_CHECK_BLE_CENTRAL_PRESENT_MS) {
		return TIME_CHECK_BLE_CENTRAL_PRESENT_MS - passedTime;
	}
	return 0;
}

/**
 * Will check the BLE functions if a sensor value was out of range and set them
 * accordingly in the notification handler.
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

/**
 * Update the notification handler with the current pairing mode status.
 * @returns True if there is a notification in the queue, else false.
 */
bool updateNotificationHandler_PairingMode(bool active) {
	DEBUG_PRINTF_POS(4, "Update notification with value %d\n", active);
	notificationHandler->updatePairingNotification(active);
	return !notificationHandler->isEmpty();
}

/**
 * Function to update the current power status of the arduino.
 * For not set from the define values BATTERY_LEVEL_FLAGS_FIELD and
 * BATTERY_POWER_STATE_FIELD defined in the header file.
 */
void setArduinoPowerStatus() {
	DEBUG_PRINT_POS(4, "\n");
	set_battery_level_status(
		BATTERY_LEVEL_FLAGS_FIELD, BATTERY_POWER_STATE_FIELD, 100
	);
}

/**
 * Calls the play melody function of the notification handler with the
 * MELODY_PAIRING_SUCCESSFUL defined in the header file.
 */
void playPairingMelody() {
	DEBUG_PRINT_POS(4, "\n");

	uint16_t melody[][2] = MELODY_PAIRING_SUCCESSFUL;
	uint8_t melodyLength = sizeof(melody) / sizeof(uint16_t *);
	std::vector<std::tuple<uint16_t, uint16_t>> melodyVector;
	for (uint8_t i = 0; i < melodyLength; i++) {
		melodyVector.push_back(std::make_tuple(melody[i][0], melody[i][1]));
	}

	notificationHandler->playMelodyOnPiezoBuzzer(melodyVector);
}

#endif

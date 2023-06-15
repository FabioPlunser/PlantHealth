#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include "../../include/Defines.h"
#include "LedController.hpp"
#include "NotificationQueue.hpp"
#include "PiezoBuzzerController.hpp"
#include "SensorError.hpp"
#include "SensorErrors.h"

#include <tuple>
#include <vector>

/**
 * Check castability by checking if the notification type is of the type Sensor
 * Error.
 */
#define CHECK_IF_CASTABLE_TO_SENSOR_ERROR(notification)             \
	(notification != NULL && notification->getNotificationType() == \
								 Notification::NotificationType::SENSOR_ERROR)

/**
 * Casts the notification to a sensor error and store it in the given variable.
 * This macro should only be used if the notification is checked with
 * CHECK_IF_CASTABLE_TO_SENSOR_ERROR before.
 */
#define CAST_NOTIFICATION_TO_SENSOR_ERROR(notification, varName) \
	assert(CHECK_IF_CASTABLE_TO_SENSOR_ERROR(notification));     \
	varName = static_cast<const SensorError *>(notification);

/**
 * Check if a value for a sensor error is valid. Valid values are
 * ERROR_VALUE_NOTHING, ERROR_VALUE_LOW and ERROR_VALUE_HIGH.
 */
#define CHECK_VALID_VALUE_VALID(value)                              \
	if (value != ERROR_VALUE_NOTHING && value != ERROR_VALUE_LOW && \
		value != ERROR_VALUE_HIGH) {                                \
		ERROR_PRINT("Value was out of range. Value was ", value);   \
		return;                                                     \
	}

/**
 * Helper macro to decompose a hex value to its rgb values.
 */
#define DECOMPOSE_HEX_RGB_R(hexval) ((hexval >> 16) & 0xFF)
#define DECOMPOSE_HEX_RGB_G(hexval) ((hexval >> 8) & 0xFF)
#define DECOMPOSE_HEX_RGB_B(hexval) (hexval & 0xFF)

class NotificationHandler {
	private:
		NotificationQueue * notificationQueue;
		LedHandler * ledConstroller;
		PiezoBuzzerController * piezoBuzzerController;
		const Notification * prevErrorNotification = NULL;
		bool inSilentMode						   = false;
		unsigned long timeOfSilenceEnd			   = 0;

		NotificationHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			this->notificationQueue = NotificationQueue::getNotificationQueue();
			this->ledConstroller =
				LedHandler::getLedHandler(ledPinRed, ledPinGreen, ledPinBlue);
			this->piezoBuzzerController =
				PiezoBuzzerController::getInstance(PIN_PIEZO_BUZZER);
		}

	public:
		NotificationHandler & operator=(NotificationHandler &) = delete;
		NotificationHandler(NotificationHandler &)			   = delete;

		/**
		 * Get singelton instance of the notification handler.
		 */
		static NotificationHandler * getInstance(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			DEBUG_PRINT_POS(4, "\n");
			static NotificationHandler notificationHandler(
				ledPinRed, ledPinGreen, ledPinBlue
			);
			return &notificationHandler;
		}

	private:
		/**
		 * Add a sensor Error to the notification queue.
		 * Gets the priority of the error from the error type and status from
		 * the defines header.
		 * @param errorType The type of the error.
		 * @param errorStatus The status of the error. (High or low)
		 */
		void addSensorError(
			SensorErrors::Type errorType, SensorErrors::Status errorStatus
		) {
			DEBUG_PRINT_POS(4, "\n");
			int priority = 0;
			switch (errorType) {
				case SensorErrors::Type::AirHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_HUMIDITY_TO_HIGH_PRIORITY
								   : AIR_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirPressureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_PRESSURE_TO_HIGH_PRIORITY
								   : AIR_PRESSURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirQualityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_QUALITY_TO_HIGH_PRIORITY
								   : AIR_QUALITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirTemperatureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_TEMPERATURE_TO_HIGH_PRIORITY
								   : AIR_TEMPERATURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::SoilHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? SOIL_HUMIDITY_TO_HIGH_PRIORITY
								   : SOIL_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::LightIntensityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? LUMINOSITY_TO_HIGH_PRIORITY
								   : LUMINOSITY_TO_LOW_PRIORITY;
					break;
				default:
					break;
			}
			SensorError error(errorType, errorStatus, priority);
			notificationQueue->addError(error);
		}

		/**
		 * Function to set the colors and time arrays in the led controller form
		 * a notification.
		 * @return The time in ms till the next required execution to
		 * update LED.
		 */
		int16_t setLEDfromNotification(const Notification & notification) {
			DEBUG_PRINT_POS(4, "\n");
			// Only change led status if the notification is not the same as the
			// previous one.
			if (prevErrorNotification != NULL &&
				*prevErrorNotification == notification) {
				DEBUG_PRINT_POS(
					3, "Notification was the same as the previous one.\n"
				)
				return ledConstroller->updateLEDStatus();
			}
			DEBUG_PRINT_POS(3, "New notification set.")
			uint16_t ledOnMs[] = {
				LED_TIME_NOTIFICATION_ON_MS, LED_TIME_NOTIFICATION_ON_MS};
			uint16_t ledOffMs[] = {
				LED_TIME_NOTIFICATION_BLINK_PAUSE_MS,
				LED_TIME_NOTIFICATION_OFF_MS};
			uint8_t arraySize = sizeof(ledOnMs) / sizeof(ledOnMs[0]);
			// Currently only the pairing mode is a pure notification.
			uint8_t colorR =
				DECOMPOSE_HEX_RGB_R(LED_COLOR_NOTIFICATION_PAIRING);
			uint8_t colorG =
				DECOMPOSE_HEX_RGB_G(LED_COLOR_NOTIFICATION_PAIRING);
			uint8_t colorB =
				DECOMPOSE_HEX_RGB_B(LED_COLOR_NOTIFICATION_PAIRING);
			bool loop = true;
			ledConstroller->setErrorProperties(
				colorR, colorG, colorB, ledOnMs, ledOffMs, arraySize, loop
			);
			if (prevErrorNotification != NULL) {
				delete (prevErrorNotification);
				prevErrorNotification = NULL;
			}
			prevErrorNotification = new Notification(notification);
			return ledConstroller->updateLEDStatus();
		}

		/**
		 * Will set the corresponding color and tim intervall for the provided
		 * sensorError.
		 * @return The time in ms till the next required execution to
		 * update LED.
		 */
		int16_t setLEDfromSensorError(const SensorError & sensorError) {
			DEBUG_PRINT_POS(4, "\n");
			// Only change led status if the notification is not the same as the
			// previous one. Therefore try to cast the previous notification to
			// a sensor error. If not possible, the notification is not a sensor
			// error and therefore a new one. Else they get compared.
			if (CHECK_IF_CASTABLE_TO_SENSOR_ERROR(prevErrorNotification)) {
				const SensorError * prevSensorError;
				CAST_NOTIFICATION_TO_SENSOR_ERROR(
					prevErrorNotification, prevSensorError
				);
				// If the previous sensor error is the same as the current one
				// the time till the next update is returned.
				if (prevSensorError != NULL &&
					*prevSensorError == sensorError) {
					DEBUG_PRINT_POS(3, "Led got updated without changes\n");
					return ledConstroller->updateLEDStatus();
				}
			}
			uint32_t colorCode = 0;
			bool isHigh =
				sensorError.getErrorStatus() == SensorErrors::Status::High;
			// Set the color code depending on the error type.
			switch (sensorError.getErrorType()) {
				case SensorErrors::Type::AirHumidityError:
					colorCode = LED_COLOR_ERROR_AIR_HUMIDITY;
					break;
				case SensorErrors::Type::AirPressureError:
					colorCode = LED_COLOR_ERROR_AIR_PRESSURE;
					break;
				case SensorErrors::Type::AirQualityError:
					colorCode = LED_COLOR_ERROR_AIR_QUALITY;
					break;
				case SensorErrors::Type::AirTemperatureError:
					colorCode = LED_COLOR_ERROR_AIR_TEMPERATURE;
					break;
				case SensorErrors::Type::SoilHumidityError:
					colorCode = LED_COLOR_ERROR_SOIL_HUMIDITY;
					break;
				case SensorErrors::Type::LightIntensityError:
					colorCode = LED_COLOR_ERROR_LIGHT_INTENSITY;
					break;
				default:
					break;
			}

			DEBUG_PRINTF_POS(
				3, "LED will be set with color code %p\n", (void *) colorCode
			);

			std::vector<uint16_t> ledOnMs;
			std::vector<uint16_t> ledOffMs;
			// If the value is high the LED will blink twice. Otherwise just
			// once.
			if (isHigh) {
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_BLINK_PAUSE_MS);
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_OFF_MS);
			} else {
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_OFF_MS);
			}
			DEBUG_PRINT_POS(3, "Times contained in vector:\n");
			for (unsigned int i = 0; i < ledOnMs.size(); i++) {
				DEBUG_PRINTF_POS(
					3, "On %u = %u, Off = %u\n", i, ledOnMs.at(i),
					ledOffMs.at(i)
				);
			}
			// Decompose the color code into the corresponding RGB values.
			uint8_t colorR = DECOMPOSE_HEX_RGB_R(colorCode);
			uint8_t colorG = DECOMPOSE_HEX_RGB_G(colorCode);
			uint8_t colorB = DECOMPOSE_HEX_RGB_B(colorCode);
			bool loop	   = true;
			ledConstroller->setErrorProperties(
				colorR, colorG, colorB, ledOnMs.data(), ledOffMs.data(),
				ledOnMs.size(), loop
			);
			DEBUG_PRINTF_POS(
				3, "Led controller got set. Is high was %d.\n", isHigh
			);
			// If the previous error notification not Null we delete it.
			if (prevErrorNotification != NULL) {
				delete (prevErrorNotification);
				prevErrorNotification = NULL;
			}
			// Create a copy of the sensor error and save it as the previous.
			prevErrorNotification = new SensorError(sensorError);
			// Update the LED status and return the time till the next update.
			return ledConstroller->updateLEDStatus();
		}

	public:
		/**
		 * @returns if there are any notifications in the queue.
		 */
		bool isEmpty() {
			DEBUG_PRINT_POS(4, "\n");
			return this->notificationQueue->isEmpty();
		}

		/**
		 * This function silences the led and piezo puzzer for the provided time
		 * or until the top priority error changes.
		 * @param timeToSilenceMs: Time in ms how long the error should be
		 * silenced for on button press.
		 */
		void silenceNotification(unsigned long timeToSilenceMs) {
			DEBUG_PRINT_POS(4, "\n");
			this->inSilentMode	   = true;
			this->timeOfSilenceEnd = millis() + timeToSilenceMs;
			update();
		}

		/**
		 * @return -1 if no errors are present.\
		 * @return Otherwise the time till the next required call to update
		 * in ms
		 */
		int32_t update() {
			DEBUG_PRINT_POS(4, "\n");
			// Timetamp of the last tone played.
			static unsigned long previousTone = 0;
			DEBUG_PRINTF(
				3, "Currently there are %u notifications.\n",
				notificationQueue->getSize()
			);
			if (notificationQueue->isEmpty()) {
				DEBUG_PRINT_POS(3, "Queue is empty.\n");
				ledConstroller->disable();
				return -1;
			}
#if ALWAYS_IN_SILENT_MODE
			this->inSilentMode	   = true;
			this->timeOfSilenceEnd = millis() + 100'000;
#endif
			// If in silent mode we check if the time of silence has passed.
			if (this->inSilentMode) {
				DEBUG_PRINT_POS(3, "In silent mode.\n");
				if ((int32_t) this->timeOfSilenceEnd - (int32_t) millis() < 0) {
					DEBUG_PRINT_POS(3, "Silent mode ended.\n");
					this->inSilentMode = false;
					return ledConstroller->updateLEDStatus();
				} else {
					// Set the LED to the silent mode in case it is the first
					// call.
					ledConstroller->silence();
					return this->timeOfSilenceEnd - millis();
				}
			} else {
				const Notification * topNotification =
					notificationQueue->getPrioritisedNotification();
				// Only play a tone if the the top notification is a error ant
				// enough time has passed since the last tone.
				// (Only errors to exclude the pairing notification)
				if (millis() - previousTone > PIEZO_BUZZER_TONE_INTERVALL_MS &&
					topNotification->getNotificationType() ==
						Notification::NotificationType::SENSOR_ERROR) {
					DEBUG_PRINT_POS(3, "Buzzer tone.\n");
					previousTone = millis();
					piezoBuzzerController->startBuzzer(
						PIEZO_BUZZER_TONE_FREQUENCY_HZ,
						PIEZO_BUZZER_TONE_DURATION_MS
					);
				}
				// Time to wait till the next update.
				int16_t timeToWait;
				// Provide current top notification to the LED controller in
				// case it did change.
				if (CHECK_IF_CASTABLE_TO_SENSOR_ERROR(topNotification)) {
					const SensorError * error;
					CAST_NOTIFICATION_TO_SENSOR_ERROR(topNotification, error);
					timeToWait = setLEDfromSensorError(*error);
					DEBUG_PRINTF_POS(
						3, "Found error with time %d.\n", timeToWait
					);
				} else {
					timeToWait = setLEDfromNotification(*topNotification);
					DEBUG_PRINTF_POS(
						3, "Found notification with time %d.\n", timeToWait
					);
				}
				return timeToWait;
			}
		}

		/**
		 * This function udates the queue with the current pairing state.
		 * If the value did change and in silent mode the silent mode will end.
		 */
		void updatePairingNotification(bool isActive) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static bool prevValue = false;
			// Return if the value did not change.
			if (isActive == prevValue) {
				DEBUG_PRINTF_POS(
					3, "Is active with same value as previous. Value was %d\n",
					isActive
				);
				return;
			}
			this->inSilentMode = false;
			Notification notification(NOTIFICATION_PAIRING_MODE_PRIORITY);
			// If set to active add the notification to the queue. Otherwise
			// delete it from the queue.
			if (isActive) {
				DEBUG_PRINT_POS(3, "Error gets added to queue.\n");
				notificationQueue->addError(notification);
			} else {
				DEBUG_PRINT_POS(3, "Error gets removed from queue.\n");
				notificationQueue->deleteErrorFromQueue(notification);
				update();
			}
			prevValue = isActive;
		}

		/**
		 * This function updates the queue with the current SoilHumidityValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateSoilHumidityValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::SoilHumidityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::SoilHumidityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		/**
		 * This function updates the queue with the current AirHumidityValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateAirHumidityValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirHumidityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::AirHumidityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		/**
		 * This function updates the queue with the current AirPressureValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateAirPressureValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirPressureError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::AirPressureError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		/**
		 * This function updates the queue with the current AirTemperatureValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateAirTemperatureValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirTemperatureError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::AirTemperatureError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		/**
		 * This function updates the queue with the current AirQualityValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateAirQualityValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirQualityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::AirQualityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		/**
		 * This function updates the queue with the current LightIntensityValid
		 * value.
		 * @param value is only allowed if it is either ERROR_VALUE_NOTHING,
		 * -LOW or -HIGH.
		 */
		void updateLightIntensityValid(uint8_t value) {
			DEBUG_PRINT_POS(4, "\n");
			// Store previous value to only update the queue if something
			// changed.
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			// Value is only allowed if it is either ERROR_VALUE_NOTHING, -LOW
			// or -HIGH. Otherwise return
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			this->inSilentMode = false;
			// If previous value was different from -NOTHING it will remove the
			// previous error.
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::LightIntensityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				update();
				return;
			}
			// Add sensor Error by type and witht he priority dependent on the
			// provided value.
			addSensorError(
				SensorErrors::Type::LightIntensityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void playMelodyOnPiezoBuzzer(
			std::vector<std::tuple<uint16_t, uint16_t>> noteAndDurationList
		) {
			DEBUG_PRINT_POS(4, "\n");
			piezoBuzzerController->playMelody(noteAndDurationList);
		}
};

#endif
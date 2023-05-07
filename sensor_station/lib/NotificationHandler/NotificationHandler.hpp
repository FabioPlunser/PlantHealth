#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include "../../include/Defines.h"
#include "LedController.hpp"
#include "NotificationQueue.hpp"
#include "PiezoBuzzerController.hpp"
#include "SensorError.hpp"
#include "SensorErrors.h"

#define CHECK_IF_CASTABLE_TO_SENSOR_ERROR(notification) \
	(notification->getNotificationType() ==             \
	 Notification::NotificationType::SENSOR_ERROR)

#define CAST_NOTIFICATION_TO_SENSOR_ERROR(notification, varName) \
	assert(CHECK_IF_CASTABLE_TO_SENSOR_ERROR(notification));     \
	varName = static_cast<const SensorError *>(notification);

#define CHECK_VALID_VALUE_VALID(value)                              \
	if (value != ERROR_VALUE_NOTHING && value != ERROR_VALUE_LOW && \
		value != ERROR_VALUE_HIGH) {                                \
		ERROR_PRINT("Value was out of range. Value was ", value);   \
		return;                                                     \
	}

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

		static NotificationHandler * getInstance(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			static NotificationHandler notificationHandler(
				ledPinRed, ledPinGreen, ledPinBlue
			);
			return &notificationHandler;
		}

	private:
		void addSensorError(
			SensorErrors::Type errorType, SensorErrors::Status errorStatus
		) {
			int priority = 0;
			switch (errorType) {
				case SensorErrors::Type::AirHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_HUMIDITY_TO_HIGH_PRIORITY
								   : AIR_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirPressureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_TEMPERATURE_TO_HIGH_PRIORITY
								   : AIR_TEMPERATURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirQualityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_PRESSURE_TO_HIGH_PRIORITY
								   : AIR_PRESSURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirTemperatureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_QUALITY_TO_HIGH_PRIORITY
								   : AIR_QUALITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::SoilHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? SOIL_HUMIDITY_TO_HIGH_PRIORITY
								   : SOIL_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::LightIntensityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_HUMIDITY_TO_HIGH_PRIORITY
								   : AIR_HUMIDITY_TO_LOW_PRIORITY;
					break;
				default:
					break;
			}
			SensorError error(errorType, errorStatus, priority);
			notificationQueue->addError(error);
		}

		int16_t setLEDfromNotification(const Notification & notification) {
			if (prevErrorNotification == &notification) {
				return ledConstroller->updateLEDStatus();
			}
			uint16_t ledOnMs[]	= {LED_TIME_NOTIFICATION_ON_MS};
			uint16_t ledOffMs[] = {LED_TIME_NOTIFICATION_OFF_MS};
			uint8_t arraySize	= sizeof(ledOnMs) / sizeof(ledOnMs[0]);
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
			return ledConstroller->updateLEDStatus();
		}

		/**
		 * Will set the corresponding color and tim intervall for the provided
		 * sensorError.
		 * @return The time in ms till the next required execution to
		 * update LED.
		 */
		int16_t setLEDfromSensorError(const SensorError & sensorError) {
			if (prevErrorNotification == &sensorError) {
				return ledConstroller->updateLEDStatus();
			}
			uint32_t colorCode = 0;
			bool isHigh =
				sensorError.getErrorStatus() == SensorErrors::Status::High;

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

			std::vector<uint16_t> ledOnMs;
			std::vector<uint16_t> ledOffMs;
			if (isHigh) {
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_BLINK_PAUSE_MS);
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_OFF_MS);
			} else {
				ledOnMs.push_back(LED_TIME_ERROR_ON_MS);
				ledOffMs.push_back(LED_TIME_ERROR_OFF_MS);
			}
			uint8_t colorR = DECOMPOSE_HEX_RGB_R(colorCode);
			uint8_t colorG = DECOMPOSE_HEX_RGB_G(colorCode);
			uint8_t colorB = DECOMPOSE_HEX_RGB_B(colorCode);
			bool loop	   = true;
			ledConstroller->setErrorProperties(
				colorR, colorG, colorB, ledOnMs.data(), ledOffMs.data(),
				ledOnMs.size(), loop
			);
			return ledConstroller->updateLEDStatus();
		}

	public:
		bool isEmpty() { return this->notificationQueue->isEmpty(); }

		/**
		 * This function silences the led and piezo puzzer for the provided time
		 * or until the top priority error changes.
		 * @param timeToSilenceMs: Time in ms how long the error should be
		 * silenced for on button press.
		 */
		void silenceNotification(unsigned long timeToSilenceMs) {
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
			static unsigned long previousTone = 0;
			if (notificationQueue->isEmpty()) {
				ledConstroller->disable();
				return -1;
			}
			if (this->inSilentMode) {
				if ((int32_t) millis() - (int32_t) this->timeOfSilenceEnd < 0) {
					this->inSilentMode = false;
					return ledConstroller->updateLEDStatus();
				} else {
					ledConstroller->silence();
					return this->timeOfSilenceEnd - millis();
				}
			} else {
				if (millis() - previousTone > PIEZO_BUZZET_TONE_INTERVALL_MS) {
					previousTone = millis();
					piezoBuzzerController->startBuzzer(
						PIEZO_BUZZET_TONE_FREQUENCY_HZ,
						PIEZO_BUZZET_TONE_DURATION_MS
					);
				}
				const Notification * topNotification =
					notificationQueue->getPrioritisedNotification();
				if (CHECK_IF_CASTABLE_TO_SENSOR_ERROR(topNotification)) {
					const SensorError * error;
					CAST_NOTIFICATION_TO_SENSOR_ERROR(topNotification, error);
					return setLEDfromSensorError(*error);
				} else {
					return setLEDfromNotification(*topNotification);
				}
			}
		}

		void updatePairingNotification(bool isActive) {
			static bool prevValue = false;
			if (isActive == prevValue) {
				DEBUG_PRINTF_POS(
					3, "Is active with same value as previous. Value was %d\n",
					isActive
				);
				return;
			}
			Notification notification(NOTIFICATION_PAIRING_MODE_PRIORITY);
			if (isActive) {
				DEBUG_PRINT_POS(3, "Error gets added to queue.\n");
				notificationQueue->addError(notification);
			} else {
				DEBUG_PRINT_POS(3, "Error gets removed from queue.\n");
				notificationQueue->deleteErrorFromQueue(notification);
			}
			prevValue = isActive;
		}

		void updateSoilHumidityValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::SoilHumidityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::SoilHumidityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void updateAirHumidityValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirHumidityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::AirHumidityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void updateAirPressureValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirPressureError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::AirPressureError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void updateAirTemperatureValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirTemperatureError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::AirTemperatureError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void updateAirQualityValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::AirQualityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::AirQualityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}

		void updateLightIntensityValid(uint8_t value) {
			static uint8_t prevValue = ERROR_VALUE_NOTHING;
			CHECK_VALID_VALUE_VALID(value);
			if (value == prevValue) {
				return;
			}
			if (prevValue != ERROR_VALUE_NOTHING) {
				notificationQueue->deleteErrorFromQueue(
					SensorErrors::Type::LightIntensityError
				);
			}
			prevValue = value;
			if (value == ERROR_VALUE_NOTHING) {
				return;
			}
			addSensorError(
				SensorErrors::Type::LightIntensityError,
				value == ERROR_VALUE_HIGH ? SensorErrors::Status::High
										  : SensorErrors::Status::Low
			);
		}
};

#endif
#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include "../../include/Defines.h"
#include "LedController.cpp"
#include "NotificationQueue.cpp"
#include "SensorError.cpp"
#include "SensorErrors.h"

#define CAST_NOTIFICATION_TO_SENSOR_ERROR(notification, varName) \
	assert(                                                      \
		notification->getNotificationType() ==                   \
		Notification::NotificationType::ERROR                    \
	);                                                           \
	varName = static_cast<const SensorError *>(notification);

#define CHECK_VALID_VALUE_VALID(value)                              \
	if (value != ERROR_VALUE_NOTHING && value != ERROR_VALUE_LOW && \
		value != ERROR_VALUE_HIGH) {                                \
		ERROR_PRINT("Value was out of range. Value was ", value);   \
		return;                                                     \
	}

class NotificationHandler {
	private:
		NotificationQueue * notificationQueue;
		LedHandler * ledConstroller;

		NotificationHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			notificationQueue = NotificationQueue::getNotificationQueue();
			ledConstroller =
				LedHandler::getLedHandler(ledPinRed, ledPinGreen, ledPinBlue);
		}

		struct LedErrorParameter {
				uint16_t * ledOnMs;
				uint16_t * ledOffMs;
				uint8_t size;
				uint8_t rgbColorRed;
				uint8_t rgbColorGreen;
				uint8_t rgbColorBlue;
				bool loopError;
		} ledErrorParam = {0};

	public:
		static NotificationHandler & getErrorHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			static NotificationHandler errorHandler(
				ledPinRed, ledPinGreen, ledPinBlue
			);
			return errorHandler;
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

	public:
		void addNotification(Notification & notification) {
			notificationQueue->addError(notification);
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

		void updateAirqualityValid(uint8_t value) {
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
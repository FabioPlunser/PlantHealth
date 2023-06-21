#ifndef ERROR_CLASS
#define ERROR_CLASS

#include "Notification.hpp"
#include "SensorErrors.h"

#include <Arduino.h>

/**
 * A class for a sensor error. It will store the type and the status of the
 * error and derive the priority from the Notification class.
 */
class SensorError : public Notification {
	private:
		// Type for the sensor it belongs to and the status of the error to
		// indicate if it is to high or to low.
		SensorErrors::Type type;
		SensorErrors::Status status;

	public:
		/**
		 * Creates a new sensor error with the given type and status.
		 * @param type: The type of the sensor that caused the error.
		 * @param status: The status of the error. (To high or to low)
		 * @param priority: The priority of the error.
		 */
		SensorError(
			SensorErrors::Type type, SensorErrors::Status status,
			uint8_t priority
		)
			: Notification(priority) {
			this->notificationType = NotificationType::SENSOR_ERROR;
			this->type			   = type;
			this->status		   = status;
		}
		/**
		 * Returns what sensor the error belongs to.
		 */
		SensorErrors::Type getErrorType() const { return this->type; }
		/**
		 * Returns the status of the error. (To high or to low)
		 */
		SensorErrors::Status getErrorStatus() const { return this->status; }

		bool operator==(const SensorError & other) const {
			return Notification::operator==(other) &&
				   this->type == other.type && this->status == other.status;
		}

		bool operator<(const SensorError & other) const {
			return Notification::operator<(other);
		}

		SensorError & operator=(const SensorError & other) {
			if (this != &other) {
				Notification::operator=(other);
				this->status = other.status;
				this->type	 = other.type;
			}
			return *this;
		}
};

#endif
#ifndef ERROR_CLASS
#define ERROR_CLASS

#include "Notification.cpp"
#include "SensorErrors.h"

#include <cstdint>

class SensorError : public Notification {
	private:
		SensorErrors::Type type;
		SensorErrors::Status status;

	public:
		SensorError(
			SensorErrors::Type type, SensorErrors::Status status,
			uint8_t priority
		)
			: Notification(priority) {
			this->notificationType = NotificationType::ERROR;
			this->type			   = type;
			this->status		   = status;
		}

		SensorErrors::Type getErrorType() const { return this->type; }
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
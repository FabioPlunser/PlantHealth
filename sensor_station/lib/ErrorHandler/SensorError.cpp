#ifndef ERROR_CLASS
#define ERROR_CLASS

#include "SensorErrors.h"

#include <Arduino.h>

class SensorError {
	private:
		SensorErrors::Type type;
		SensorErrors::Status status;
		uint8_t priority;

	public:
		SensorError(
			SensorErrors::Type type, SensorErrors::Status status,
			uint8_t priority
		) {
			this->type	   = type;
			this->status   = status;
			this->priority = priority;
		}

		uint8_t getPriority() const { return this->priority; }

		SensorErrors::Type getErrorType() const { return this->type; }

		bool operator==(const SensorError & other) const {
			return this == &other && this->type == other.type &&
				   this->status == other.status &&
				   this->priority == other.priority;
		}

		bool operator<(const SensorError & other) const {
			if (this == &other) {
				return false;
			}
			return this->getPriority() < other.getPriority();
		}

		SensorError & operator=(const SensorError & other) {
			if (this != &other) {
				this->priority = other.priority;
				this->status   = other.status;
				this->type	   = other.type;
			}
			return *this;
		}
};

#endif
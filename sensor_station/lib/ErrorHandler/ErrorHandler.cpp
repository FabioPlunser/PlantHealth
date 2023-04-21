#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include <Arduino.h>
#include <list>
#include <queue>
#include <stdlib.h>

class SensorErrors {
	public:
		enum Type {
			SoilHumidityError,
			LightIntensityError,
			AirHumidityError,
			AirTemperatureError,
			AirQualityError,
			AirPressureError,
		};

		enum Status { High, Low };
};

class Error {
	private:
		SensorErrors::Type type;
		SensorErrors::Status status;
		uint8_t priority;

	public:
		Error(
			SensorErrors::Type type, SensorErrors::Status status,
			uint8_t priority
		) {
			this->type	   = type;
			this->status   = status;
			this->priority = priority;
		}

		uint8_t getPriority() const { return this->priority; }

		SensorErrors::Type getErrorType() const { return this->type; }

		bool operator==(const Error & other) const {
			return this == &other && this->type == other.type &&
				   this->status == other.status &&
				   this->priority == other.priority;
		}

		bool operator<(const Error & other) const {
			if (this == &other) {
				return false;
			}
			return this->getPriority() < other.getPriority();
		}

		Error & operator=(const Error & other) {
			if (this != &other) {
				this->priority = other.priority;
				this->status   = other.status;
				this->type	   = other.type;
			}
			return *this;
		}
};

class ErrorQueueClass {
	private:
		std::priority_queue<Error> queue;
		ErrorQueueClass() {}

	public:
		ErrorQueueClass & operator=(ErrorQueueClass &) = delete;
		ErrorQueueClass(ErrorQueueClass &)			   = delete;

		static ErrorQueueClass & getErrorHandler() {
			static ErrorQueueClass errorHandler;
			return errorHandler;
		}

	private:
		/**
		 * @return: The error with top priority after adding the error.
		 */
		const Error & addError(Error & error) {
			queue.push(error);
			return queue.top();
		}

		const Error getPrioritisedError() const { return queue.top(); }

		const Error removePrioritisedError() {
			Error error = queue.top();
			queue.pop();
			return error;
		}

		void clearAllErrors() {
			while (queue.size() > 0) {
				queue.pop();
			}
		}

		/**
		 * Deletes all errors that are the same as toDelete in the error queue
		 * and returns the number of elements that got deleted from the queue.
		 * @param toDelete: The error to delete, get checked with the ==
		 * operator from Error
		 * @return Number of elements deleted
		 */
		uint8_t deleteErrorFromQueue(Error & toDelete) {
			// List to store the values we don't wand to delete.
			std::list<Error> list;
			uint8_t numDeleted = 0;
			bool belowPriority = false;
			/*
			Will iterate over all elements in the queue by removing them
			from the queue. If the top element is the error nothing will
			happen and we will move on to the next. If the element is not the
			error we will append it to a linked list. If the top element has
			a lower priotiry as the error to delete we know we have deleted
			all errors of this type.
			*/
			while (!belowPriority && queue.size() > 0) {
				const Error & topError = queue.top();
				if (topError.getPriority() < toDelete.getPriority()) {
					belowPriority = true;
				} else {
					if (topError == toDelete) {
						numDeleted++;
					} else {
						list.push_front(topError);
					}
					queue.pop();
				}
			}
			while (list.size() > 0) {
				queue.push(list.front());
				list.pop_front();
			}
			return numDeleted;
		}

		/**
		 * Deletes all errors of the provided type from the error queue
		 * and returns the number of elements that got deleted.
		 * @param errorType: The type of error to delete.
		 * @return Number of errors deleted.
		 */
		uint8_t deleteErrorFromQueue(SensorErrors::Type errorType) {
			// List to store the values we don't wand to delete.
			std::priority_queue<Error> newQueue;
			uint8_t numDeleted = 0;
			while (queue.size() > 0) {
				const Error & topError = queue.top();
				if (topError.getErrorType() == errorType) {
					numDeleted++;
				} else {
					newQueue.push(topError);
				}
				queue.pop();
			}
			queue = newQueue;
			return numDeleted;
		}
};

class ErrorOutputClass {
	private:
		uint8_t valueRed;
		uint8_t valueGreen;
		uint8_t valueBlue;

		uint8_t pinRed;
		uint8_t pinGreen;
		uint8_t pinBlue;

		uint16_t * durationOn;
		uint16_t * durationOff;
		uint8_t durationSize;
		uint8_t durationIdx			 = 0;
		unsigned long prevChangeTime = 0;
		bool isOn					 = false;

		ErrorOutputClass(
			uint8_t valueRed, uint8_t valueGreen, uint8_t valueBlue,
			uint16_t * durationOn, uint16_t * durationOff, uint8_t durationSize,
			uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue
		) {
			this->durationOn   = durationOn;
			this->durationOff  = durationOff;
			this->durationSize = durationSize;
			this->valueRed	   = valueRed;
			this->valueGreen   = valueGreen;
			this->valueBlue	   = valueBlue;
			this->pinRed	   = pinRed;
			this->pinGreen	   = pinGreen;
			this->pinBlue	   = pinBlue;
			setLEDStatus(true);
			pinMode(pinRed, OUTPUT);
			pinMode(pinGreen, OUTPUT);
			pinMode(pinBlue, OUTPUT);
		}

	public:
		ErrorOutputClass & operator=(ErrorOutputClass &) = delete;
		ErrorOutputClass(ErrorOutputClass &)			 = delete;

		static ErrorOutputClass & getErrorOutput(
			uint8_t valueRed, uint8_t valueGreen, uint8_t valueBlue,
			uint16_t * durationOn, uint16_t * durationOff, uint8_t durationSize,
			uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue
		) {
			static ErrorOutputClass errorClass(
				valueRed, valueGreen, valueBlue, durationOn, durationOff,
				durationSize, pinRed, pinGreen, pinBlue
			);
			return errorClass;
		}

	private:
		void setLEDStatus(bool on) {
			if (on == this->isOn) {
				return;
			}
			this->isOn			 = on;
			this->prevChangeTime = millis();
			if (isOn) {
				analogWrite(this->pinRed, this->valueRed);
				analogWrite(this->pinGreen, this->valueGreen);
				analogWrite(this->pinBlue, this->valueBlue);
			} else {
				analogWrite(this->pinRed, 0);
				analogWrite(this->pinGreen, 0);
				analogWrite(this->pinBlue, 0);
			}
		}

		void toggleLEDStatus() { setLEDStatus(!this->isOn); }

	public:
		uint16_t getMsTillNext() {
			if (durationIdx >= durationSize) {
				return 0;
			}
			uint16_t * timeArray	 = isOn ? durationOn : durationOff;
			unsigned long passedTime = millis();
			if (passedTime > timeArray[durationIdx]) {
				return 0;
			}
			return timeArray[durationIdx] - passedTime;
		}

		uint16_t updateLEDStatus() {
			uint16_t remainingTime = getMsTillNext();
			if (remainingTime > 0) {
				// Remaining time to wait
				return remainingTime;
			}
			if (durationIdx >= durationSize) {
				setLEDStatus(false);
				return 0;
			}
			toggleLEDStatus();
			if (this->isOn) {
				durationIdx++;
			}
			return getMsTillNext();
		}
};

#endif
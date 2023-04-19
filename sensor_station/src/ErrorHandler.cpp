#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include <Arduino.h>
#include <LinkedList.h>
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

class ErrorHandlerClass {
	private:
		std::priority_queue<Error> queue;

	public:
		/**
		 * @return: The error with top priority after adding the error.
		 */
		const Error & addError(Error error) {
			queue.push(error);
			return queue.top();
		}

		const Error getTop() { return queue.top(); }

		const Error removeTop() {
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
};

#endif
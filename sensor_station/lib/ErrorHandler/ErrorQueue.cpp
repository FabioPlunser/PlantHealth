#ifndef ERROR_QUEUE_CLASS
#define ERROR_QUEUE_CLASS

#include "SensorError.cpp"
#include "SensorErrors.h"

#include <Arduino.h>
#include <list>
#include <queue>

class ErrorQueue {
	private:
		std::priority_queue<SensorError> queue;
		ErrorQueue() {}

	public:
		ErrorQueue & operator=(ErrorQueue &) = delete;
		ErrorQueue(ErrorQueue &)			 = delete;

		static ErrorQueue & getErrorQueue() {
			static ErrorQueue errorQueue;
			return errorQueue;
		}

	public:
		/**
		 * @return: The error with top priority after adding the error.
		 */
		const SensorError & addError(SensorError & error) {
			queue.push(error);
			return queue.top();
		}

		const SensorError getPrioritisedError() const { return queue.top(); }

		const SensorError removePrioritisedError() {
			SensorError error = queue.top();
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
		 * operator from SensorError
		 * @return Number of elements deleted
		 */
		uint8_t deleteErrorFromQueue(SensorError & toDelete) {
			// List to store the values we don't wand to delete.
			std::list<SensorError> list;
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
				const SensorError & topError = queue.top();
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
			std::priority_queue<SensorError> newQueue;
			uint8_t numDeleted = 0;
			while (queue.size() > 0) {
				const SensorError & topError = queue.top();
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

#endif
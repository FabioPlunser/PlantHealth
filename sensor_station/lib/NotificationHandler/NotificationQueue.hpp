#ifndef ERROR_QUEUE_CLASS
#define ERROR_QUEUE_CLASS

#include "SensorError.hpp"
#include "SensorErrors.h"

#include <Arduino.h>
#include <list>
#include <queue>

/**
 * A class to sort the current errors that occur in the program. It will store
 * the errors in a prioirty queue and provide methods to get the notification
 * with the highes priority.
 */
class NotificationQueue {
	private:
		// Define the comparator for the priority queue.
		std::priority_queue<
			const Notification *, std::vector<const Notification *>,
			Notification::NotificationLessComparator>
			queue;
		NotificationQueue() {}

	public:
		NotificationQueue & operator=(NotificationQueue &) = delete;
		NotificationQueue(NotificationQueue &)			   = delete;

		static NotificationQueue * getNotificationQueue() {
			DEBUG_PRINT_POS(4, "\n");
			static NotificationQueue notificationQueue;
			return &notificationQueue;
		}

	public:
		bool isEmpty() {
			DEBUG_PRINT_POS(4, "\n");
			return this->queue.empty();
		}

		/**
		 * Adds a copy of the notification to the queue. If it is a sensor error
		 * it will convert it to prevent slicing.
		 * @return: The error with top priority after adding the error.
		 */
		const Notification & addError(Notification & notification) {
			DEBUG_PRINT_POS(4, "\n");
			Notification * queueElement;
			// If it is a sensor error we need to convert it to prevent slicing.
			switch (notification.getNotificationType()) {
				case Notification::NotificationType::SENSOR_ERROR:
					queueElement = new SensorError(
						*static_cast<SensorError *>(&notification)
					);
					break;

				default:
					queueElement = new Notification(notification);
					break;
			}
			// Add the copy to the queue
			queue.push(queueElement);
			return *queue.top();
		}

		/**
		 * Get the notification with the highest priority (determined by the
		 * provided operator of the Notification class).
		 */
		const Notification * getPrioritisedNotification() const {
			DEBUG_PRINT_POS(4, "\n");
			return queue.top();
		}

		/**
		 * Deletes the notification with the highest priority (determined by the
		 * provided operator of the Notification class).
		 */
		void deletePrioritisedNotification() {
			DEBUG_PRINT_POS(4, "\n");
			const Notification * notificationToDelete = queue.top();
			queue.pop();
			delete (notificationToDelete);
			return;
		}

		/**
		 * Deletes all notifications in the queue.
		 */
		void clearAllErrors() {
			DEBUG_PRINT_POS(4, "\n");
			while (!queue.empty()) {
				deletePrioritisedNotification();
			}
		}

		uint8_t getSize() {
			DEBUG_PRINT_POS(4, "\n");
			return queue.size();
		}

		/**
		 * Deletes all errors that are the same as toDelete in the error queue
		 * and returns the number of elements that got deleted from the queue.
		 * @param toDelete: The error to delete, get checked with the ==
		 * operator from SensorError
		 * @return Number of elements deleted
		 */
		uint8_t deleteErrorFromQueue(Notification & toDelete) {
			DEBUG_PRINT_POS(4, "\n");
			DEBUG_PRINTF_POS(3, "Queue size at beginning = %d\n", queue.size());
			// List to store the values we don't wand to delete.
			std::list<const Notification *> list;
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
			while (!belowPriority && !queue.empty()) {
				// Get the top error and check if it is the error to delete.
				const Notification * topError = queue.top();
				// If the Type is different no casting is required.
				if (topError->getNotificationType() !=
					toDelete.getNotificationType()) {
					list.push_front(topError);
					// Since they are sorted by priority we can stop here if the
					// highest is lower than the desired one.
				} else if (topError->getPriority() < toDelete.getPriority()) {
					belowPriority = true;
				} else {
					bool isEqual = false;
					// Cast to SensorError to use the == operator, if they are
					// of the type SensorError.
					switch (topError->getNotificationType()) {
						case Notification::NotificationType::SENSOR_ERROR:
							isEqual =
								*static_cast<const SensorError *>(topError) ==
								*static_cast<const SensorError *>(&toDelete);
							break;

						default:
							isEqual = *topError == toDelete;
							break;
					}
					if (isEqual) {
						numDeleted++;
						delete (topError);
					} else {
						// Store all notifications that are not the error to add
						// them back afterwards.
						list.push_front(topError);
					}
				}
				// Remove the top element from the queue. ot get the next one.
				queue.pop();
			}
			// Add all elements that were not to be removed back to the queue.
			while (list.size() > 0) {
				queue.push(list.front());
				list.pop_front();
			}
			DEBUG_PRINTF_POS(3, "Queue size at end = %d\n", queue.size());
			return numDeleted;
		}

		/**
		 * Deletes all errors of the provided type from the error queue
		 * and returns the number of elements that got deleted.
		 * @param errorType: The type of error to delete.
		 * @return Number of errors deleted.
		 */
		uint8_t deleteErrorFromQueue(SensorErrors::Type errorType) {
			DEBUG_PRINT_POS(4, "\n");
			DEBUG_PRINTF_POS(3, "Queue size at beginning = %d\n", queue.size());
			DEBUG_PRINTF_POS(4, "Deleting error type: %d\n", errorType);
			// Here we need to go over all elements. Therefore we will create a
			// new queue of the same type and add all errors that are not to be
			// deleted to it.
			decltype(queue) newQueue;
			uint8_t numDeleted = 0;
			// Go over all elements in the queue
			while (!queue.empty()) {
				const Notification * topNotification = queue.top();
				// Cast to SensorError to check for the error type, if they are
				// of the type SensorError.
				if (topNotification->getNotificationType() ==
					Notification::SENSOR_ERROR) {
					const SensorError * topError =
						static_cast<const SensorError *>(topNotification);
					// If the error type is the one we want to delete we
					// increase the counter and delete the error.
					if (topError->getErrorType() == errorType) {
						numDeleted++;
						DEBUG_PRINTF_POS(
							4, "Deleted error %d\n", topError->getErrorType()
						);
						delete (topError);
						// Else we push the error to the new queue.
					} else {
						newQueue.push(topNotification);
					}
					// If of the type Notification we can just add the
					// notification to the queue.
				} else {
					newQueue.push(topNotification);
				}
				// Remove the top element we just processed.
				queue.pop();
			}
			// Replace the old queue with the new one.
			queue = newQueue;
			DEBUG_PRINTF_POS(3, "Queue size at beginning = %d\n", queue.size());
			return numDeleted;
		}
};

#endif
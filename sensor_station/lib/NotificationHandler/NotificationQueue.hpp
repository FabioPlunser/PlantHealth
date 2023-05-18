#ifndef ERROR_QUEUE_CLASS
#define ERROR_QUEUE_CLASS

#include "SensorError.hpp"
#include "SensorErrors.h"

#include <Arduino.h>
#include <list>
#include <queue>

class NotificationQueue {
	private:
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
		 * @return: The error with top priority after adding the error.
		 */
		const Notification & addError(Notification & notification) {
			DEBUG_PRINT_POS(4, "\n");
			Notification * queueElement;
			switch (notification.getNotificationType()) {
				case Notification::NotificationType::SENSOR_ERROR:
					queueElement = new SensorError(
						*static_cast<SensorError *>(&notification)
					);
					// TODO: Test if all the values get sett accordingly.
					break;

				default:
					queueElement = new Notification(notification);
					break;
			}
			queue.push(queueElement);
			return *queue.top();
		}

		const Notification * getPrioritisedNotification() const {
			DEBUG_PRINT_POS(4, "\n");
			return queue.top();
		}

		void deletePrioritisedNotification() {
			DEBUG_PRINT_POS(4, "\n");
			const Notification * notificationToDelete = queue.top();
			queue.pop();
			delete (notificationToDelete);
			return;
		}

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
				const Notification * topError = queue.top();
				if (topError->getNotificationType() !=
					toDelete.getNotificationType()) {
					list.push_front(topError);
				} else if (topError->getPriority() < toDelete.getPriority()) {
					belowPriority = true;
				} else {
					bool isEqual = false;
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
						list.push_front(topError);
					}
				}
				queue.pop();
			}
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
			// List to store the values we don't wand to delete.
			decltype(queue) newQueue;
			uint8_t numDeleted = 0;
			while (!queue.empty()) {
				const Notification * topNotification = queue.top();
				if (topNotification->getNotificationType() ==
					Notification::SENSOR_ERROR) {
					const SensorError * topError =
						static_cast<const SensorError *>(topNotification);
					if (topError->getErrorType() == errorType) {
						numDeleted++;
						DEBUG_PRINTF_POS(
							4, "Deleted error %d\n", topError->getErrorType()
						);
						delete (topError);
					} else {
						newQueue.push(topNotification);
					}
				} else {
					newQueue.push(topNotification);
				}
				queue.pop();
			}
			queue = newQueue;
			DEBUG_PRINTF_POS(3, "Queue size at beginning = %d\n", queue.size());
			return numDeleted;
		}
};

#endif
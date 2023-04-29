#ifndef ERROR_QUEUE_CLASS
#define ERROR_QUEUE_CLASS

#include "SensorError.cpp"
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
			static NotificationQueue notificationQueue;
			return &notificationQueue;
		}

	public:
		/**
		 * @return: The error with top priority after adding the error.
		 */
		const Notification & addError(Notification & notification) {
			Notification * queueElement;
			switch (notification.getNotificationType()) {
				case Notification::NotificationType::ERROR:
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
			return queue.top();
		}

		void deletePrioritisedNotification() {
			const Notification * notificationToDelete = queue.top();
			queue.pop();
			delete (notificationToDelete);
			return;
		}

		void clearAllErrors() {
			while (!queue.empty()) {
				deletePrioritisedNotification();
			}
		}

		uint8_t getSize() { return queue.size(); }

		/**
		 * Deletes all errors that are the same as toDelete in the error queue
		 * and returns the number of elements that got deleted from the queue.
		 * @param toDelete: The error to delete, get checked with the ==
		 * operator from SensorError
		 * @return Number of elements deleted
		 */
		uint8_t deleteErrorFromQueue(Notification & toDelete) {
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
						case Notification::NotificationType::ERROR:
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
			decltype(queue) newQueue;
			uint8_t numDeleted = 0;
			while (!queue.empty()) {
				const Notification * topNotification = queue.top();
				if (topNotification->getNotificationType() ==
					Notification::ERROR) {
					const SensorError * topError =
						static_cast<const SensorError *>(topNotification);
					if (topError->getErrorType() == errorType) {
						numDeleted++;
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
			return numDeleted;
		}
};

#endif
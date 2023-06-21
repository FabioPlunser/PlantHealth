#ifndef NOTIFICATION_CLASS
#define NOTIFICATION_CLASS

#include "../../include/Defines.h"

#include <Arduino.h>

/**
 * A class for a notification with a priority and simple methods to get the
 * priority and the type of the notification.
 */
class Notification {
	public:
		enum NotificationType {
			NOTIFICATION = PRIORITY_NOTIFICATIONS,
			SENSOR_ERROR = PRIORITY_SENSOR_ERRORS
		};

	protected:
		NotificationType notificationType;

	private:
		uint8_t priority;

	public:
		Notification(uint8_t priority) {
			this->notificationType = NotificationType::NOTIFICATION;
			this->priority		   = priority;
		}

		NotificationType getNotificationType() const {
			return this->notificationType;
		}

		uint8_t getPriority() const { return this->priority; }

		bool operator==(const Notification & other) const {
			return this->notificationType == other.notificationType &&
				   this->priority == other.priority;
		}

		/**
		 * Compares two notifications. If the notification types are different
		 * they are sorted by their notification type. If the notification types
		 * are the same they are sorted by their priority.
		 */
		bool operator<(const Notification & other) const {
			if (this == &other) {
				return false;
			}
			// If one of them is an error and the other not, it return true if
			// the first one is the one with the error. This will provide a
			// sorting with all notifications at first and the errors at the
			// end.
			return this->notificationType != other.notificationType
					   ? this->notificationType < other.notificationType
					   : this->priority < other.priority;
		}

		Notification & operator=(const Notification & other) {
			if (this != &other) {
				this->priority		   = other.priority;
				this->notificationType = other.notificationType;
			}
			return *this;
		}
		/**
		 * Comparator struct for the priority queue to sort the notifications
		 */
		struct NotificationLessComparator {
				bool
				operator()(const Notification * fst, const Notification * snd) {
					return *fst < *snd;
				}
		};
};

#endif
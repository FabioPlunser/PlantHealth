#include "lib/NotificationHandler/NotificationQueue.cpp"

#include "Colors.h"
#include "lib/NotificationHandler/Notification.cpp"
#include "lib/NotificationHandler/SensorError.cpp"
#include "lib/NotificationHandler/SensorErrors.h"

#include <cassert>
#include <cstdio>

#define ASSERT_TRUE(statement)                                               \
	if (statement) {                                                         \
		printf(                                                              \
			COLOR_A_GRN "%s - %d succeded\n" COLOR_RESET, __func__, __LINE__ \
		);                                                                   \
	} else {                                                                 \
		printf(                                                              \
			COLOR_A_RED "%s - %d failed\n" COLOR_RESET, __func__, __LINE__   \
		);                                                                   \
	}

#define ASSERT_FALSE(statement) ASSERT_TRUE(!(statement))

#define ASSERT_EQUAL_INT(expected, actual)                                     \
	if (expected == actual) {                                                  \
		printf(COLOR_A_GRN "%s - %d equal\n" COLOR_RESET, __func__, __LINE__); \
	} else {                                                                   \
		printf(                                                                \
			COLOR_A_RED "%s - %d expected: %d, actual: %d\n" COLOR_RESET,      \
			__func__, __LINE__, expected, actual                               \
		);                                                                     \
	}

#define CAST_NOTIFICATION_TO_SENSOR_ERROR(notification, varName) \
	assert(                                                      \
		notification->getNotificationType() ==                   \
		Notification::NotificationType::SENSOR_ERROR             \
	);                                                           \
	varName = static_cast<const SensorError *>(notification);

void testNotification_Comparing() {
	Notification n1(7);
	Notification n2(3);
	Notification n3(3);
	ASSERT_FALSE(n1 < n2);
	ASSERT_FALSE(n1 == n2);
	ASSERT_FALSE(n2 < n3);
	ASSERT_TRUE(n2 < n1);
	ASSERT_TRUE(n2 == n3);
}

void testNotificationQueue_EmptyAtCreaton() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	ASSERT_EQUAL_INT(0, queue->getSize());
}

void testNotificationQueue_SizeIncreasesByOneAtErrorAdding() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	;
	ASSERT_EQUAL_INT(0, queue->getSize());
	Notification n1(1);
	Notification n2(2);
	Notification n3(3);
	Notification n4(4);
	Notification n5(5);
	queue->addError(n1);
	queue->addError(n2);
	queue->addError(n3);
	queue->addError(n4);
	queue->addError(n5);
	ASSERT_EQUAL_INT(5, queue->getSize());
	queue->deleteErrorFromQueue(n1);
	ASSERT_EQUAL_INT(4, queue->getSize());
	queue->deleteErrorFromQueue(n4);
	ASSERT_EQUAL_INT(3, queue->getSize());
	queue->deleteErrorFromQueue(n2);
	ASSERT_EQUAL_INT(2, queue->getSize());
	queue->deleteErrorFromQueue(n3);
	ASSERT_EQUAL_INT(1, queue->getSize());
	queue->deleteErrorFromQueue(n5);
	ASSERT_EQUAL_INT(0, queue->getSize());
}

void testNotificationQueue_SizeDecreasesWithDeletion() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	;
	ASSERT_EQUAL_INT(0, queue->getSize());
	Notification n1(5);
	queue->addError(n1);
	ASSERT_EQUAL_INT(1, queue->getSize());
}

void testNotificationQueue_ErrorGetsDeletedByPriority() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();

	ASSERT_EQUAL_INT(0, queue->getSize());
	Notification n1(5);
	Notification n2(5);
	Notification n3(5);
	Notification n4(6);
	queue->addError(n1);
	queue->addError(n2);
	queue->addError(n3);
	queue->addError(n4);
	ASSERT_EQUAL_INT(4, queue->getSize());
	queue->deleteErrorFromQueue(n2);
	ASSERT_EQUAL_INT(1, queue->getSize());
}

void testNotificationQueue_getTopPriorityNotifcation() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	uint8_t numbers[]		  = {87, 36, 23, 3, 225, 171};
	for (int i = 0; i < sizeof(numbers) / sizeof(numbers[0]); i++) {
		Notification n(numbers[i]);
		queue->addError(n);
	}
	for (int i = 0; i < 3; i++) {
		const Notification * top = queue->getPrioritisedNotification();
		ASSERT_EQUAL_INT(225, top->getPriority());
	}
	const Notification * top;
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(225, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(171, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(87, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(36, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(23, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(3, top->getPriority());
	queue->deletePrioritisedNotification();
}

// ---------- Sensor Errors in queue ---------- //

void testNotificationQueue_NotificationHaveHigherPriorityThanError() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	Notification n1(5);
	Notification n2(103);
	Notification n3(220);
	SensorError s1(
		SensorErrors::Type::SoilHumidityError, SensorErrors::Status::High, 17
	);
	SensorError s2(
		SensorErrors::Type::LightIntensityError, SensorErrors::Status::Low, 68
	);
	SensorError s3(
		SensorErrors::Type::AirHumidityError, SensorErrors::Status::High, 112
	);
	SensorError s4(
		SensorErrors::Type::AirPressureError, SensorErrors::Status::Low, 221
	);
	queue->addError(n1);
	queue->addError(n2);
	queue->addError(n3);
	queue->addError(s1);
	queue->addError(s2);
	queue->addError(s3);
	queue->addError(s4);
	const Notification * top;
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(220, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(103, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(5, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(221, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(112, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(68, top->getPriority());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	ASSERT_EQUAL_INT(17, top->getPriority());
	queue->deletePrioritisedNotification();
}

void testNotificationQueue_SensorErrorKeepsDataInQueue() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	// Create scope to assert that valus will remain stored even after leaving
	// theit scope
	if (true) {
		SensorError s1(
			SensorErrors::Type::SoilHumidityError, SensorErrors::Status::High,
			68
		);
		SensorError s2(
			SensorErrors::Type::LightIntensityError, SensorErrors::Status::Low,
			17
		);
		SensorError s3(
			SensorErrors::Type::AirHumidityError, SensorErrors::Status::High,
			112
		);
		SensorError s4(
			SensorErrors::Type::AirPressureError, SensorErrors::Status::Low, 221
		);
		queue->addError(s1);
		queue->addError(s2);
		queue->addError(s3);
		queue->addError(s4);
	}

	const Notification * top = queue->getPrioritisedNotification();
	const SensorError * error;
	CAST_NOTIFICATION_TO_SENSOR_ERROR(top, error);
	ASSERT_EQUAL_INT(
		SensorErrors::Type::AirPressureError, error->getErrorType()
	);
	ASSERT_EQUAL_INT(SensorErrors::Status::Low, error->getErrorStatus());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	CAST_NOTIFICATION_TO_SENSOR_ERROR(top, error);
	ASSERT_EQUAL_INT(
		SensorErrors::Type::AirHumidityError, error->getErrorType()
	);
	ASSERT_EQUAL_INT(SensorErrors::Status::High, error->getErrorStatus());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	CAST_NOTIFICATION_TO_SENSOR_ERROR(top, error);
	ASSERT_EQUAL_INT(
		SensorErrors::Type::SoilHumidityError, error->getErrorType()
	);
	ASSERT_EQUAL_INT(SensorErrors::Status::High, error->getErrorStatus());
	queue->deletePrioritisedNotification();
	top = queue->getPrioritisedNotification();
	CAST_NOTIFICATION_TO_SENSOR_ERROR(top, error);
	ASSERT_EQUAL_INT(
		SensorErrors::Type::LightIntensityError, error->getErrorType()
	);
	ASSERT_EQUAL_INT(SensorErrors::Status::Low, error->getErrorStatus());
	queue->deletePrioritisedNotification();
}

// ---------- Main, setup and reset ---------- //

void reset() {
	NotificationQueue * queue = NotificationQueue::getNotificationQueue();
	queue->clearAllErrors();
}

int main() {
	void (*tests[])() = {
		testNotification_Comparing,
		testNotificationQueue_EmptyAtCreaton,
		testNotificationQueue_SizeIncreasesByOneAtErrorAdding,
		testNotificationQueue_SizeDecreasesWithDeletion,
		testNotificationQueue_ErrorGetsDeletedByPriority,
		testNotificationQueue_getTopPriorityNotifcation,
		testNotificationQueue_NotificationHaveHigherPriorityThanError,
		testNotificationQueue_SensorErrorKeepsDataInQueue};

	for (int i = 0; i < sizeof(tests) / sizeof(tests[0]); i++) {
		tests[i]();
		printf("\n");
		reset();
	}
}

#include "Defines.h"

#ifdef DO_BLE_TEST

#include <Arduino.h>
#include <ArduinoBLE.h>
#include <modules/communication.h>

// Define a custom UUID for our service
#define SERVICE_UUID "dea07cc4-d084-11ed-a760-325096b39f48"

BLEService service(SERVICE_UUID);

BLEUnsignedCharCharacteristic batteryLevelChar("2a19", BLERead);

BLEDescriptor batteryLevelDescriptor("2901", "millis");

void setup() {
	// Initialize the BLE library
	BLE.begin();

	BLE.setDeviceName("SensorStation");
	BLE.setLocalName("SensorStation");

	// Add the characteristics and descriptors to the service
	batteryLevelChar.addDescriptor(batteryLevelDescriptor);

	service.addCharacteristic(batteryLevelChar);

	// Add the service to the BLE device
	BLE.addService(service);

	// Start advertising the service
	BLE.advertise();
}

void loop() { BLEDevice central = BLE.central(); }

// void setup() {
// 	Serial.begin(115200);
// 	pinMode(A3, OUTPUT);
// 	pinMode(A6, OUTPUT);

// 	analogWrite(A3, 255);
// 	analogWrite(A6, 0);

// 	while (!Serial) {
// 		;
// 	}
// 	delay(1000);

// 	analogWrite(A3, 0);
// 	analogWrite(A6, 255);

// 	initialize_communication();
// 	enable_pairing_mode();
// }

// void loop() {
// 	BLEDevice central = BLE.central();
// 	if (central) {
// 		Serial.println("* Connected to central device!");
// 		Serial.print("* Device MAC address: ");
// 		Serial.println(central.address());
// 		Serial.println(" ");
// 		while (central.connected()) {
// 			static bool wasHere = false;
// 			if (!wasHere) {
// 				Serial.print("Was inside of loop\n");
// 				wasHere = true;
// 			}
// 			setValues();
// 		}
// 		Serial.println("* Disconnected to central device!");
// 	}
// 	Serial.println("Searching for central device!");
// 	delay(500);
// }

#endif
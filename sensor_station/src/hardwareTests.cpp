#include "Defines.h"

#ifdef DO_HARDWARE_TEST

#define BME680_DEBUG
#define DEBUG_SERIAL Serial

#include "HardwareTests/AirSensorTest.cpp"
#include "HardwareTests/HydrometerTest.cpp"
#include "HardwareTests/LedTest.cpp"
#include "HardwareTests/PhototransistorTest.cpp"
#include "HardwareTests/PiezoBuzzerTest.cpp"

#include <Adafruit_BME680.h>
#include <Arduino.h>
#include <modules/communication.h>

/**
 * This class will execute basic tests for all the connected components of the
 * arduino.
 * ----- Always -----
 * -> Will print notification on button press or change of dip switch with
 * current value in the message
 * ----- Setup only -----
 * -> First it will execute the LED test function, that will fade the color in
 * and out in the order red - green - blue. Afterwards the colours will
 * interwine with each other.
 * -> Second test is the piezo buzzer that will execute 10 logarithmic
 * equidistant sounds ranging from 100Hz to 5kHz.
 * ----- In the loop after setup -----
 * -> Will read the values from the phototransistor, the hydrometer and the air
 * quality sensors and print them periodically till the end of time.
 */

// -------------------- Function Prototypes -------------------- //

bool writeNumberToBufferBinary(
	char * buffer, uint64_t number, uint8_t numberSize
);
void dipSwitchInterruptFunction();
void dipSwitchFunction();
void buttonFunction();
void checkPressedButtons();

// -------------------- Global variables -------------------- //

LedTest ledTest(PIN_RGB_RED, PIN_RGB_GREEN, PIN_RGB_BLUE);
PiezoBuzzerTest piezoBuzzerTest(PIN_PIEZO_BUZZER);
HydrometerTest hydrometerTest(PIN_HYDROMETER);
Adafruit_BME680 * bme680;
AirSensorTest * airSensorTest;
PhototransistorTest phototransistorTest(PIN_PHOTOTRANSISTOR);

// -------------------- Setup -------------------- //

void setup() {
	Serial.begin(9600);
	initialize_communication();
	enable_pairing_mode();

	pinMode(PIN_RGB_BLUE, OUTPUT);
	pinMode(PIN_RGB_GREEN, OUTPUT);
	pinMode(PIN_RGB_RED, OUTPUT);
	analogWrite(PIN_RGB_RED, 255);
	while (!Serial) {
		;
	}

	bme680		  = new Adafruit_BME680();
	airSensorTest = new AirSensorTest(bme680);

	analogWrite(PIN_RGB_RED, 0);
	analogWrite(PIN_RGB_GREEN, 255);
	delay(1000);
	analogWrite(PIN_RGB_GREEN, 0);
	analogWrite(PIN_RGB_RED, 0);
	analogWrite(PIN_RGB_BLUE, 0);

	Serial.print("Start LED test\n");
	delay(1000);
	ledTest.executeTest();
	Serial.print("Finished LED test\n");

	Serial.print("Start piezo-buzzer test\n");
	delay(1000);
	piezoBuzzerTest.executeTest();
	Serial.print("Finished piezo-buzzer test\n");

	// Interrupt setup
	Serial.print("Setup interrupts\n");

	pinMode(PIN_BUTTON_1, INPUT_PULLDOWN);

	attachInterrupt(
		digitalPinToInterrupt(PIN_BUTTON_1), buttonFunction, RISING
	);

	Serial.print("BME688 is set up ");
	Serial.print(airSensorTest->isInitSuccessful() ? "correct" : "incorrect");
	Serial.print(".\n");
	Serial.print("Exit setup.\n");
}

// -------------------- Loop -------------------- //
bool buttonPressed = false;

void loop() {
	static const int buffer_size = 128;
	char buffer[buffer_size];
	// Hydrometer
	sprintf(
		buffer, "Soil humidity Value: %4d -> %6.2f%%\n",
		hydrometerTest.getHumidity_10bit(),
		hydrometerTest.getHumidity_percentage()
	);
	Serial.print(buffer);

	// Air sensor
	uint32_t gas_resistance = 0;
	float temperature = 0, humidity = 0, pressure = 0;
	AirSensorTest::UPDATE_ERROR error = airSensorTest->getMeasuredValues(
		&pressure, &gas_resistance, &temperature, &humidity
	);
	switch (error) {
		case AirSensorTest::UPDATE_ERROR::TO_EARLY:
			snprintf(buffer, buffer_size, "Was to early to update values.\n");
			break;
		case AirSensorTest::UPDATE_ERROR::NOT_INIT:
			snprintf(buffer, buffer_size, "BME680 not initialized.\n");
			break;
		case AirSensorTest::UPDATE_ERROR::LOST_CONNECTION:
			snprintf(buffer, buffer_size, "Lost connection to BME680.\n");
			break;
		case AirSensorTest::UPDATE_ERROR::NOTHING:
			int written = 0;
			written		+= snprintf(
				&buffer[written], buffer_size - written,
				"Temperature: %4.1fC\n", temperature
			);
			written += snprintf(
				&buffer[written], buffer_size - written,
				"Air humidity: %4.1f%%\n", humidity
			);
			written += snprintf(
				&buffer[written], buffer_size - written, "Pressure: %8.1fPa\n",
				pressure
			);
			written += snprintf(
				&buffer[written], buffer_size - written,
				"Gas resistance: %5luOhm\n", gas_resistance
			);
			if (written >= buffer_size - 1) {
				Serial.print("Buffer was not big enough\n");
			}
			Serial.print("Values:\n");
			break;
			// default:
			// 	snprintf(buffer, buffer_size, "Case %d not covored.\n", error);
			// 	break;
	}

	Serial.print(buffer);
	Serial.print("BME688 is set up ");
	Serial.print(airSensorTest->isInitSuccessful() ? "correct" : "incorrect");
	Serial.print(".\n");

	snprintf(
		buffer, buffer_size, "Phototransistor Value: %4d -> %6.2f%%\n\n",
		phototransistorTest.getHumidity_10bit(),
		phototransistorTest.getHumidity_percentage()
	);
	Serial.print(buffer);

	if (buttonPressed) {
		Serial.print("Button pressed.\n");
		dipSwitchFunction();
		buttonPressed = false;
	}

	for (int i = 0; i < 10; i++) {
		dipSwitchFunction();
		checkPressedButtons();
		delay(500);
	}
}

// -------------------- Functions -------------------- //

void buttonFunction() { buttonPressed = true; }

void checkPressedButtons() {
	static PinStatus prevStatus[3] = {PinStatus::LOW};
	PinStatus currentStatus[3]	   = {
		digitalRead(PIN_BUTTON_1), digitalRead(PIN_BUTTON_2),
		digitalRead(PIN_BUTTON_3)};
	for (int i = 0; i < 3; i++) {
		if (prevStatus[i] != currentStatus[i]) {
			char buffer[64];
			snprintf(
				buffer, 64, "Value for button %d changed from %d to %d.\n",
				i + 1, prevStatus[i], currentStatus[i]
			);
			prevStatus[i] = currentStatus[i];
			Serial.print(buffer);
		}
	}
}

void dipSwitchFunction() {
	static uint8_t prevStatus  = 0;
	uint8_t newStatus		   = 0;
	int dipSwitchConnection[8] = {PIN_DIP_1, PIN_DIP_2, PIN_DIP_3, PIN_DIP_4,
								  PIN_DIP_5, PIN_DIP_6, PIN_DIP_7, PIN_DIP_8};
	for (int i = 0; i < 8; i++) {
		newStatus |= digitalRead(dipSwitchConnection[i]) << i;
	}
	if (newStatus != prevStatus) {
		char buffer[64];
		char bufferPrevNum[9];
		char bufferNewNum[9];
		writeNumberToBufferBinary(bufferPrevNum, prevStatus, 8);
		writeNumberToBufferBinary(bufferNewNum, newStatus, 8);
		snprintf(
			buffer, 64, "Previous number: %s, new Number: %s\n", bufferPrevNum,
			bufferNewNum
		);
		Serial.print(buffer);
		prevStatus = newStatus;
	}
}

bool writeNumberToBufferBinary(
	char * buffer, uint64_t number, uint8_t numberSize
) {
	if (numberSize > 64) {
		return false;
	}
	// Will write buffer with highest bit first
	// Shift number rigth to get the bit at that position
	for (int i = 0; i < numberSize; i++) {
		buffer[i] = '0' + ((number >> (numberSize - i - 1)) & 0b1);
	}
	buffer[numberSize] = '\0';
	return true;
}

#endif

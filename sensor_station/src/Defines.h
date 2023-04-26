#ifndef GENERAL_PURPOSE_DEFINITIONS
#define GENERAL_PURPOSE_DEFINITIONS

// ----- Functions ----- //

#define ERROR_PRINT(text, value) \
	Serial.print(__func__);      \
	Serial.print(" -> ");        \
	Serial.print(text);          \
	Serial.println(value);

// ----- Error handling functions ----- //

#define TRUE  1
#define FALSE 0

/* If DO_HARDWARE_TEST is defined the hardware tests will be executet to test
 all the connected devices of the Arduino. Otherwise the main programm will be
 flashed onto the arduino. DO_MAIN will execute the main program. DO_BLE_TEST
 will initialize a connection and send values to it
*/
// #define DO_HARDWARE_TEST
#define DO_MAIN
// #define DO_BLE_TEST

/* If USE_DESCRIPTORS is true ble.cpp will use descriptors. Otherwise it will
 * use more services.
 */
#define USE_DESCRIPTORS				  FALSE

/**
 * If this value is true it will output the time it takes to read the sensor
 * values to the Serial.
 */
#define PRINT_TIME_READ_SENSOR		  TRUE

// Definition of boundary values
#define ANALOG_READ_MAX_VALUE		  1023
#define UPDATE_INTERVAL_BME680_MS_MAX 1000
#define DURATION_IN_PAIRING_MODE_MS	  60'000

// This line defines according to GATT what info fileds will be sent over BLE
#define BATTERY_LEVEL_FLAGS_FIELD	  0b0'1'0
#define BATTERY_POWER_STATE_FLAGS	  0b000'000'00'00'00'01'0

// Mapping of the arduino pin connections
#define PIN_PHOTOTRANSISTOR			  A0
#define PIN_HYDROMETER				  A1
#define PIN_PIEZO_BUZZER			  A2
#define PIN_RGB_RED					  A3
#define PIN_SDA						  A4
#define PIN_SCL						  A5
#define PIN_RGB_BLUE				  A6
#define PIN_RGB_GREEN				  A7

#define PIN_DIP_1					  D12 // Lowest
#define PIN_DIP_2					  D11
#define PIN_DIP_3					  D10
#define PIN_DIP_4					  D9
#define PIN_DIP_5					  D8
#define PIN_DIP_6					  D7
#define PIN_DIP_7					  D6
#define PIN_DIP_8					  D5 // Highest

#define PIN_BUTTON_1				  D4 // R
#define PIN_BUTTON_2				  D3 // M
#define PIN_BUTTON_3				  D2 // L

#endif

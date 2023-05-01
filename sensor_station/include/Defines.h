#ifndef GENERAL_PURPOSE_DEFINITIONS
#define GENERAL_PURPOSE_DEFINITIONS

#define TRUE			  1
#define FALSE			  0

/**
 * Will set the level of debug prints.
 * 0 -> No print at all appear.
 * 1 -> Basic notifications.
 * 2 -> More notifications.
 * 3 -> Even more notifications. (Not used so far)
 * 4 -> Full call stack of every function call. (Not implemented so far)
 */
#define DEBUG_PRINT_LEVEL 3

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
#define USE_DESCRIPTORS					  FALSE

/**
 * If this value is true it will output the time it takes to read the sensor
 * values to the Serial.
 */
#define PRINT_TIME_READ_SENSOR			  TRUE

/**
 * If this constant is set to true the sensor station will only connect to the
 * device that was connected previously. If false no button press is required to
 * pair it and it will always send data to a device that connects.
 */
#define PAIRING_BUTTON_REQUIRED			  FALSE

// Definition of boundary values
#define ANALOG_READ_MAX_VALUE			  1023
#define UPDATE_INTERVAL_BME680_MS_MAX	  1000
#define DURATION_IN_PAIRING_MODE_MS		  (5 * 60'000)
#define TIME_CHECK_BLE_CENTRAL_PRESENT_MS 1000

// This line defines according to GATT what info fields will be sent over BLE
#define BATTERY_LEVEL_FLAGS_FIELD		  0b0'1'0
#define BATTERY_POWER_STATE_FIELD		  0b000'000'00'00'00'01'0

// Values that will be sent over BLE
#define SENSOR_STATION_LOCKED_VALUE		  0
#define SENSOR_STATION_UNLOCKED_VALUE	  1

/*
	This section will be used to define what values that the sensor station gets
   will result in which errors.
*/
#define ERROR_VALUE_NOTHING				  0
#define ERROR_VALUE_LOW					  1
#define ERROR_VALUE_HIGH				  2

/*
	Here one can define the priority of the errors.
	If 2 errors have the same priority only one will be displayed. Which one it
   is is undefined. Highest number means highest priority.
*/
#define AIR_QUALITY_TO_HIGH_PRIORITY	  6'1
#define AIR_QUALITY_TO_LOW_PRIORITY		  6'0
#define SOIL_HUMIDITY_TO_HIGH_PRIORITY	  5'1
#define SOIL_HUMIDITY_TO_LOW_PRIORITY	  5'0
#define AIR_TEMPERATURE_TO_HIGH_PRIORITY  4'1
#define AIR_TEMPERATURE_TO_LOW_PRIORITY	  4'0
#define LUMINOSITY_TO_HIGH_PRIORITY		  3'1
#define LUMINOSITY_TO_LOW_PRIORITY		  3'0
#define AIR_HUMIDITY_TO_HIGH_PRIORITY	  2'1
#define AIR_HUMIDITY_TO_LOW_PRIORITY	  2'0
#define AIR_PRESSURE_TO_HIGH_PRIORITY	  1'1
#define AIR_PRESSURE_TO_LOW_PRIORITY	  1'0

/*
	This section of priorities will bind stronger than the Sensor errors.
	If PIORITY_ERRORS is lower than PRIORITY_NOTIFICATIONS then the notification
   will allways be displayed first.
*/
#define PRIORITY_SENSOR_ERRORS			  0
#define PRIORITY_NOTIFICATIONS			  1

// These are the color codes for the LEDs with time intervall for on and off
// Colors are in order Red'Green'Blue
#define LED_COLOR_ERROR_AIR_QUALITY		  0xFF'00'FF
#define LED_COLOR_ERROR_AIR_TEMPERATURE	  0xFF'00'00
#define LED_COLOR_ERROR_AIR_HUMIDITY	  0xFF'FF'FF
#define LED_COLOR_ERROR_AIR_PRESSURE	  0xFF'99'00
#define LED_COLOR_ERROR_SOIL_HUMIDITY	  0x00'00'FF
#define LED_COLOR_ERROR_LIGHT_INTENSITY	  0xFF'FF'00
#define LED_TIME_ERROR_ON_MS			  1000
#define LED_TIME_ERROR_OFF_MS			  4000
#define LED_TIME_ERROR_BLINK_PAUSE_MS	  1000

#define LED_COLOR_NOTIFICATION_PAIRING	  0x00'FF'00
#define LED_TIME_NOTIFICATION_ON_MS		  1000
#define LED_TIME_NOTIFICATION_OFF_MS	  4000

#define PIEZO_BUZZET_TONE_INTERVALL_MS	  15'000
#define PIEZO_BUZZET_TONE_DURATION_MS	  100
#define PIEZO_BUZZET_TONE_FREQUENCY_HZ	  3'000

// Mapping of the arduino pin connections
#define PIN_PHOTOTRANSISTOR				  A0
#define PIN_HYDROMETER					  A1
#define PIN_PIEZO_BUZZER				  A2
#define PIN_RGB_RED						  A3
#define PIN_SDA							  A4
#define PIN_SCL							  A5
#define PIN_RGB_BLUE					  A6
#define PIN_RGB_GREEN					  A7

#define PIN_DIP_1						  D12 // Lowest
#define PIN_DIP_2						  D11
#define PIN_DIP_3						  D10
#define PIN_DIP_4						  D9
#define PIN_DIP_5						  D8
#define PIN_DIP_6						  D7
#define PIN_DIP_7						  D6
#define PIN_DIP_8						  D5 // Highest

#define PIN_BUTTON_1					  D4 // R
#define PIN_BUTTON_2					  D3 // M
#define PIN_BUTTON_3					  D2 // L

#endif

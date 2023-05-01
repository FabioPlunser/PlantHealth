#include "Defines.h"

// ----- Functions ----- //

#define ERROR_PRINT(text, value) \
	Serial.print(__func__);      \
	Serial.print(" -> ");        \
	Serial.print(text);          \
	Serial.println(value);

#define DEBUG_PRINT(level, text)      \
	if (level >= DEBUG_PRINT_LEVEL) { \
		Serial.print(text);           \
	}

#define DEBUG_PRINTLN(level, text) \
	DEBUG_PRINT(level, text);      \
	Serial.println();

#define DEBUG_PRINTF(level, formattedText, ...)                         \
	if (DEBUG_PRINT_LEVEL >= level) {                                   \
		char debugText[256];                                            \
		snprintf(                                                       \
			debugText, sizeof(debugText) / sizeof(char), formattedText, \
			__VA_ARGS__                                                 \
		);                                                              \
		Serial.print(debugText);                                        \
	}

#define DEBUG_PRINT_POSITION(level)   \
	if (level >= DEBUG_PRINT_LEVEL) { \
		Serial.print(__FILE__);       \
		Serial.print(" -> ");         \
		Serial.print(__LINE__);       \
		Serial.print(" ");            \
		Serial.print(__func__);       \
		Serial.print(": ");           \
	}

#define DEBUG_PRINT_POS(level, text) \
	DEBUG_PRINT_POSITION(level);     \
	DEBUG_PRINT(level, text)

#define DEBUG_PRINTLN_POS(level, text) \
	DEBUG_PRINT_POSITION(level);       \
	DEBUG_PRINTLN(level, text)

#define DEBUG_PRINTF_POS(level, formattedText, ...) \
	DEBUG_PRINT_POSITION(level);                    \
	DEBUG_PRINTF(level, formattedText, __VA_ARGS__)

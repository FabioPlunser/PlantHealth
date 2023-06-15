#ifndef HYDROMETER_TEST_CLASS
#define HYDROMETER_TEST_CLASS

#include <Arduino.h>
#include <Defines.h>

/**
 * Class to handle a hydrometer that gets read over a analog read.
 */
class HydrometerClass {
	private:
		int pin;
		uint16_t humidity;

		/**
		 * Will perform a reading on the sensor and update the values, if the
		 * previous reading is older than 1s.
		 */
		void tryUpdateValues() {
			static const int MILLIS_TILL_UPDATE = 1000;
			static unsigned long lastUpdate =
				millis() - (MILLIS_TILL_UPDATE + 1);
			if (millis() - lastUpdate > MILLIS_TILL_UPDATE) {
				lastUpdate	   = millis();
				this->humidity = analogRead(this->pin);
			}
		}

	public:
		HydrometerClass(int pinNum) {
			this->pin = pinNum;
			pinMode(this->pin, INPUT);
			tryUpdateValues();
		}
		/**
		 * Will try to update the currently stored value of the humidity.
		 * @returns: The humidity in 16bit format (0-1023)
		 */
		uint16_t getHumidity_16bit() {
			tryUpdateValues();
			return this->humidity;
		}

		/**
		 * Will convert the 16bit humidity value to a percentage.
		 * @returns: The humidity in floating point percentage (0-100)
		 */
		float getHumidity_percentage() {
			tryUpdateValues();
			return ((float) this->humidity) * 100 / ANALOG_READ_MAX_VALUE;
		}
};

#endif
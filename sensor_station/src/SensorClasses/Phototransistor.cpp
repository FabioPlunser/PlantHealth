#ifndef PHOTOTRANSISTOR_CLASS
#define PHOTOTRANSISTOR_CLASS

#include <Arduino.h>
#include <Defines.h>

/**
 * Class to handle a phototransistor that gets read over a analog read.
 */
class PhototransistorClass {
	private:
		int pin;
		uint16_t lighting;

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
				this->lighting = analogRead(this->pin);
			}
		}

	public:
		PhototransistorClass(int pinNum) {
			this->pin = pinNum;
			pinMode(this->pin, INPUT);
			tryUpdateValues();
		}

		/**
		 * Will try to update the currently stored value of the lighting.
		 * @returns: The lighting in 16bit format (0-1023)
		 */
		uint16_t getLighting_16bit() {
			tryUpdateValues();
			return this->lighting;
		}

		/**
		 * Will convert the 16bit lighting value to a percentage.
		 * @returns: The lighting in floating point percentage (0-100)
		 */
		float getLighting_percentage() {
			tryUpdateValues();
			return ((float) this->lighting) * 100 / ANALOG_READ_MAX_VALUE;
		}
};

#endif
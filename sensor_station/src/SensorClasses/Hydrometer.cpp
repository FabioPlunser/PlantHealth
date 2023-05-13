#ifndef HYDROMETER_TEST_CLASS
#define HYDROMETER_TEST_CLASS

#include <Arduino.h>
#include <Defines.h>

class HydrometerClass {
	private:
		int pin;
		uint16_t humidity;

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

		uint16_t getHumidity_16bit() {
			tryUpdateValues();
			return this->humidity;
		}

		float getHumidity_percentage() {
			tryUpdateValues();
			return ((float) this->humidity) * 100 / ANALOG_READ_MAX_VALUE;
		}
};

#endif
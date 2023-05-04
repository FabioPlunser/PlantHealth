#ifndef PHOTOTRANSISTOR_CLASS
#define PHOTOTRANSISTOR_CLASS

#include <Arduino.h>
#include <Defines.h>

class PhototransistorClass {
	private:
		int pin;
		uint16_t lighting;

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

		uint16_t getLighting_16bit() {
			tryUpdateValues();
			return this->lighting;
		}

		float getLighting_percentage() {
			tryUpdateValues();
			return ((float) this->lighting) * 100 / ANALOG_READ_MAX_VALUE;
		}
};

#endif
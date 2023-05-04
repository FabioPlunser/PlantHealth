#ifndef PIEZO_BUZZER_TEST_CLASS
#define PIEZO_BUZZER_TEST_CLASS

#include "../../include/Defines.h"

#include <Arduino.h>

class PiezoBuzzerController {
	private:
		uint8_t buzzerPin;

		PiezoBuzzerController(int pin) {
			this->buzzerPin = pin;
			pinMode(this->buzzerPin, OUTPUT);
		}

	public:
		PiezoBuzzerController & operator=(PiezoBuzzerController &) = delete;
		PiezoBuzzerController(PiezoBuzzerController &)			   = delete;

		static PiezoBuzzerController * getInstance(int pin) {
			static PiezoBuzzerController controller(pin);
			return &controller;
		}

		/**
		 * Will output a tone at a frequency and duration provided over the
		 * parametes.
		 */
		void startBuzzer(unsigned int frequency, unsigned int duration = 1000) {
			tone(this->buzzerPin, frequency, duration);
		}
		void stopBuzzer() { noTone(this->buzzerPin); }
};

#endif
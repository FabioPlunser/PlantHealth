#ifndef PIEZO_BUZZER_TEST_CLASS
#define PIEZO_BUZZER_TEST_CLASS

#include "../../include/Defines.h"

#include <Arduino.h>
#include <tuple>
#include <vector>

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
#if USE_PIEZO_BUZZER
			tone(this->buzzerPin, frequency, duration);
#endif // USE_PIEZO_BUZZER
		}
		void stopBuzzer() { noTone(this->buzzerPin); }

		void playMelody(
			std::vector<std::tuple<uint16_t, uint16_t>> noteAndDurationList
		) {
#if USE_PIEZO_BUZZER
			for (auto noteAndDuration : noteAndDurationList) {
				uint16_t note	  = std::get<0>(noteAndDuration);
				uint16_t duration = std::get<1>(noteAndDuration);
				this->startBuzzer(note, duration);
				delay(50);
			}
			stopBuzzer();
#endif // USE_PIEZO_BUZZER
		}
};

#endif
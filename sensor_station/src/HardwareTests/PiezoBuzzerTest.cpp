#ifndef PIEZO_BUZZER_TEST_CLASS
#define PIEZO_BUZZER_TEST_CLASS

#include <Arduino.h>
#include <Defines.h>

class PiezoBuzzerTest {
	private:
		int buzzerPin;

	public:
		/**
		 * Will set the pin for the test.
		 * @param pin
		 */
		PiezoBuzzerTest(int pin) {
			this->buzzerPin = pin;
			pinMode(this->buzzerPin, OUTPUT);
		}

		/**
		 * Will output at a frequenzy in the range of 100Hz to 5kHz over 5 steps
		 * with each sounding for a duraton of half a second.
		 */
		void executeTest() {
			for (int i = 0; i < 5; i++) {
				int frequency = (int) (100 * pow(2, (i + 1) * 5.6439 / 5));
				tone(buzzerPin, frequency);
				delay(500);
			}
			noTone(buzzerPin);
		}
};

#endif
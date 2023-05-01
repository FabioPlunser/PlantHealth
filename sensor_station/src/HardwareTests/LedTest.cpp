#ifndef LED_TEST_CLASS
#define LED_TEST_CLASS

#include <Arduino.h>
#include <Defines.h>
#include <math.h>

class LedTest {
	private:
		struct {
				int red, green, blue;
		} pins;

	public:
		/**
		 * Will setup the class to execute a test on the led with the provided
		 * pins.
		 * @param redPin
		 * @param greenPin
		 * @param bluePin
		 */
		LedTest(int redPin, int greenPin, int bluePin) {
			this->pins.red	 = redPin;
			this->pins.green = greenPin;
			this->pins.blue	 = bluePin;
			pinMode(redPin, OUTPUT);
			pinMode(greenPin, OUTPUT);
			pinMode(bluePin, OUTPUT);
		}

		/**
		 * Will first show the colors in the order red -> green -> blue.
		 * Afterwards it will cycle through the colors with sine curves.
		 */
		void executeTest() {
			analogWrite(this->pins.red, 0);
			analogWrite(this->pins.green, 0);
			analogWrite(this->pins.blue, 0);

			// Increase and decrease color red
			for (int i = 0; i < 256; i++) {
				analogWrite(this->pins.red, i);
				delay(2);
			}
			for (int i = 255; i >= 0; i--) {
				analogWrite(this->pins.red, i);
				delay(2);
			}
			// Increase and decrease color green
			for (int i = 0; i < 256; i++) {
				analogWrite(this->pins.green, i);
				delay(2);
			}
			for (int i = 255; i >= 0; i--) {
				analogWrite(this->pins.green, i);
				delay(2);
			}
			// Increase and decrease color blue
			for (int i = 0; i < 256; i++) {
				analogWrite(this->pins.blue, i);
				delay(2);
			}
			for (int i = 255; i >= 0; i--) {
				analogWrite(this->pins.blue, i);
				delay(2);
			}
			// Go through the values with a sinus curve
			for (int j = 0; j < 3; j++) {
				for (int i = 0; i < 256; i++) {
					float normI		 = ((float) i) / 256 * 2 * PI;
					float valueRed	 = sin(normI);
					float valueGreen = sin(normI + 2 * PI / 3);
					float valueBlue	 = sin(normI - 2 * PI / 3);
					analogWrite(
						this->pins.red, (int) ((valueRed + 1) / 2 * 255)
					);
					analogWrite(
						this->pins.green, (int) ((valueGreen + 1) / 2 * 255)
					);
					analogWrite(
						this->pins.blue, (int) ((valueBlue + 1) / 2 * 255)
					);
					delay(10);
				}
			}
			analogWrite(this->pins.red, 0);
			analogWrite(this->pins.green, 0);
			analogWrite(this->pins.blue, 0);
		}
};

#endif
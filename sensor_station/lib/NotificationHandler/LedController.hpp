#ifndef ERRPR_LED_CLASS
#define ERRPR_LED_CLASS

#include <Arduino.h>
#include <list>
#include <queue>
#include <stdlib.h>

class LedHandler {
	private:
		// Variables for the pins the leds ar connected to
		uint8_t pinRed;
		uint8_t pinGreen;
		uint8_t pinBlue;

		// Variables for the current color of the led
		uint8_t valueRed;
		uint8_t valueGreen;
		uint8_t valueBlue;

		// Arrays with the duration on and off for the led to iterate over
		uint16_t * durationOn  = NULL;
		uint16_t * durationOff = NULL;
		uint8_t durationSize;
		// Bool to indicate if the led should loop over the duration arrays
		bool loopError;

		// Current index in the duration arrays
		uint8_t durationIdx			 = 0;
		unsigned long prevChangeTime = 0;
		bool isOn					 = false;
		// Bool to indicate if the led did execute all the durations if not
		// looping
		bool isFinished				 = true;
		// Bool to indicate if the handler should loop through the duration
		// arrays and enable the led
		bool isEnabled				 = true;

		/**
		 * @param pinRed The pin the red led is connected to
		 * @param pinGreen The pin the green led is connected to
		 * @param pinBlue The pin the blue led is connected to
		 */
		LedHandler(uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue) {
			DEBUG_PRINT_POS(4, "\n");
			this->pinRed   = pinRed;
			this->pinGreen = pinGreen;
			this->pinBlue  = pinBlue;
			pinMode(pinRed, OUTPUT);
			pinMode(pinGreen, OUTPUT);
			pinMode(pinBlue, OUTPUT);
			setLEDStatus(true);
		}

		~LedHandler() {
			DEBUG_PRINT_POS(4, "\n");
			if (this->durationOn != NULL) {
				free(this->durationOn);
				free(this->durationOff);
			}
		}

	public:
		LedHandler & operator=(LedHandler &) = delete;
		LedHandler(LedHandler &)			 = delete;

		// Get singelton instance of the class
		static LedHandler *
		getLedHandler(uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue) {
			DEBUG_PRINT_POS(4, "\n");
			static LedHandler ledHandler(pinRed, pinGreen, pinBlue);
			return &ledHandler;
		}

	private:
		// Switch the led on or off
		void setLEDStatus(bool on) {
			DEBUG_PRINT_POS(4, "\n");
			if (on == this->isOn) {
				return;
			}
			this->isOn			 = on;
			this->prevChangeTime = millis();
			if (isOn) {
				analogWrite(this->pinRed, this->valueRed);
				analogWrite(this->pinGreen, this->valueGreen);
				analogWrite(this->pinBlue, this->valueBlue);
			} else {
				analogWrite(this->pinRed, 0);
				analogWrite(this->pinGreen, 0);
				analogWrite(this->pinBlue, 0);
			}
		}

		// Switch the current state of the led
		void toggleLEDStatus() {
			DEBUG_PRINT_POS(4, "\n");
			setLEDStatus(!this->isOn);
		}

		/**
		 * @returns 0 if the led is not enabled or the time till the next change
		 */
		uint16_t getMsTillNext() {
			DEBUG_PRINT_POS(4, "\n");
			if (durationIdx >= durationSize) {
				DEBUG_PRINTF_POS(
					3, "Overflow, duration Idx = %d, max was %d\n", durationIdx,
					durationSize
				);
				return 0;
			}
			// Get the array that is currently in use
			uint16_t * timeArray	 = isOn ? durationOn : durationOff;
			unsigned long passedTime = millis() - prevChangeTime;
			DEBUG_PRINTF_POS(3, "Passed time = %lu.\n", passedTime);
			if (passedTime > timeArray[durationIdx]) {
				return 0;
			}
			DEBUG_PRINTF_POS(
				3, "Time till next is %lu\n",
				timeArray[durationIdx] - passedTime
			);
			return timeArray[durationIdx] - passedTime;
		}

	public:
		/**
		 * Enables the LedController. Will pick up where it stopped in the
		 * duration arrays.
		 * @returns the time till the next change
		 */
		uint16_t enable() {
			DEBUG_PRINT_POS(4, "\n");
			this->isEnabled = true;
			return updateLEDStatus();
		}

		/**
		 * Will disable the led controller without resetting the duration
		 * arrays. More like a pause
		 */
		void disable() {
			DEBUG_PRINT_POS(4, "\n");
			this->isEnabled = false;
			setLEDStatus(false);
		}

		/**
		 * This function will disable the led until the next call that will
		 * modify the led status. (Updating will resume the led controller)
		 */
		void silence() {
			DEBUG_PRINT_POS(4, "\n");
			setLEDStatus(false);
		}

		/**
		 * Define the properties of the currently displayed error.
		 * @param valueRed The value for the red led (0-255)
		 * @param valueGreen The value for the green led (0-255)
		 * @param valueBlue The value for the blue led (0-255)
		 * @param durationOn An array of durations the led should be on
		 * @param durationOff An array of durations the led should be off (same
		 * size as durationOn)
		 * @param durationSize The size of the duration arrays
		 * @param loopError If true the duration arrays will be looped through
		 */
		void setErrorProperties(
			uint8_t valueRed, uint8_t valueGreen, uint8_t valueBlue,
			uint16_t * durationOn, uint16_t * durationOff, uint8_t durationSize,
			bool loopError = false
		) {
			DEBUG_PRINT_POS(4, "\n");
			if (this->durationOn != NULL) {
				free(this->durationOn);
				free(this->durationOff);
			}
			// Create a copy of the duration arrays
			this->durationOn =
				(uint16_t *) malloc(sizeof(uint16_t) * durationSize);
			this->durationOff =
				(uint16_t *) malloc(sizeof(uint16_t) * durationSize);
			for (int i = 0; i < durationSize; i++) {
				this->durationOn[i]	 = durationOn[i];
				this->durationOff[i] = durationOff[i];
			}
			this->durationSize = durationSize;
			this->valueRed	   = valueRed;
			this->valueGreen   = valueGreen;
			this->valueBlue	   = valueBlue;
			this->loopError	   = loopError;
			this->isFinished   = false;
			this->durationIdx  = 0;
			enable();
			setLEDStatus(true);
		}

		/**
		 * This function will update the led status if the led is enabled.
		 * If the controller is silenced this will resume the current error.
		 * @returns The time till the next call is needed
		 */
		uint16_t updateLEDStatus() {
			DEBUG_PRINT_POS(4, "\n");
			if (isFinished || !isEnabled) {
				DEBUG_PRINTF_POS(
					3, "Wait time was 0. isFinished = %d, isEnabled = %d.\n",
					isFinished, isEnabled
				);
				return 0;
			}
			// Would signify that we went through the whole array.
			// Will reset if in loop mode or stop the led otherwise.
			if (durationIdx >= durationSize) {
				DEBUG_PRINT_POS(3, "Index overflow.\n");
				if (!loopError) {
					DEBUG_PRINT_POS(3, "Led gets stopped.\n");
					setLEDStatus(false);
					this->isFinished = true;
					return 0;
				}
				this->durationIdx = 0;
			}
			uint16_t remainingTime = getMsTillNext();
			DEBUG_PRINTF_POS(3, "Remaining time = %u\n", remainingTime);
			// It it is not time to switch yet, return the remaining time
			if (remainingTime > 0) {
				DEBUG_PRINT_POS(3, "Return time.\n");
				return remainingTime;
			}
			toggleLEDStatus();
			DEBUG_PRINT_POS(3, "Led got toggled.\n");
			// Only advance the index if the led is on, since we will use the
			// off array at the same index first
			if (this->isOn) {
				durationIdx++;
				DEBUG_PRINT_POS(3, "Index got moved.\n");
			}
			uint16_t timeTillNext = getMsTillNext();
			DEBUG_PRINTF_POS(3, "Time till next change is %u.\n", timeTillNext);
			return timeTillNext;
		}
};

#endif
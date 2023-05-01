#ifndef ERRPR_LED_CLASS
#define ERRPR_LED_CLASS

#include <Arduino.h>
#include <list>
#include <queue>
#include <stdlib.h>

class LedHandler {
	private:
		uint8_t pinRed;
		uint8_t pinGreen;
		uint8_t pinBlue;

		uint8_t valueRed;
		uint8_t valueGreen;
		uint8_t valueBlue;

		uint16_t * durationOn  = NULL;
		uint16_t * durationOff = NULL;
		uint8_t durationSize;
		bool loopError;

		uint8_t durationIdx			 = 0;
		unsigned long prevChangeTime = 0;
		bool isOn					 = false;
		bool isFinished				 = true;
		bool isEnabled				 = true;

		LedHandler(uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue) {
			this->pinRed   = pinRed;
			this->pinGreen = pinGreen;
			this->pinBlue  = pinBlue;
			pinMode(pinRed, OUTPUT);
			pinMode(pinGreen, OUTPUT);
			pinMode(pinBlue, OUTPUT);
			setLEDStatus(true);
		}

		~LedHandler() {
			if (this->durationOn != NULL) {
				free(this->durationOn);
				free(this->durationOff);
			}
		}

	public:
		LedHandler & operator=(LedHandler &) = delete;
		LedHandler(LedHandler &)			 = delete;

		static LedHandler *
		getLedHandler(uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue) {
			static LedHandler ledHandler(pinRed, pinGreen, pinBlue);
			return &ledHandler;
		}

	private:
		void setLEDStatus(bool on) {
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

		void toggleLEDStatus() { setLEDStatus(!this->isOn); }

		uint16_t getMsTillNext() {
			if (durationIdx >= durationSize) {
				return 0;
			}
			uint16_t * timeArray	 = isOn ? durationOn : durationOff;
			unsigned long passedTime = millis();
			if (passedTime > timeArray[durationIdx]) {
				return 0;
			}
			return timeArray[durationIdx] - passedTime;
		}

	public:
		uint16_t enable() {
			this->isEnabled = true;
			return updateLEDStatus();
		}

		void disable() {
			this->isEnabled = false;
			setLEDStatus(false);
		}

		void setErrorProperties(
			uint8_t valueRed, uint8_t valueGreen, uint8_t valueBlue,
			uint16_t * durationOn, uint16_t * durationOff, uint8_t durationSize,
			bool loopError = false
		) {
			if (this->durationOn != NULL) {
				free(this->durationOn);
				free(this->durationOff);
			}
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
			setLEDStatus(true);
		}

		uint16_t updateLEDStatus() {
			if (isFinished || !isEnabled) {
				return 0;
			}
			uint16_t remainingTime = getMsTillNext();
			if (remainingTime > 0) {
				return remainingTime;
			}
			if (durationIdx >= durationSize) {
				if (!loopError) {
					setLEDStatus(false);
					this->isFinished = true;
					return 0;
				}
				this->durationIdx = 0;
			}
			toggleLEDStatus();
			if (this->isOn) {
				durationIdx++;
			}
			return getMsTillNext();
		}
};

#endif
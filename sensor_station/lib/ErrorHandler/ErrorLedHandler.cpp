#ifndef ERRPR_LED_CLASS
#define ERRPR_LED_CLASS

#include <Arduino.h>
#include <list>
#include <queue>
#include <stdlib.h>

class ErrorLedHandler {
	private:
		uint8_t pinRed;
		uint8_t pinGreen;
		uint8_t pinBlue;

		uint8_t valueRed;
		uint8_t valueGreen;
		uint8_t valueBlue;

		uint16_t * durationOn;
		uint16_t * durationOff;
		uint8_t durationSize;
		bool loopError;

		uint8_t durationIdx			 = 0;
		unsigned long prevChangeTime = 0;
		bool isOn					 = false;
		bool isFinished				 = true;

		ErrorLedHandler(uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue) {
			this->pinRed   = pinRed;
			this->pinGreen = pinGreen;
			this->pinBlue  = pinBlue;
			pinMode(pinRed, OUTPUT);
			pinMode(pinGreen, OUTPUT);
			pinMode(pinBlue, OUTPUT);
			setLEDStatus(true);
		}

	public:
		ErrorLedHandler & operator=(ErrorLedHandler &) = delete;
		ErrorLedHandler(ErrorLedHandler &)			   = delete;

		static ErrorLedHandler & getErrorOutput(

			uint8_t pinRed, uint8_t pinGreen, uint8_t pinBlue
		) {
			static ErrorLedHandler errorClass(pinRed, pinGreen, pinBlue);
			return errorClass;
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

	public:
		enum SetVisulisationError {
			ValueRedError,
			ValueGreenError,
			ValueBlueError,
			DurationSizeError,
			Nothing
		};
		SetVisulisationError
		setErrorVisualisation(uint8_t valueRed, uint8_t valueGreen,
							  uint8_t valueBlue, uint16_t * durationOn,
							  uint16_t * durationOff, uint8_t durationSize,
							  bool loopError = false;) {
			if (valueRed > 0xFF || valueRed < 0) {
				return SetVisulisationError::ValueRedError;
			}
			if (valueGreen > 0xFF || valueGreen < 0) {
				return SetVisulisationError::ValueGreenError;
			}
			if (valueBlue > 0xFF || valueBlue < 0) {
				return SetVisulisationError::ValueBlueError;
			}
			if (durationSize <= 0) {
				return SetVisulisationError::DurationSizeError;
			}
			this->durationOn   = durationOn;
			this->durationOff  = durationOff;
			this->durationSize = durationSize;
			this->valueRed	   = valueRed;
			this->valueGreen   = valueGreen;
			this->valueBlue	   = valueBlue;
			this->loopError	   = loopError;
			this->isFinished   = false;
			this->durationIdx  = 0;
			setLEDStatus(true);
			return SetVisulisationError::Nothing;
		}

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

		uint16_t updateLEDStatus() {
			if (isFinished) {
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
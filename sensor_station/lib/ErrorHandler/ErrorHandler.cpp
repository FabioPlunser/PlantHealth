#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include "ErrorLedHandler.cpp"
#include "ErrorQueue.cpp"
#include "SensorError.cpp"
#include "SensorErrors.h"

class ErrorHandler {
	private:
		ErrorQueue errorQueue;
		ErrorLedHandler ledHandler;
		ErrorHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {}

	public:
		static ErrorHandler & getErrorHandler
}

#endif
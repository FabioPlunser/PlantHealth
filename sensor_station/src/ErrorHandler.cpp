#include <functional>
#include <stdlib.h>
// #include <iostream>
#include <queue>
#include <string_view>
// #include <vector>

enum ErrorType {
	SoilHumidity,
	LightIntensity,
	AirHumidity,
	AirTemperature,
	AirQuality,
	AirPressure
};

enum ErrorHighLow { High, Low };

class Error {
	private:
		ErrorType type;
		ErrorHighLow dir;
		uint8_t priority;

	public:
		bool operator<(const Error & other) const {
			if (this == &other) {
				return false;
			}
			return this->getPriority() < other.getPriority();
		}

		uint8_t getPriority() const { return this->priority; }
};

class ErrorHandlerClass {
	private:
		std::priority_queue<Error> queue;

	public:
		bool addError(Error error) {
			queue.push(error);
			return true;
		}
};
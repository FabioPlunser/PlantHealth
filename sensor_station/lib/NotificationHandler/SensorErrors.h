#ifndef SENSOR_ERRORS_HEADER
#define SENSOR_ERRORS_HEADER

class SensorErrors {
	public:
		enum Type {
			SoilHumidityError,
			LightIntensityError,
			AirHumidityError,
			AirTemperatureError,
			AirQualityError,
			AirPressureError,
		};

		enum Status { High, Low };
};

#endif
#ifndef AIR_SENSOR_TEST_CLASS
#define AIR_SENSOR_TEST_CLASS

#include <Adafruit_BME680.h>
#include <Arduino.h>
#include <Defines.h>

#define SET_IF_NOT_NULL(x, y) \
	if (x != NULL) {          \
		*x = y;               \
	}

class AirSensorClass {
	private:
		Adafruit_BME680 * bme680;
		bool initialized;

	public:
		enum UPDATE_ERROR {
			NOTHING			= 0,
			TO_EARLY		= 1,
			NOT_INIT		= 2,
			LOST_CONNECTION = 3
		};

	private:
		/**
		 * Will perform a reading on the sensor and update the values, if
		 * possible. If not init yet, it will try to init the sensor.
		 * @return Will set error of type UPDATE_ERROR:\
		 * @return NOTHING  -> Success\
		 * @return TO_EARLY -> Values might not be updated\
		 * @return NOT_INIT -> Sensor was not initialized successfully\
		 * @return LOST_CONNECTION -> Connection lost after a successful read
		 * was performed.
		 */
		UPDATE_ERROR tryUpdateValues() {
			static unsigned long nextUpdate = millis();
			if (nextUpdate > millis()) {
				return UPDATE_ERROR::TO_EARLY;
			} else {
				if (!this->initialized) {
					this->initialized = bme680->begin();
					if (!this->initialized) {
						nextUpdate = millis() + UPDATE_INTERVAL_BME680_MS_MAX;
						return UPDATE_ERROR::NOT_INIT;
					}
				}
				if (!bme680->performReading()) {
					this->initialized = false;
					return UPDATE_ERROR::LOST_CONNECTION;
				}
				nextUpdate = millis() + UPDATE_INTERVAL_BME680_MS_MAX;
				return UPDATE_ERROR::NOTHING;
			}
		}

	public:
		AirSensorClass(Adafruit_BME680 * providedBle680) {
			this->bme680	  = providedBle680;
			this->initialized = false;
			if (this->bme680) {
				this->initialized = bme680->begin();
			}
		}

		~AirSensorClass() { delete (this->bme680); }

		/**
		 * Set the values in the provided parameters to the measured values.
		 * If any value stays NULL, it will not be updated.
		 * @return Will set error of type UPDATE_ERROR:
		 * @return NOTHING  -> Success
		 * @return TO_EARLY -> Values might not be updated
		 * @return NOT_INIT -> Sensor was not initialized successfully
		 * @return LOST_CONNECTION -> Connection lost after a successful read
		 * was performed.
		 */
		UPDATE_ERROR getMeasuredValues(
			float * pressure = NULL, uint32_t * gas_resistance = NULL,
			float * temperature = NULL, float * humidity = NULL
		) {
			UPDATE_ERROR updateError = tryUpdateValues();
			if (updateError == UPDATE_ERROR::NOT_INIT) {
				this->initialized = bme680->begin();
				if (this->initialized == false) {
					return updateError;
				}
			}
			SET_IF_NOT_NULL(pressure, bme680->readPressure());
			SET_IF_NOT_NULL(gas_resistance, bme680->readGas());
			SET_IF_NOT_NULL(temperature, bme680->readTemperature());
			SET_IF_NOT_NULL(humidity, bme680->readHumidity());
			return UPDATE_ERROR::NOTHING;
		}

		bool isInitSuccessful() { return this->initialized; }
};

#endif
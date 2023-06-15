#ifndef SENSOR_VALUE_HANDLER_CLASS
#define SENSOR_VALUE_HANDLER_CLASS

#include "AirSensor.cpp"
#include "DipSwitch.cpp"
#include "Hydrometer.cpp"
#include "Phototransistor.cpp"
#include "ValueAccumulator.hpp"

#include <CompilerFunctions.hpp>
#include <Defines.h>
#include <modules/communication.h>

/**
 * Class to handle the values of the AirSensor, Hydrometer and Phototransistor.
 * Provides functionality to read values and calculate averages.
 */
class SensorValueHandlerClass {
		// ---------- Variables ---------- //
	private:
		ValueAccumulatorClass * airTemperatureAccumulator;
		ValueAccumulatorClass * airHumidityAccumulator;
		ValueAccumulatorClass * airPressureAccumulator;
		ValueAccumulatorClass * airQualityAccumulator;
		ValueAccumulatorClass * soilHumidityAccumulator;
		ValueAccumulatorClass * luminosityAccumulator;
		AirSensorClass * airSensor;
		HydrometerClass * hydrometer;
		PhototransistorClass * phototransistor;

		// ---------- Constructors ---------- //
	private:
		SensorValueHandlerClass(
			AirSensorClass * airSensor, HydrometerClass * hydrometer,
			PhototransistorClass * phototransistor
		) {
			DEBUG_PRINT_POS(4, "\n");
			airTemperatureAccumulator = new ValueAccumulatorClass();
			airHumidityAccumulator	  = new ValueAccumulatorClass();
			airPressureAccumulator	  = new ValueAccumulatorClass();
			airQualityAccumulator	  = new ValueAccumulatorClass();
			soilHumidityAccumulator	  = new ValueAccumulatorClass();
			luminosityAccumulator	  = new ValueAccumulatorClass();
			this->airSensor			  = airSensor;
			this->hydrometer		  = hydrometer;
			this->phototransistor	  = phototransistor;
		}

		~SensorValueHandlerClass() {
			DEBUG_PRINT_POS(4, "\n");
			delete (airTemperatureAccumulator);
			delete (airHumidityAccumulator);
			delete (airPressureAccumulator);
			delete (airQualityAccumulator);
			delete (soilHumidityAccumulator);
			delete (luminosityAccumulator);
		}

	public:
		/**
		 * Set the function to calculate the weight of a sensor value.
		 * If no function is set, or the provided function is NULL, the default
		 * weight function from the ValueAccumulatorClass will be used.
		 */
		void setWeightCalculatorFunction(double (*weightFunction
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset)) {
			DEBUG_PRINT_POS(4, "\n");
			airTemperatureAccumulator->setWeightFunction(weightFunction);
			airHumidityAccumulator->setWeightFunction(weightFunction);
			airPressureAccumulator->setWeightFunction(weightFunction);
			airQualityAccumulator->setWeightFunction(weightFunction);
			soilHumidityAccumulator->setWeightFunction(weightFunction);
			luminosityAccumulator->setWeightFunction(weightFunction);
		}

		// Get the singelton instance of this class.
		static SensorValueHandlerClass * getInstance(
			AirSensorClass * airSensor, HydrometerClass * hydrometer,
			PhototransistorClass * phototransistor
		) {
			DEBUG_PRINT_POS(4, "\n");
			assert(airSensor != NULL);
			assert(hydrometer != NULL);
			assert(phototransistor != NULL);
			static SensorValueHandlerClass instance(
				airSensor, hydrometer, phototransistor
			);
			return &instance;
		}

		/**
		 * This function sets the current sensor values in the provided struct.
		 * If an update error occures from the AirSensor, the values will be set
		 * to the GATT "not known" values.
		 * @param str Pointer to the struct to set the values in.
		 * @return UPDATE_ERROR::NOTHING if no error occured, otherwise the
		 * 	   error code. See AirSensorClass::UPDATE_ERROR enum for more
		 * information.
		 */
		AirSensorClass::UPDATE_ERROR
		setSensorValuesFromSensors(sensor_data_t * str) {
			DEBUG_PRINT_POS(4, "\n");
			float pressure = 0, temperature = 0, humidity = 0;
			uint32_t gas_resistance	 = 0;
			uint16_t earth_humidity	 = hydrometer->getHumidity_16bit();
			uint16_t light_intensity = phototransistor->getLighting_16bit();

			AirSensorClass::UPDATE_ERROR updateError =
				airSensor->getMeasuredValues(
					&pressure, &gas_resistance, &temperature, &humidity
				);
			DEBUG_PRINT(3, "Sensor Values are:\n");
			DEBUG_PRINTF(3, "\tPressure: %f\n", pressure);
			DEBUG_PRINTF(3, "\tGas Resistance: %lu\n", gas_resistance);
			DEBUG_PRINTF(3, "\tTemperature: %f\n", temperature);
			DEBUG_PRINTF(3, "\tAir Humidity: %f\n", humidity);
			DEBUG_PRINTF(3, "\tEarth Humidity: %u\n", earth_humidity);
			DEBUG_PRINTF(3, "\tLight Intensity: %u\n", light_intensity);

			if (updateError == AirSensorClass::UPDATE_ERROR::NOTHING) {
				str->air_humidity = convertToGATT_airHumidity(humidity);
				str->air_quality  = convertToGATT_airQuality(gas_resistance);
				str->temperature  = convertToGATT_airTemperature(temperature);
				str->air_pressure = convertToGATT_airPressure(pressure);
			} else {
				str->air_humidity = convertToGATT_airHumidity_notKnown();
				str->air_quality  = convertToGATT_airQuality_notKnown();
				str->temperature  = convertToGATT_airTemperature_notKnown();
				str->air_pressure = convertToGATT_airPressure_notKnown();
			}
			str->earth_humidity = convertToGATT_soilHumidity(earth_humidity);
			str->light_intensity =
				convertToGATT_lightIntensity(light_intensity);
			return updateError;
		};

		// ---------- Functions ---------- //
	private:
		/**
		 * @returns The converted value according to the GATT humidity value.
		 */
		uint16_t convertToGATT_soilHumidity(uint16_t humidity) {
			DEBUG_PRINT_POS(4, "\n");
			DEBUG_PRINTF_POS(2, "Soil humidity = %u\n", humidity);
			// Calibration values
			static uint16_t valueHigh = 300;
			static uint16_t valueLow  = 950;
			float calculation		  = 100 - ((humidity - valueHigh) /
									   (float) (valueLow - valueHigh) * 100);

			DEBUG_PRINTF_POS(2, "Calculated = %lf\n", calculation);
			if (calculation < 0 || calculation > 100) {
				return convertToGATT_soilHumidity_notKnown();
			}
			return uint16_t(calculation * 100);
		}
		/**
		 * @returns The GATT humidity value for "not known".
		 */
		uint16_t convertToGATT_soilHumidity_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint16_t) 0xFFFF;
		}

		/**
		 * @returns The converted value according to the GATT air humidity
		 * value.
		 */
		uint16_t convertToGATT_airHumidity(float humidity) {
			DEBUG_PRINT_POS(4, "\n");
			if (humidity < 0 || humidity > 100) {
				return convertToGATT_airHumidity_notKnown();
			}
			return uint16_t(humidity * 100);
		}
		/**
		 * @returns The GATT air humidity value for "not known".
		 */
		uint16_t convertToGATT_airHumidity_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint16_t) 0xFFFF;
		}

		/**
		 * @returns The converted value according to the GATT air pressure
		 * value.
		 */
		uint32_t convertToGATT_airPressure(float pressure) {
			DEBUG_PRINT_POS(4, "\n");
			return uint32_t(pressure * 10);
		}
		/**
		 * @returns The GATT air pressure value for "not known".
		 */
		uint32_t convertToGATT_airPressure_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint8_t) 0xFFFF'FFFF;
		}

		/**
		 * @returns The converted value according to the GATT air quality
		 * value.
		 */
		uint8_t convertToGATT_airQuality(float gas_resistance) {
			DEBUG_PRINT_POS(4, "\n");
			DEBUG_PRINTF_POS(2, "Quality is %f\n", gas_resistance);
			const float calibrationValue = 250'000;
			return (uint8_t) (100 - (gas_resistance / calibrationValue) * 100);
		}
		/**
		 * @returns The GATT air quality value for "not known".
		 */
		uint8_t convertToGATT_airQuality_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint8_t) 0xFF;
		}

		/**
		 * @returns The converted value according to the GATT air temperature
		 * value.
		 */
		uint8_t convertToGATT_airTemperature(float temperature) {
			DEBUG_PRINT_POS(4, "\n");
			if (temperature < -64 || temperature > 63) {
				return convertToGATT_airTemperature_notKnown();
			}
			return (uint8_t) (temperature * 2);
		}
		/**
		 * @returns The GATT air temperature value for "not known".
		 */
		uint8_t convertToGATT_airTemperature_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint8_t) 0x7F;
		}

		/**
		 * @returns The converted value according to the GATT light intensity
		 * value.
		 */
		uint16_t convertToGATT_lightIntensity(float lightIntensity) {
			DEBUG_PRINT_POS(4, "\n");
			uint16_t luminosity =
				luminosityFromVoltage((uint16_t) lightIntensity);
			if (luminosity > 65534) {
				return convertToGATT_lightIntensity_notKnown();
			}
			return luminosity;
		}
		/**
		 * @returns The GATT light intensity value for "not known".
		 */
		uint16_t convertToGATT_lightIntensity_notKnown() {
			DEBUG_PRINT_POS(4, "\n");
			return (uint16_t) 0xFFFF;
		}

		/**
		 * @returns The luminoisty in lumen from the Voltage.
		 */
		uint16_t luminosityFromVoltage(uint16_t measured) {
			DEBUG_PRINT_POS(4, "\n");
			const float R	= 2200;
			const float Vin = 3.3;
			float Vout =
				float(measured) / 1023 * Vin; // Convert analog to voltage
			float diodaResistance =
				(R * (Vin - Vout)) / Vout; // Convert voltage to resistance
			return 500 / (diodaResistance / 1000
						 ); // Convert resitance to kOhm and afterwards to lumen
							// TODO: Check 500 as max luminosity
		}

	public:
		/**
		 * Gets the current sensor value of the sensors and adds them to the
		 * accumulators. Will clear the sensor data read flag so signal that
		 * sensor values for reding are available.
		 */
		void addSensorValuesToAccumulator() {
			DEBUG_PRINT_POS(4, "\n");
			float airPressure = 0, airTemperature = 0, airHumidity = 0;
			uint32_t airGasResistance = 0;
			uint16_t soilhHumidity	  = hydrometer->getHumidity_16bit();
			uint16_t lightIntensity	  = phototransistor->getLighting_16bit();

			AirSensorClass::UPDATE_ERROR updateError =
				airSensor->getMeasuredValues(
					&airPressure, &airGasResistance, &airTemperature,
					&airHumidity
				);

			DEBUG_PRINT(3, "Sensor Values are:\n");
			if (updateError != AirSensorClass::UPDATE_ERROR::LOST_CONNECTION &&
				updateError != AirSensorClass::UPDATE_ERROR::NOT_INIT) {
				airTemperatureAccumulator->addValue(airTemperature);
				airHumidityAccumulator->addValue(airHumidity);
				airPressureAccumulator->addValue(airPressure);
				airQualityAccumulator->addValue(airGasResistance);
				DEBUG_PRINTF(3, "\tPressure: %f\n", airPressure);
				DEBUG_PRINTF(3, "\tGas Resistance: %lu\n", airGasResistance);
				DEBUG_PRINTF(3, "\tTemperature: %f\n", airTemperature);
				DEBUG_PRINTF(3, "\tAir Humidity: %f\n", airHumidity);
			} else {
				DEBUG_PRINTF_POS(
					1, "Encountered Update Error. Error was %d\n", updateError
				);
			}
			DEBUG_PRINTF(3, "\tEarth Humidity: %u\n", soilhHumidity);
			DEBUG_PRINTF(3, "\tLight Intensity: %u\n", lightIntensity);
			soilHumidityAccumulator->addValue(soilhHumidity);
			luminosityAccumulator->addValue(lightIntensity);
			clear_sensor_data_read_flag();
		};

		/**
		 * Will read the sensor values and set them in the BLE characteristics.
		 * Resets the accumulators afterwards.
		 * @returns True if the sensor values were set successfully. Else False
		 */
		bool setAccumulatedSensorValuesInBle() {
			DEBUG_PRINT_POS(4, "\n");
			if (soilHumidityAccumulator->getTotalWeight() <= 0) {
				return false;
			}
			clearAllFlags();
			sensor_data_t sensorData = {
				.earth_humidity = convertToGATT_soilHumidity(
					soilHumidityAccumulator->getNormalizedValue()
				),
				.air_humidity =
					airHumidityAccumulator->getTotalWeight() == 0
						? convertToGATT_airHumidity_notKnown()
						: convertToGATT_airHumidity(
							  airHumidityAccumulator->getNormalizedValue()
						  ),
				.air_pressure =
					airHumidityAccumulator->getTotalWeight() == 0
						? convertToGATT_airPressure_notKnown()
						: convertToGATT_airPressure(
							  airPressureAccumulator->getNormalizedValue()
						  ),
				.air_quality =
					airHumidityAccumulator->getTotalWeight() == 0
						? convertToGATT_airQuality_notKnown()
						: convertToGATT_airQuality(
							  airQualityAccumulator->getNormalizedValue()
						  ),
				.temperature =
					airHumidityAccumulator->getTotalWeight() == 0
						? convertToGATT_airTemperature_notKnown()
						: convertToGATT_airTemperature(
							  airTemperatureAccumulator->getNormalizedValue()
						  ),
				.light_intensity = convertToGATT_lightIntensity(
					luminosityAccumulator->getNormalizedValue()
				)};
			airHumidityAccumulator->reset();
			airPressureAccumulator->reset();
			airQualityAccumulator->reset();
			airTemperatureAccumulator->reset();
			soilHumidityAccumulator->reset();
			luminosityAccumulator->reset();
			set_sensor_data(sensorData);
			return true;
		}

		void resetAccumulators() {
			airHumidityAccumulator->reset();
			airPressureAccumulator->reset();
			airQualityAccumulator->reset();
			airTemperatureAccumulator->reset();
			soilHumidityAccumulator->reset();
			luminosityAccumulator->reset();
		}
};

#endif
#ifndef SENSOR_MEASUREMENTS_CLASS
#define SENSOR_MEASUREMENTS_CLASS

#include <CompilerFunctions.hpp>
#include <cstdlib>

class ValueAccumulatorClass {
		// ---------- Variables ---------- //
	private:
		double accumulatedWeightedValue = 0;
		double totalWeights				= 0;
		unsigned long lastUpdate;
		unsigned long lastReset;
		double (*calculateWheight
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset);

		// ---------- Constructors ---------- //
	public:
		ValueAccumulatorClass() {
			this->calculateWheight = NULL;
			reset();
		}
		ValueAccumulatorClass(double (*calculateWheight
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset))
			: ValueAccumulatorClass() {
			this->calculateWheight = calculateWheight;
		}

		// ---------- Functions ---------- //

	private:
		/**
		 * Calculates the weight factor, if a function is present. Else the
		 * weigth will always be 1.
		 */
		double getWeightFactor(unsigned long timeOfMeasurement) {
			return this->calculateWheight == NULL
					   ? 1
					   : calculateWheight(
							 timeOfMeasurement, lastUpdate, lastReset
						 );
		}

	public:
		/**
		 * Set the function to calculate the weight factor. If set to NULL the
		 * weight will always be 1.
		 */
		void setWeightFunction(double (*calculateWheight
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset)) {
			this->calculateWheight = calculateWheight;
		}

		/**
		 * Reset the accumulated value and the total weights.
		 * The last reset and last update time will be set to the current time.
		 */
		void reset() {
			DEBUG_PRINT_POS(4, "\n");
			accumulatedWeightedValue = 0;
			totalWeights			 = 0;
			lastReset				 = millis();
			lastUpdate				 = lastReset;
		}

		/**
		 * Add a value to the accumulated value. The value will be weighted by
		 * the factor from the weight function.
		 */
		void addValue(double value) {
			DEBUG_PRINT_POS(4, "\n");
			unsigned long timeStamp	 = millis();
			double weight			 = getWeightFactor(timeStamp);
			lastUpdate				 = timeStamp;
			accumulatedWeightedValue += value * weight;
			totalWeights			 += weight;
		}

		/**
		 * Get the total weight of all values added since the last reset.
		 */
		double getTotalWeight() { return this->totalWeights; }

		/**
		 * Get the accumulated value since the last reset. (sum of value *
		 * weight)
		 */
		double getAccumulatedWeightedValue() {
			return this->accumulatedWeightedValue;
		}

		/**
		 * Get the normalized value since the last reset. Represents the
		 * weighted average of all values added since the last reset.
		 */
		double getNormalizedValue() {
			return getAccumulatedWeightedValue() / getTotalWeight();
		}
};

#endif

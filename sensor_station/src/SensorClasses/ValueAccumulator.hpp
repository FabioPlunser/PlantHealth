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
		double getWeightFactor(unsigned long timeOfMeasurement) {
			return this->calculateWheight == NULL
					   ? 1
					   : calculateWheight(
							 timeOfMeasurement, lastUpdate, lastReset
						 );
		}

	public:
		void setWeightFunction(double (*calculateWheight
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset)) {
			this->calculateWheight = calculateWheight;
		}

		void reset() {
			DEBUG_PRINT_POS(4, "\n");
			accumulatedWeightedValue = 0;
			totalWeights			 = 0;
			lastReset				 = millis();
			lastUpdate				 = lastReset;
		}

		void addValue(double value) {
			DEBUG_PRINT_POS(4, "\n");
			unsigned long timeStamp	 = millis();
			double weight			 = getWeightFactor(timeStamp);
			lastUpdate				 = timeStamp;
			accumulatedWeightedValue += value * weight;
			totalWeights			 += weight;
		}

		double getTotalWeight() { return this->totalWeights; }

		double getAccumulatedWeightedValue() {
			return this->accumulatedWeightedValue;
		}

		double getNormalizedValue() {
			return getAccumulatedWeightedValue() / getTotalWeight();
		}
};

#endif

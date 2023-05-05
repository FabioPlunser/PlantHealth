#ifndef SENSOR_MEASUREMENTS_CLASS
#define SENSOR_MEASUREMENTS_CLASS

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
		ValueAccumulatorClass() { this->calculateWheight = NULL; }
		ValueAccumulatorClass(double (*calculateWheight
		)(unsigned long timeNow, unsigned long timeLastUpdate,
		  unsigned long timeLastReset)) {
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
			accumulatedWeightedValue = 0;
			totalWeights			 = 0;
			lastReset				 = millis();
			lastUpdate				 = lastReset;
		}

		void addValue(double value) {
			lastUpdate				 = millis();
			double weight			 = getWeightFactor(lastUpdate);
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

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
		double (*calculateWheight)(double lastUpdate, double lastReset);

		// ---------- Constructors ---------- //
	public:
		ValueAccumulatorClass() { this->calculateWheight = NULL; }
		ValueAccumulatorClass(double (*calculateWheight
		)(double lastUpdate, double lastReset)) {
			this->calculateWheight = calculateWheight;
		}

		// ---------- Functions ---------- //

	private:
		double getWeightFactor() {
			return this->calculateWheight == NULL
					   ? 1
					   : calculateWheight(lastUpdate, lastReset);
		}

	public:
		void reset() {
			accumulatedWeightedValue = 0;
			totalWeights			 = 0;
			lastReset				 = millis();
			lastUpdate				 = lastReset;
		}

		void addValue(double value) {
			double weight			 = getWeightFactor();
			accumulatedWeightedValue += value * weight;
			totalWeights			 += weight;
			lastUpdate				 = millis();
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

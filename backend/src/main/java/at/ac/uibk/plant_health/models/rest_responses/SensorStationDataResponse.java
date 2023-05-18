package at.ac.uibk.plant_health.models.rest_responses;

import static java.util.stream.Collectors.groupingBy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SensorStationDataResponse extends RestResponse implements Serializable {
	private final List<InnerSensors> data;
	private final UUID sensorStationId;
	public SensorStationDataResponse(
			SensorStation sensorStation, LocalDateTime from, LocalDateTime to
	) {
		this.sensorStationId = sensorStation.getDeviceId();
		if (!sensorStation.isDeleted()) {
			this.data = sensorStation.getSensorData()
								.stream()
								.filter(sensorData
										-> sensorData.getTimeStamp().isAfter(from)
												&& sensorData.getTimeStamp().isBefore(to))
								.collect(groupingBy(SensorData::getSensor))
								.entrySet()
								.stream()
								.map(entry -> new InnerSensors(entry, sensorStation))
								.toList();

		} else {
			this.data = null;
		}
	}

	@Getter
	private static class InnerSensors implements Serializable {
		private final String sensorType;
		private final String sensorUnit;
		private final List<TimeStampedSensorData> values;
		private final List<SensorLimits> sensorLimits;
		public InnerSensors(
				Map.Entry<Sensor, List<SensorData>> sensorData, SensorStation sensorStation
		) {
			this.sensorType = sensorData.getKey().getType();
			this.sensorUnit = sensorData.getKey().getUnit();

			this.sensorLimits =
					sensorStation.getSensorLimits()
							.stream()
							.filter(limit -> limit.getSensor().equals(sensorData.getKey()))
							.sorted(Comparator.comparing(SensorLimits::getTimeStamp))
							.toList();

			this.values = sensorData.getValue()
								  .stream()
								  .sorted(Comparator.comparing(SensorData::getTimeStamp))
								  .map(TimeStampedSensorData::new)
								  .toList();
		}
	}

	@Getter
	private static class TimeStampedSensorData implements Serializable {
		private final LocalDateTime timeStamp;
		private final double value;
		private final boolean isAboveLimit;
		private final boolean isBelowLimit;
		private final char alarm;

		public TimeStampedSensorData(SensorData sensorData) {
			this.timeStamp = sensorData.getTimeStamp();
			this.value = sensorData.getValue();
			this.isAboveLimit = sensorData.isAboveLimit();
			this.isBelowLimit = sensorData.isBelowLimit();
			this.alarm = sensorData.getAlarm();
		}
	}
}

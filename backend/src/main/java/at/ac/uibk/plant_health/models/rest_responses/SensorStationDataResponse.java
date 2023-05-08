package at.ac.uibk.plant_health.models.rest_responses;

import static java.util.stream.Collectors.groupingBy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SensorStationDataResponse extends RestResponse implements Serializable {
	private final List<InnerSensors> data;
	private final List<SensorData> allData;
	private final UUID sensorStationId;

	public SensorStationDataResponse(
			SensorStation sensorStation, LocalDateTime from, LocalDateTime to
	) {
		this.sensorStationId = sensorStation.getDeviceId();

		this.data = sensorStation.getSensorData()
							.stream()
							.filter(data
									-> data.getTimeStamp().isAfter(from)
											&& data.getTimeStamp().isBefore(to))
							.collect(groupingBy(SensorData::getSensor))
							.entrySet()
							.stream()
							.map(InnerSensors::new)
							.toList();
		this.allData = sensorStation.getSensorData();
	}

	@Getter
	private static class InnerSensors implements Serializable {
		private final String sensorType;
		private final String sensorUnit;
		private final List<TimeStampedSensorData> values;

		public InnerSensors(Map.Entry<Sensor, List<SensorData>> sensorData) {
			this.sensorType = sensorData.getKey().getType();
			this.sensorUnit = sensorData.getKey().getUnit();
			this.values = sensorData.getValue().stream().map(TimeStampedSensorData::new).toList();
		}
	}

	@Getter
	private static class TimeStampedSensorData implements Serializable {
		private final LocalDateTime timestamp;
		private final double value;
		private final boolean isAboveLimit;
		private final boolean isBelowLimit;
		private final char alarm;

		public TimeStampedSensorData(SensorData sensorData) {
			this.timestamp = sensorData.getTimeStamp();
			this.value = sensorData.getValue();
			this.isAboveLimit = sensorData.isAboveLimit();
			this.isBelowLimit = sensorData.isBelowLimit();
			this.alarm = sensorData.getAlarm();
		}
	}
}

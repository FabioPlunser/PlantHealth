package at.ac.uibk.plant_health.models.rest_responses;

import static java.util.stream.Collectors.groupingBy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DashBoardDataResponse extends RestResponse implements Serializable {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final List<DashboardPlant> plants;

	public DashBoardDataResponse(Person person) {
		this.plants = person.getSensorStationPersonReferences()
							  .stream()
							  .filter(SensorStationPersonReference::isInDashboard)
							  .map(SensorStationPersonReference::getSensorStation)
							  .map(DashboardPlant::new)
							  .toList();
	}

	@Getter
	private class DashboardPlant implements Serializable {
		@JsonProperty(value = "plant-name")
		private final String name;

		private final List<TimeStampedSensorData> values;

		public DashboardPlant(SensorStation sensorStation) {
			this.name = sensorStation.getName();
			this.values = sensorStation.getSensorData()
								  .stream()
								  .collect(groupingBy(SensorData::getTimeStamp))
								  .entrySet()
								  .stream()
								  .map(TimeStampedSensorData::new)
								  .sorted(Comparator.comparing(o -> o.timestamp))
								  .toList();
		}
	}

	@Getter
	private class TimeStampedSensorData implements Serializable {
		private final LocalDateTime timestamp;
		private final List<InnerSensorData> sensors;
		public TimeStampedSensorData(Map.Entry<LocalDateTime, List<SensorData>> e) {
			this.timestamp = e.getKey();
			this.sensors = e.getValue().stream().map(InnerSensorData::new).toList();
		}
	}

	@Getter
	private class InnerSensorData implements Serializable {
		private final String sensor;
		private final int value;
		private final String unit;
		private final char alarm;

		public InnerSensorData(SensorData sensorData) {
			this.sensor = sensorData.getSensor().getType();
			this.value = sensorData.getValue();
			this.unit = ""; // TODO
			this.alarm = sensorData.getAlarm();
		}
	}
}

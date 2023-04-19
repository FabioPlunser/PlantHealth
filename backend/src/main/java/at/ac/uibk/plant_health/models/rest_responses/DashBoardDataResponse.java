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
import at.ac.uibk.plant_health.repositories.PlantPersonReferenceRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.MODULE)
public class DashBoardDataResponse extends RestResponse implements Serializable {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<DashboardPlant> plants;

	public DashBoardDataResponse(Person person) {
		this.plants = person.getSensorStationPersonReferences()
							  .stream()
							  .filter(SensorStationPersonReference::isInDashboard)
							  .map(SensorStationPersonReference::getSensorStation)
							  .map(DashboardPlant::new)
							  .toList();
	}

	@Getter
	private class DashboardPlant {
		@JsonProperty(value = "plant-name")
		private String name;

		private List<TimeStampedSensorData> values;

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
	private class TimeStampedSensorData {
		private LocalDateTime timestamp;
		private List<InnerSensorData> sensors;
		public TimeStampedSensorData(Map.Entry<LocalDateTime, List<SensorData>> e) {
			this.timestamp = e.getKey();
			this.sensors = e.getValue().stream().map(InnerSensorData::new).toList();
		}
	}

	@Getter
	private class InnerSensorData {
		private String sensor;
		private int value;
		private String unit;
		private char alarm;

		public InnerSensorData(SensorData sensorData) {
			this.sensor = sensorData.getSensor().getType();
			this.value = sensorData.getValue();
			this.unit = ""; // TODO
			this.alarm = sensorData.getAlarm();
		}
	}
}

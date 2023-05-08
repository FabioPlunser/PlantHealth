package at.ac.uibk.plant_health.models.rest_responses;

import static java.util.stream.Collectors.groupingBy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DashBoardDataResponse extends RestResponse implements Serializable {
	private final List<DashboardSensorStation> sensorStations;

	public DashBoardDataResponse(Person person) {
		this.sensorStations = person.getSensorStationPersonReferences()
									  .stream()
									  .filter(SensorStationPersonReference::isInDashboard)
									  .map(SensorStationPersonReference::getSensorStation)
									  .map(DashboardSensorStation::new)
									  .toList();
	}

	@Getter
	private static class DashboardSensorStation implements Serializable {
		private final String name;
		private final String roomName;
		private final UUID sensorStationId;
		private final List<UUID> pictureIds;

		//		private final List<TimeStampedSensorData> values;

		public DashboardSensorStation(SensorStation sensorStation) {
			this.name = sensorStation.getName();

			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.pictureIds = sensorStation.getPlantPictures()
									  .stream()
									  .map(SensorStationPicture::getPictureId)
									  .toList();
			this.sensorStationId = sensorStation.getDeviceId();

			//			this.values = sensorStation.getSensorData()
			//					.stream()
			//					.collect(groupingBy(SensorData::getTimeStamp))
			//					.entrySet()
			//					.stream()
			//					.map(TimeStampedSensorData::new)
			//					.sorted(Comparator.comparing(o -> o.timestamp))
			//					.toList();
		}
	}

	//	@Getter
	//	private class TimeStampedSensorData implements Serializable {
	//		private final LocalDateTime timestamp;
	//		private final List<InnerSensorData> sensors;
	//		public TimeStampedSensorData(Map.Entry<LocalDateTime, List<SensorData>> e) {
	//			this.timestamp = e.getKey();
	//			this.sensors = e.getValue().stream().map(InnerSensorData::new).toList();
	//		}
	//	}
	//
	//	@Getter
	//	private class InnerSensorData implements Serializable {
	//		private final String sensor;
	//		private final double value;
	//		private final String unit;
	//		private final char alarm;
	//
	//		public InnerSensorData(SensorData sensorData) {
	//			this.sensor = sensorData.getSensor().getType();
	//			this.value = sensorData.getValue();
	//			this.unit = sensorData.getSensor().getUnit();
	//			this.alarm = sensorData.getAlarm();
	//		}
	//	}
}

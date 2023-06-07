package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
@Getter
@SuperBuilder
public class GardenerDashBoardResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> assignedSensorStations;
	private final List<InnerResponse> addedSensorStations;
	public GardenerDashBoardResponse(List<SensorStation> allSensorStation, Person person) {
		this.assignedSensorStations = allSensorStation.stream()
											  .filter(s -> !s.isDeleted())
											  .filter(s -> {
												  if (s.getGardener() != null)
													  return s.getGardener().equals(person);
												  else
													  return false;
											  })
											  .map(InnerResponse::new)
											  .toList();
		this.addedSensorStations = person.getSensorStationPersonReferences()
										   .stream()
										   .filter(SensorStationPersonReference::isInDashboard)
										   .map(SensorStationPersonReference::getSensorStation)
										   .map(InnerResponse::new)
										   .toList();
	}

	@Getter
	public static class InnerResponse implements Serializable {
		private final UUID sensorStationId;
		private final String bdAddress;
		private final String roomName;
		private final String name;
		private final int transferInterval;
		private final Person gardener;
		private final int dipSwitchId;
		private final List<AlarmResponse> alarms;
		private final boolean unlocked;
		private final boolean accessPointUnlocked;
		private final boolean connected;
		private final boolean deleted;
		private final boolean reported;
		public InnerResponse(SensorStation sensorStation) {
			this.sensorStationId = sensorStation.getDeviceId();
			this.bdAddress = sensorStation.getBdAddress();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.name = sensorStation.getName();
			this.transferInterval = sensorStation.getAccessPoint().getTransferInterval();
			this.gardener = sensorStation.getGardener();
			this.dipSwitchId = sensorStation.getDipSwitchId();
			this.alarms = sensorStation.getSensorData()
								  .stream()
								  .map(SensorData::getSensor)
								  .distinct()
								  .map(sensor -> new AlarmResponse(sensor, sensorStation))
								  .toList();
			this.accessPointUnlocked = sensorStation.getAccessPoint().isUnlocked();
			this.unlocked = sensorStation.isUnlocked();
			this.connected = sensorStation.isConnected();
			this.deleted = sensorStation.isDeleted();
			this.reported = sensorStation.isReported();
		}
	}

	@Getter
	public static class AlarmResponse implements Serializable {
		private final Sensor sensor;
		private final String alarm;

		public AlarmResponse(Sensor sensor, SensorStation sensorStation) {
			this.sensor = sensor;
			this.alarm = sensorStation.getSensorData()
								 .stream()
								 .filter(d -> d.getSensor().equals(sensor))
								 .max(Comparator.comparing(SensorData::getTimeStamp))
								 .map(SensorData::getAlarm)
								 .orElse("n");
		}
	}
}

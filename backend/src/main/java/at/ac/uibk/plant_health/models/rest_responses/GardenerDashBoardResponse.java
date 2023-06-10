package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
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
		private final String alarm;
		private final boolean unlocked;
		private final boolean accessPointUnlocked;
		private final boolean connected;
		private final boolean deleted;
		public InnerResponse(SensorStation sensorstation) {
			this.sensorStationId = sensorstation.getDeviceId();
			this.bdAddress = sensorstation.getBdAddress();
			this.roomName = sensorstation.getAccessPoint().getRoomName();
			this.name = sensorstation.getName();
			this.transferInterval = sensorstation.getAccessPoint().getTransferInterval();
			this.gardener = sensorstation.getGardener();
			this.dipSwitchId = sensorstation.getDipSwitchId();
			var alarm = sensorstation.getSensorData().stream().min(
					Comparator.comparing(SensorData::getTimeStamp)
			);
			if (alarm.isPresent())
				this.alarm = alarm.get().getAlarm();
			else
				this.alarm = "n";
			this.accessPointUnlocked = sensorstation.getAccessPoint().isUnlocked();
			this.unlocked = sensorstation.isUnlocked();
			this.connected = sensorstation.isConnected();
			this.deleted = sensorstation.isDeleted();
		}
	}
}

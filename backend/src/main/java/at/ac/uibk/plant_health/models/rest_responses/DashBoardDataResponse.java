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
		private final boolean connected;
		private final boolean unlocked;
		private final boolean deleted;

		public DashboardSensorStation(SensorStation sensorStation) {
			if (!sensorStation.isDeleted()) {
				this.name = sensorStation.getName();
				this.roomName = sensorStation.getAccessPoint().getRoomName();
				this.pictureIds = sensorStation.getPlantPictures()
										  .stream()
										  .map(SensorStationPicture::getPictureId)
										  .toList();
				this.sensorStationId = sensorStation.getDeviceId();
				this.connected = sensorStation.isConnected();
				this.unlocked = sensorStation.isUnlocked();
				this.deleted = false;
			} else {
				this.name = null;
				this.roomName = null;
				this.pictureIds = null;
				this.sensorStationId = null;
				this.connected = false;
				this.unlocked = false;
				this.deleted = true;
			}
		}
	}
}

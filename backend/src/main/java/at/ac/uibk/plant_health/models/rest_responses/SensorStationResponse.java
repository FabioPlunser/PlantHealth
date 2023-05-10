package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;

@Getter
public class SensorStationResponse extends RestResponse implements Serializable {
	private final InnerResponse sensorStation;

	public SensorStationResponse(SensorStation sensorStation, Person person) {
		super();
		this.sensorStation = new InnerResponse(sensorStation, person);
	}

	@Getter
	private static class InnerResponse {
		private final String bdAddress;
		private final int dipSwitchId;
		private final String name;
		private final UUID sensorStationId;
		private final boolean unlocked;
		private final boolean connected;
		private final boolean deleted;
		private final List<SensorLimits> sensorLimits;
		private final List<SensorStationPersonReference> sensorStationPersonReferences;
		private final List<SensorStationPicture> sensorStationPictures;
		private final List<Sensor> sensors;

		public InnerResponse(SensorStation sensorStation, Person person) {
			this.bdAddress = sensorStation.getBdAddress();
			this.dipSwitchId = sensorStation.getDipSwitchId();
			this.name = sensorStation.getName();
			this.sensorStationId = sensorStation.getDeviceId();
			this.unlocked = sensorStation.isUnlocked();
			this.connected = sensorStation.isConnected();
			this.deleted = sensorStation.isDeleted();
			if (sensorStation.getSensorLimits().size() > 0) {
				this.sensorLimits = sensorStation.getSensorLimits();
			} else {
				this.sensorLimits = sensorStation.getSensorData()
											.stream()
											.map(SensorData::getSensor)
											.distinct()
											.toList()
											.stream()
											.map(sensorData
												 -> new SensorLimits(
														 LocalDateTime.now(), 0, 0, 0, sensorData,
														 person, sensorStation
												 ))
											.toList();
			}
			this.sensorStationPersonReferences = sensorStation.getSensorStationPersonReferences();
			this.sensorStationPictures = sensorStation.getSensorStationPictures();
			this.sensors = sensorStation.getSensorData()
								   .stream()
								   .map(SensorData::getSensor)
								   .distinct()
								   .toList();
		}
	}
}

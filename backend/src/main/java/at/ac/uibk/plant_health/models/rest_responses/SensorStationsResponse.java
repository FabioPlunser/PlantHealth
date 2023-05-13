package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationsResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> sensorStations;

	public SensorStationsResponse(List<SensorStation> sensorStations) {
		this.sensorStations = sensorStations.stream()
									  .filter(s -> !s.isDeleted())
									  .map(InnerResponse::new)
									  .toList();
	}

	@Getter
	private static class InnerResponse implements Serializable {
		private final UUID sensorStationId;
		private final String bdAddress;
		private final String roomName;
		private final String name;
		private final int dipSwitchId;
		private final boolean unlocked;
		private final boolean connected;
		private final boolean deleted;
		public InnerResponse(SensorStation sensorstation) {
			this.sensorStationId = sensorstation.getDeviceId();
			this.bdAddress = sensorstation.getBdAddress();
			this.roomName = sensorstation.getAccessPoint().getRoomName();
			this.name = sensorstation.getName();
			this.dipSwitchId = sensorstation.getDipSwitchId();
			this.unlocked = sensorstation.isUnlocked();
			this.connected = sensorstation.isConnected();
			this.deleted = sensorstation.isDeleted();
		}
	}
}

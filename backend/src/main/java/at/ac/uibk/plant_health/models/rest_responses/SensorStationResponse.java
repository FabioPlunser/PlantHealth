package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationResponse extends RestResponse implements Serializable {
	private final List<SensorStation> sensorStations;

	public SensorStationResponse(List<SensorStation> sensorStations) {
		this.sensorStations = sensorStations.stream().filter(s -> !s.isDeleted()).toList();
	}

	//	@Getter
	//	private class InnerSensorStation implements Serializable {
	//		private final UUID id;
	//		private final int dipSwitchId;
	//		private final boolean locked;
	//
	//		public InnerSensorStation(SensorStation sensorStation) {
	//			this.id = sensorStation.getDeviceId();
	//			this.dipSwitchId = sensorStation.getDipSwitchId();
	//			this.locked = sensorStation.isUnlocked();
	//		}
	//	}
}

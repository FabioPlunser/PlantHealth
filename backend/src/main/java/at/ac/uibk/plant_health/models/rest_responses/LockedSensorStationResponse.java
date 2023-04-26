package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class LockedSensorStationResponse extends RestResponse implements Serializable {
	private final List<SensorStation> sensorStations;

	public LockedSensorStationResponse(List<SensorStation> sensorStations) {
		this.sensorStations = sensorStations;
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

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
@NoArgsConstructor(access = AccessLevel.MODULE)
public class LockedSensorStationResponse extends RestResponse implements Serializable {
	@JsonProperty("sensor-stations")
	private List<InnerSensorStation> sensorStations;

	public LockedSensorStationResponse(List<SensorStation> sensorStations) {
		this.sensorStations = sensorStations.stream().map(InnerSensorStation::new).toList();
	}

	private class InnerSensorStation {
		private UUID id;
		@JsonProperty("dip-switch")
		private int dipSwitchId;
		private boolean locked;

		public InnerSensorStation(SensorStation sensorStation) {
			this.id = sensorStation.getDeviceId();
			this.dipSwitchId = sensorStation.getDipSwitchId();
			this.locked = sensorStation.isUnlocked();
		}
	}
}

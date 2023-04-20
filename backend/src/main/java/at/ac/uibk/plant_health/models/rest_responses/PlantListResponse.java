package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PlantListResponse extends RestResponse {
	private final List<InnerPlant> plants;

	public PlantListResponse(List<SensorStation> sensorStations) {
		this.plants = sensorStations.stream().map(InnerPlant::new).toList();
	}

	@Getter
	private class InnerPlant implements Serializable {
		private final UUID id;
		@JsonProperty("plant-name")
		private final String name;
		@JsonProperty("room-name")
		private final String roomName;

		public InnerPlant(SensorStation sensorStation) {
			this.id = sensorStation.getDeviceId();
			this.name = sensorStation.getName();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
		}
	}
}

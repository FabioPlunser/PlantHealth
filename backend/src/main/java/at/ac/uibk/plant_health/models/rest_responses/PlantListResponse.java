package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantListResponse extends RestResponse {
	private List<InnerPlant> plants;

	public PlantListResponse(List<SensorStation> sensorStations) {
		this.plants = sensorStations.stream().map(InnerPlant::new).toList();
	}

	private class InnerPlant {
		private UUID id;
		@JsonProperty("plant-name")
		private String name;
		@JsonProperty("room-name")
		private String roomName;

		public InnerPlant(SensorStation sensorStation) {
			this.id = sensorStation.getDeviceId();
			this.name = sensorStation.getName();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
		}
	}
}

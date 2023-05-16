package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserSensorStationsResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> sensorStations;
	public UserSensorStationsResponse(List<SensorStation> sensorStations, Person person) {
		this.sensorStations =
				sensorStations.stream()
					.map(InnerResponse::new)
					.toList();
	}

	@Getter
	private static class InnerResponse implements Serializable {
		private final UUID sensorStationId;
		private final String roomName;
		private final String sensorStationName;
		public InnerResponse(SensorStation sensorStation) {
			this.sensorStationId = sensorStation.getDeviceId();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.sensorStationName = sensorStation.getName();
		}
	}
}

package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationsResponse extends RestResponse implements Serializable {
	private final List<SensorStationsInnerResponse> sensorStations;
	public SensorStationsResponse(List<SensorStation> sensorStations, Person person) {
		this.sensorStations =
				sensorStations.stream()
						.filter(s
								-> !s.isDeleted() && s.isUnlocked()
										&& s.getAccessPoint().isUnlocked())
						.filter(s
								-> person.getSensorStationPersonReferences().stream().noneMatch(
										r -> r.getSensorStation().equals(s)
								))
						.map(SensorStationsInnerResponse::new)
						.toList();
	}

	@Getter
	private static class SensorStationsInnerResponse implements Serializable {
		private final UUID sensorStationId;
		private final String roomName;
		private final String name;
		// newest picture for the response interface in frontend
		// because it gets fetched in a separate request
		private final String newestPicture = "";
		public SensorStationsInnerResponse(SensorStation sensorStation) {
			this.sensorStationId = sensorStation.getDeviceId();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.name = sensorStation.getName();
		}
	}
}

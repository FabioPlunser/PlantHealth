package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationsResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> sensorStations;
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

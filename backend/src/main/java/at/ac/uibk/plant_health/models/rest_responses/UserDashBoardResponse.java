package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserDashBoardResponse extends RestResponse implements Serializable {
	private final List<SensorStationBaseResponse> sensorStations;

	public UserDashBoardResponse(Person person) {
		this.sensorStations = person.getSensorStationPersonReferences()
									  .stream()
									  .filter(SensorStationPersonReference::isInDashboard)
									  .map(SensorStationPersonReference::getSensorStation)
									  .map(SensorStationBaseResponse::new)
									  .toList();
	}
}

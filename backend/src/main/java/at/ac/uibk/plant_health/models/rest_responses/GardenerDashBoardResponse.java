package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
@Getter
@SuperBuilder
public class GardenerDashBoardResponse extends RestResponse implements Serializable {
	private final List<SensorStationBaseResponse> assignedSensorStations;
	private final List<SensorStationBaseResponse> addedSensorStations;
	public GardenerDashBoardResponse(List<SensorStation> allSensorStation, Person person) {
		this.assignedSensorStations = allSensorStation.stream()
											  .filter(s -> {
												  if (s.getGardener() != null)
													  return s.getGardener().equals(person);
												  else
													  return false;
											  })
											  .map(SensorStationBaseResponse::new)
											  .toList();
		this.addedSensorStations = person.getSensorStationPersonReferences()
										   .stream()
										   .filter(SensorStationPersonReference::isInDashboard)
										   .map(SensorStationPersonReference::getSensorStation)
										   .map(SensorStationBaseResponse::new)
										   .toList();
	}
}

package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AdminDashBoardResponse extends RestResponse implements Serializable {
	private final int numOfUsers;
	private final int numOfConnectedSensorStations;
	private final int numOfConnectedAccessPoints;
	private final List<SensorStationBaseResponse> sensorStations;
	public AdminDashBoardResponse(
			List<SensorStation> sensorStationList, List<AccessPoint> accessPointList,
			List<Person> personList, Person person
	) {
		this.numOfUsers = personList.size();
		this.numOfConnectedSensorStations = (int) sensorStationList.stream()
													.filter(s -> !s.isDeleted() && s.isConnected())
													.count();
		this.numOfConnectedAccessPoints = (int) accessPointList.stream()
												  .filter(a -> !a.isDeleted() && a.isConnected())
												  .count();
		this.sensorStations = person.getSensorStationPersonReferences()
									  .stream()
									  .filter(SensorStationPersonReference::isInDashboard)
									  .map(SensorStationPersonReference::getSensorStation)
									  .map(SensorStationBaseResponse::new)
									  .toList();
	}
}

package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.rest_responses.GardenerDashBoardResponse.InnerResponse;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AdminSensorStationsResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> sensorStations;

	public AdminSensorStationsResponse(List<SensorStation> sensorStations) {
		this.sensorStations = sensorStations.stream()
									  .filter(s -> !s.isDeleted())
									  .map(InnerResponse::new)
									  .toList();
	}
}

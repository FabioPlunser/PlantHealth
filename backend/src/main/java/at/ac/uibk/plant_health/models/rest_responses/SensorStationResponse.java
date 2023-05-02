package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationResponse extends RestResponse implements Serializable {
	private final List<SensorStation> sensorStations;

	public SensorStationResponse(List<SensorStation> sensorStations) {
		super();
		this.sensorStations = sensorStations.stream().filter(s -> !s.isDeleted()).toList();
	}
}

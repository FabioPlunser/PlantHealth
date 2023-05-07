package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import lombok.Getter;

@Getter
public class SensorStationResponse extends RestResponse implements Serializable {
	private final SensorStation sensorStation;
	private final List<Sensor> sensors;

	public SensorStationResponse(SensorStation sensorStation) {
		super();
		this.sensorStation = sensorStation;
		this.sensors = sensorStation.getSensorData()
							   .stream()
							   .map(SensorData::getSensor)
							   .distinct()
							   .toList();
	}
}

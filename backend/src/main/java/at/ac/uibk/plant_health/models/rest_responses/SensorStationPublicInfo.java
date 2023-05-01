package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SensorStationPublicInfo extends RestResponse implements Serializable {
	private final String name;
	private final String roomName;

	public SensorStationPublicInfo(SensorStation sensorStation) {
		this.name = sensorStation.getName();
		this.roomName = sensorStation.getAccessPoint().getRoomName();
	}
}

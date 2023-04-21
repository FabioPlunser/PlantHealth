package at.ac.uibk.plant_health.models.rest_responses;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.plant_health.models.device.SensorStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class QrCodeResponse extends RestResponse {
	private String name;
	private String roomName;
	private List<String> pictures;

	public QrCodeResponse(SensorStation sensorStation) {
		this.name = sensorStation.getName();
		this.roomName = sensorStation.getAccessPoint().getRoomName();
		// TODO: Implement Pictures
		this.pictures = new ArrayList<>();
	}
}

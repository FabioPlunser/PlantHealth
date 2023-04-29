package at.ac.uibk.plant_health.models.rest_responses;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.service.SensorStationService;
import lombok.Data;
import lombok.Getter;

@Getter
public class PlantPictureResponse extends RestResponse implements Serializable {
	private final List<String> pictures;

	public PlantPictureResponse(List<String> pictures, UUID sensorStationId) {
		super();
		this.pictures = pictures;
	}
}

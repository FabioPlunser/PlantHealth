package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;

@Getter
public class PlantPictureResponse extends RestResponse implements Serializable {
	List<String> pictures;

	public PlantPictureResponse(List<String> pictures) {
		super();
		this.pictures = pictures;
	}
}

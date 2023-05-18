package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import lombok.Getter;

@Getter
public class PlantPicturesResponse extends RestResponse implements Serializable {
	private final List<InnerPlantPicture> pictures;
	private final String roomName;
	private final String plantName;

	public PlantPicturesResponse(List<SensorStationPicture> pictures, SensorStation sensorStation) {
		super();
		this.pictures = pictures.stream().map(InnerPlantPicture::new).toList();
		this.roomName = sensorStation.getAccessPoint().getRoomName();
		this.plantName = sensorStation.getName();
	}

	@Getter
	private static class InnerPlantPicture implements Serializable {
		private final UUID pictureId;
		private final LocalDateTime timeStamp;

		public InnerPlantPicture(SensorStationPicture picture) {
			this.pictureId = picture.getPictureId();
			this.timeStamp = picture.getTimeStamp();
		}
	}
}

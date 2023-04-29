package at.ac.uibk.plant_health.models.rest_responses;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.service.SensorStationService;
import lombok.Data;
import lombok.Getter;

@Getter
public class PlantPictureResponse extends RestResponse implements Serializable {
	private final String encoding = "base64";
	private final List<InnerPlantPicture> pictures;

	public PlantPictureResponse(List<PlantPicture> pictures, SensorStation sensorStation) {
		super();
		this.pictures =
				pictures.stream().map(p -> new InnerPlantPicture(p, sensorStation)).toList();
	}

	@Getter
	private static class InnerPlantPicture implements Serializable {
		private final String roomName;
		private final String plantName;
		private final String picture;
		private final LocalDateTime timeStamp;

		public InnerPlantPicture(PlantPicture picture, SensorStation sensorStation) {
			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.plantName = sensorStation.getName();
			String test = convertPictureToBase64(picture);
			System.out.println(test);
			this.picture = test;
			this.timeStamp = picture.getTimeStamp();
		}
	}

	private static String convertPictureToBase64(PlantPicture picture) {
		try {
			Path path = Paths.get(picture.getPicturePath());
			System.out.println("Path: " + path);

			byte[] pictureBytes = Files.readAllBytes(path);
			return Base64.encodeBase64String(pictureBytes);
		} catch (Exception e) {
			return null;
		}
	}
}

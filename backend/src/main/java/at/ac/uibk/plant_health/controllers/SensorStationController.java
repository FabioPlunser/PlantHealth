package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.PlantPictureResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.rest_responses.SensorStationResponse;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.SensorStationService;

@RestController
public class SensorStationController {
	@Autowired
	private SensorStationService sensorStationService;

	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@GetMapping("/get-sensor-stations")
	public RestResponseEntity getSensorStations() {
		return new SensorStationResponse(sensorStationService.findAll()).toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/set-unlock-sensor-station", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setLockSensorStation(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam("unlocked") final boolean unlocked
	) {
		if (!sensorStationService.sensorStationExists(sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(404)
					.message("Could not find sensorStation")
					.toEntity();
		}
		if (!sensorStationService.setUnlocked(unlocked, sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(500)
					.message("Couldn't set unlock state of SensorStation")
					.toEntity();
		}

		return MessageResponse.builder().statusCode(200).toEntity();
	}

	@AnyPermission(Permission.GARDENER)
	@RequestMapping(value = "/set-sensor-limits", method = {RequestMethod.POST, RequestMethod.PUT})
	public RestResponseEntity setSensorLimits(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestBody final List<SensorLimits> sensorLimits
	) {
		if (!sensorStationService.sensorStationExists(sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(404)
					.message("Could not find sensorStation")
					.toEntity();
		}
		throw new NotImplementedException("Not implemented yet");
	}

	@PublicEndpoint
	@WriteOperation
	@RequestMapping(
			value = "/upload-sensor-station-picture",
			method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	uploadPicture(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestBody final String picture
	) {
		if (!sensorStationService.sensorStationExists(sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(404)
					.message("Could not find sensorStation")
					.toEntity();
		}
		if (!sensorStationService.uploadPicture(picture, sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(500)
					.message("Could not upload picture")
					.toEntity();
		}
		return MessageResponse.builder().statusCode(200).toEntity();
	}

	@PublicEndpoint
	@ReadOperation
	@GetMapping("/get-sensor-station-pictures")
	public RestResponseEntity getSensorStationPictures(@RequestParam("sensorStationId"
	) final UUID sensorStationId) {
		if (!sensorStationService.sensorStationExists(sensorStationId)) {
			return MessageResponse.builder()
					.statusCode(404)
					.message("Could not find sensorStation")
					.toEntity();
		}
		List<String> pictures = sensorStationService.getPictures(sensorStationId);
		if (pictures == null) {
			return MessageResponse.builder()
					.statusCode(500)
					.message("Could not get pictures")
					.toEntity();
		}
		return new PlantPictureResponse(pictures).toEntity();
	}
}

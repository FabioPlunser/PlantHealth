package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import javax.mail.Message;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.rest_responses.LockedSensorStationResponse;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.SensorStationService;

@RestController
public class SensorStationController {
	@Autowired
	private SensorStationService sensorStationService;

	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-sensor-stations")
	public RestResponseEntity getSensorStations() {
		return new LockedSensorStationResponse(sensorStationService.findLocked()).toEntity();
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
}

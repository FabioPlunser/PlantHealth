package at.ac.uibk.plant_health.controllers;

import at.ac.uibk.plant_health.models.rest_responses.LockedSensorStationResponse;
import at.ac.uibk.plant_health.service.SensorStationService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Permission;

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
			value = "/set-lock-sensor-station", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setLockSensorStation(
			//            @RequestBody final UUID sensorStationId
			@RequestBody final boolean locked
	) {
		throw new NotImplementedException();
	}
}

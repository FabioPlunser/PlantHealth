package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.AccessPointService;

@RestController
public class AccessPointController {
	@Autowired
	private AccessPointService accessPointService;

	private static final String LOCKED_MESSAGE = "AccessPoint is locked";

	@PublicEndpoint
	@PostMapping("/register-access-point")
	public RestResponseEntity register(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "roomName") final String roomName
	) {
		if (
				!accessPointService.isAccessPointRegistered(accessPointId) &&
				!accessPointService.register(accessPointId, roomName)
		) {
			return MessageResponse.builder()
					.statusCode(500)
					.message("Could not register AccessPoint")
					.toEntity();
		}
		if (!accessPointService.isUnlocked(accessPointId)) {
			return MessageResponse.builder()
					.statusCode(401)
					.message(LOCKED_MESSAGE)
					.toEntity();
		}
		return TokenResponse.builder()
				.statusCode(200)
				.token(accessPointService.getAccessPointAccessToken(accessPointId))
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-access-points")
	public RestResponseEntity getAccessPoints() {
		return new AccessPointListResponse(accessPointService.findAllAccessPoints()).toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/set-unlocked-access-point", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setLockAccessPoint(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "unlocked") final boolean unlocked
	) {
		if (!accessPointService.setUnlocked(unlocked, accessPointId)) {
			return MessageResponse.builder()
					.statusCode(404)
					.message("Could not set lock state of AccessPoint")
					.toEntity();
		}
		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully set lock state of AccessPoint")
				.toEntity();
	}

	@ReadOperation
	@GetMapping("/get-access-point-config")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity getAccessPointConfig(AccessPoint accessPoint) {
		if (accessPointService.isUnlocked(accessPoint.getSelfAssignedId())) {
			return new AccessPointConfigResponse(accessPoint).toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully send AccessPoint config")
				.toEntity();
	}

	@WriteOperation
	@PutMapping("/found-sensor-stations")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity foundSensorStations(
			AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStations
	) {
		if (!accessPointService.isUnlocked(accessPoint.getSelfAssignedId())) {
			return MessageResponse.builder()
					.statusCode(403)
					.message(LOCKED_MESSAGE)
					.toEntity();
		}
		if (accessPointService.foundNewSensorStation(accessPoint, sensorStations)) {
			return MessageResponse.builder()
					.statusCode(200)
					.message("Successfully found new SensorStations")
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(500)
				.message("Internal server Error")
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@PostMapping("/scan-for-sensor-stations")
	public RestResponseEntity scanForSensorStations(@RequestParam(name = "accessPointId")
													final UUID accessPointId) {
		if (!accessPointService.isAccessPointRegistered(accessPointId))
			return MessageResponse.builder()
					.statusCode(404)
					.message("AccessPoint not registered")
					.toEntity();

		if (!accessPointService.isUnlocked(accessPointId))
			return MessageResponse.builder()
					.statusCode(401)
					.message(LOCKED_MESSAGE)
					.toEntity();

		if (!accessPointService.startScan(accessPointId))
			return MessageResponse.builder()
					.statusCode(500)
					.message("Could not start scan")
					.toEntity();
		throw new NotImplementedException();
	}

	@PostMapping("/transfer-data")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity transferData(
			final AccessPoint accessPoint, @RequestBody final String data
	) {
		throw new NotImplementedException();
	}
}

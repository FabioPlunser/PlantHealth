package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.rest_responses.ListResponse;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.rest_responses.TokenResponse;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.AccessPointService;

@RestController
public class AccessPointController {
	@Autowired
	private AccessPointService accessPointService;

	@PublicEndpoint
	@PostMapping("/register-access-point")
	public RestResponseEntity register(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "roomName") final String roomName
	) {
		if (!accessPointService.isAccessPointRegistered(accessPointId)) {
			if (!accessPointService.register(accessPointId, roomName)) {
				return MessageResponse.builder()
						.statusCode(500)
						.message("Could not register AccessPoint")
						.toEntity();
			}
		}
		if (!accessPointService.isUnlocked(accessPointId)) {
			return MessageResponse.builder()
					.statusCode(401)
					.message("AccessPoint is locked")
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
		return new ListResponse<>(accessPointService.findAllAccessPoints()).toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/set-lock-access-point", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setLockAccessPoint(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "locked") final boolean locked
	) {
		if (!accessPointService.setUnlocked(locked, accessPointId)) {
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

	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/create-plant-qr-code",
			method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	createPlantQrCode(
			//            @RequestBody final UUID plantId
			@RequestBody final UUID qrCode
	) {
		throw new NotImplementedException();
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
					.message("AccessPoint is locked")
					.toEntity();

		if (!accessPointService.startScan(accessPointId))
			return MessageResponse.builder()
					.statusCode(500)
					.message("Could not start scan")
					.toEntity();
		throw new NotImplementedException();
	}

	@PostMapping("/found-sensor-stations")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity foundSensorStations(
			final AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStationList
	) {
		throw new NotImplementedException();
	}

	@PostMapping("/transfer-data")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity transferData(
			@P("accessPoint") final AccessPoint accessPoint, @RequestBody final String data
	) {
		throw new NotImplementedException();
	}
}

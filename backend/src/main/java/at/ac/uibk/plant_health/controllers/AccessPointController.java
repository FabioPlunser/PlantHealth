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
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.service.AccessPointService;
import lombok.With;

@RestController
public class AccessPointController {
	@Autowired
	private AccessPointService accessPointService;

	private static final String LOCKED_MESSAGE = "AccessPoint is locked";

	@PublicEndpoint
	@WriteOperation
	@PostMapping("/register-access-point")
	public RestResponseEntity register(
			@RequestParam(name = "selfAssignedId") final UUID selfAssignedId,
			@RequestParam(name = "roomName") final String roomName
	) {
		try {
			accessPointService.register(selfAssignedId, roomName);
			accessPointService.isUnlockedBySelfAssignedId(selfAssignedId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return TokenResponse.builder()
				.statusCode(200)
				.token(accessPointService.getAccessPointAccessToken(selfAssignedId))
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@ReadOperation
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
		try {
			accessPointService.findById(accessPointId);
			accessPointService.setUnlocked(unlocked, accessPointId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("AccessPoint unlock set to " + unlocked)
				.toEntity();
	}

	@ReadOperation
	@GetMapping("/get-access-point-config")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity getAccessPointConfig(AccessPoint accessPoint) {
		try {
			accessPointService.findById(accessPoint.getDeviceId());
			accessPointService.isUnlockedByDeviceId(accessPoint.getDeviceId());
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return new AccessPointConfigResponse(accessPoint).toEntity();
	}

	@WriteOperation
	@PostMapping("/found-sensor-stations")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity foundSensorStations(
			AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStations
	) {
		try {
			accessPointService.findById(accessPoint.getDeviceId());
			accessPointService.isUnlockedByDeviceId(accessPoint.getDeviceId());
			accessPointService.foundNewSensorStation(accessPoint, sensorStations);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully added " + sensorStations.size() + " SensorStations")
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@PostMapping("/scan-for-sensor-stations")
	public RestResponseEntity scanForSensorStations(@RequestParam(name = "accessPointId")
													final UUID accessPointId) {
		try {
			accessPointService.findById(accessPointId);
			accessPointService.isUnlockedByDeviceId(accessPointId);
			accessPointService.startScan(accessPointId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully started scan")
				.toEntity();
	}

	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@PostMapping("/set-access-point-transfer-interval")
	public RestResponseEntity setAPTransferInterval(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "transferInterval") final int transferInterval
	) {
		try {
			accessPointService.findById(accessPointId);
			accessPointService.setTransferInterval(accessPointId, transferInterval);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully set transfer interval")
				.toEntity();
	}

	@AnyPermission({Permission.ADMIN})
	@PostMapping("/update-access-point")
	public RestResponseEntity updateAccessPoint(
			@RequestParam(name = "accessPointId") final UUID accessPointId,
			@RequestParam(name = "roomName") final String roomName,
			@RequestParam(name = "transferInterval") final int transferInterval
	) {
		try {
			accessPointService.findById(accessPointId);
			accessPointService.updateAccessPointInfo(accessPointId, roomName, transferInterval);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully updated access point info")
				.toEntity();
	}

	@PostMapping("/transfer-data")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity transferData(
			final AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStationList
	) {
		try {
			accessPointService.findById(accessPoint.getDeviceId());
			accessPointService.isUnlockedByDeviceId(accessPoint.getDeviceId());
			accessPointService.setSensorStationData(sensorStationList, accessPoint);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully transfered data")
				.toEntity();
	}
}

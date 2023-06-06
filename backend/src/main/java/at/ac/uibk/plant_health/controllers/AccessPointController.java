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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.With;

@RestController
public class AccessPointController {
	@Autowired
	private AccessPointService accessPointService;

	@Operation(summary = "Register an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Access Point successfully registered",
				content = @Content(schema = @Schema(implementation = TokenResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Access Point could not be registered",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
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

	@Operation(summary = "Get all Access Points")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Access Points successfully retrieved",
				content = @Content(schema = @Schema(implementation = AccessPointListResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Access Points could not be retrieved",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@AnyPermission(Permission.ADMIN)
	@ReadOperation
	@GetMapping("/get-access-points")
	public RestResponseEntity
	getAccessPoints() {
		return new AccessPointListResponse(accessPointService.findAllAccessPoints()).toEntity();
	}

	@Operation(summary = "Set the lock state of an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Access Point lock state successfully set",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400",
						description = "Access Point lock state could not be set",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})

	@WriteOperation
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

	@Operation(summary = "Get the configuration of an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200",
				description = "Access Point configuration successfully retrieved",
				content =
						@Content(schema = @Schema(implementation = AccessPointConfigResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400",
						description = "Access Point configuration could not be retrieved",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@ReadOperation
	@GetMapping("/get-access-point-config")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity
	getAccessPointConfig(AccessPoint accessPoint) {
		accessPointService.setLastConnection(accessPoint);
		// This cannot fail because the AccessPoint has to exist and be unlocked.
		// If that were not the case the Security Chain would not have authenticated the request.
		return new AccessPointConfigResponse(accessPoint).toEntity();
	}

	@Operation(summary = "Send found sensor stations to Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Sensor Stations successfully added",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Sensor Stations could not be added",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@WriteOperation
	@RequestMapping(
			value = "/found-sensor-stations", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity
	foundSensorStations(
			AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStations
	) {
		// This cannot fail because the AccessPoint has to exist and be unlocked.
		// If that were not the case the Security Chain would not have authenticated the request.
		// Saving the Sensor Stations can not fail because of the Structure of the Method
		// (unless we run out of Memory in which case not saving a SensorStation is the least of our
		// worries).
		accessPointService.setLastConnection(accessPoint);
		accessPointService.foundNewSensorStation(accessPoint, sensorStations);

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully added " + sensorStations.size() + " SensorStations")
				.toEntity();
	}

	@Operation(summary = "Activate scan of Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Scan successfully started",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Scan could not be started",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})

	@AnyPermission(Permission.ADMIN)
	@PostMapping("/scan-for-sensor-stations")
	public RestResponseEntity
	scanForSensorStations(@RequestParam(name = "accessPointId") final UUID accessPointId) {
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

	@Operation(summary = "Set the transfer interval of an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Transfer interval successfully set",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Transfer interval could not be set",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@PostMapping("/set-access-point-transfer-interval")
	public RestResponseEntity
	setAPTransferInterval(
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

	@Operation(summary = "Update the information of an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Access Point info successfully updated",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400",
						description = "Access Point info could not be updated",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@AnyPermission({Permission.ADMIN})
	@PostMapping("/update-access-point")
	public RestResponseEntity
	updateAccessPoint(
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

	@Operation(summary = "Transfer data from Access Point to Server")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Data successfully transfered",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Data could not be transfered",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@PostMapping("/transfer-data")
	@PrincipalRequired(AccessPoint.class)
	public RestResponseEntity
	transferData(
			final AccessPoint accessPoint, @RequestBody final List<SensorStation> sensorStationList
	) {
		try {
			accessPointService.setLastConnection(accessPoint);
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

	@Operation(summary = "Delete an Access Point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Access Point successfully deleted",
				content = @Content(schema = @Schema(implementation = MessageResponse.class))
		)
		,
				@ApiResponse(
						responseCode = "400", description = "Access Point could not be deleted",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@AnyPermission({Permission.ADMIN})
	@DeleteMapping("/delete-access-point")
	@WriteOperation
	public RestResponseEntity
	deleteAccessPoint(@RequestParam("accessPointId") final UUID accessPointId) {
		try {
			accessPointService.deleteAccessPoint(accessPointId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder().statusCode(200).message("Deleted AccessPoint").toEntity();
	}

	@Operation(summary = "Get all sensor stations of an access point")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200", description = "Sensor stations successfully retrieved",
				content = @Content(
						schema = @Schema(implementation = AdminSensorStationsResponse.class)
				)
		)
		,
				@ApiResponse(
						responseCode = "400",
						description = "Sensor stations could not be retrieved",
						content = @Content(schema = @Schema(implementation = MessageResponse.class))
				)
	})
	@ReadOperation
	@AnyPermission({Permission.ADMIN})
	@GetMapping("/get-access-point-sensor-stations")
	public RestResponseEntity
	getAccessPointSensorStations(@RequestParam("accessPointId") final UUID accessPointId) {
		try {
			return new AdminSensorStationsResponse(
						   accessPointService.getAccessPointSensorStations(accessPointId)
			)
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}
}

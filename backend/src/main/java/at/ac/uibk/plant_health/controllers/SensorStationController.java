package at.ac.uibk.plant_health.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.SensorStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class SensorStationController {
	@Autowired
	private SensorStationService sensorStationService;

	@Operation(summary = "Get all available sensor stations to be added to dashboard")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station data",
			content = @Content(schema = @Schema(implementation = SensorStationsResponse.class))
	)
	@ReadOperation
	@AnyPermission({Permission.ADMIN, Permission.GARDENER, Permission.USER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-stations")
	public RestResponseEntity
	getSensorStations(Person person) {
		try {
			return new SensorStationsResponse(sensorStationService.findAll(), person).toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Get all sensor stations")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station data",
			content = @Content(schema = @Schema(implementation = AdminSensorStationsResponse.class))
	)
	@ReadOperation
	@AnyPermission({Permission.ADMIN})
	@GetMapping("/get-all-sensor-stations")
	public RestResponseEntity
	gewtAllSensorStations() {
		try {
			return new AdminSensorStationsResponse(sensorStationService.findAll()).toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}
	@Operation(summary = "Get sensor station by id")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station data",
			content = @Content(schema = @Schema(implementation = SensorStationResponse.class))
	)
	@ReadOperation
	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-station")
	public RestResponseEntity
	getSensorStation(Person person, @RequestParam("sensorStationId") final UUID sensorStationId) {
		try {
			return new SensorStationResponse(sensorStationService.findById(sensorStationId), person)
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Get sensor station by id")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station data",
			content = @Content(schema = @Schema(implementation = SensorStationPublicInfo.class))
	)
	@ReadOperation
	@PublicEndpoint
	@GetMapping("/get-sensor-station-info")
	public RestResponseEntity
	getSensorStationInfo(@RequestParam("sensorStationId") final UUID sensorStationId) {
		try {
			sensorStationService.findById(sensorStationId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return new SensorStationPublicInfo(sensorStationService.findById(sensorStationId))
				.toEntity();
	}

	@Operation(summary = "Set unlock status of sensor station")
	@ApiResponse(
			responseCode = "200", description = "Successfully set unlock status of sensor station",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@WriteOperation
	@AnyPermission(Permission.ADMIN)
	@RequestMapping(
			value = "/set-unlocked-sensor-station", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setLockSensorStation(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam("unlocked") final boolean unlocked
	) {
		try {
			sensorStationService.findById(sensorStationId);
			sensorStationService.setUnlocked(unlocked, sensorStationId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder().statusCode(200).toEntity();
	}

	@Operation(summary = "Update sensor station name and trasnfer interval")
	@ApiResponse(
			responseCode = "200", description = "Successfully updated sensor station",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@WriteOperation
	@AnyPermission({Permission.GARDENER, Permission.ADMIN})
	@PrincipalRequired(Person.class)
	@RequestMapping(
			value = "/update-sensor-station", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setSensorLimits(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam(value = "sensorStationName", required = false) final String name,
			@RequestParam(value = "transferInterval", required = false)
			final Integer transferInterval, @RequestBody final List<SensorLimits> sensorLimits
	) {
		try {
			SensorStation sensorStation = sensorStationService.findById(sensorStationId);
			sensorStationService.updateSensorStation(sensorStation, name, transferInterval);
			sensorStationService.setSensorLimits(sensorLimits, sensorStation, person);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully updated Sensor Station")
				.toEntity();
	}

	@Operation(summary = "Assign gardener to sensor station")
	@ApiResponse(
			responseCode = "200", description = "Successfully assigned gardener to sensor station",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@WriteOperation
	@AnyPermission(Permission.ADMIN)
	@PostMapping("/assign-gardener-to-sensor-station")
	public RestResponseEntity
	assignGardenerToSensorStation(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam(value = "gardenerId", required = false) final UUID gardenerId,
			@RequestParam(value = "delete", required = false) final boolean delete
	) {
		try {
			sensorStationService.assignGardenerToSensorStation(
					sensorStationService.findById(sensorStationId), gardenerId, delete
			);
			return MessageResponse.builder()
					.statusCode(200)
					.message("Successfully assigned gardener to sensor station")
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Get sensor station data")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station data",
			content = @Content(schema = @Schema(implementation = SensorStationDataResponse.class))
	)
	@ReadOperation
	@AnyPermission({Permission.GARDENER, Permission.ADMIN, Permission.USER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-station-data")
	public RestResponseEntity
	getSensorStationData(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId,
			@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") @RequestParam("from"
			) final LocalDateTime from,
			@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") @RequestParam("to"
			) final LocalDateTime to
	) {
		try {
			sensorStationService.isDeleted(sensorStationService.findById(sensorStationId));
			return new SensorStationDataResponse(
						   sensorStationService.findById(sensorStationId), from, to
			)
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Upload sensor station picture")
	@ApiResponse(
			responseCode = "200", description = "Successfully uploaded sensor station picture",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@PublicEndpoint
	@WriteOperation
	@RequestMapping(
			value = "/upload-sensor-station-picture",
			method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	uploadPicture(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam("picture") final MultipartFile picture
	) {
		try {
			sensorStationService.findById(sensorStationId);
			sensorStationService.uploadPicture(picture, sensorStationId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder().statusCode(200).toEntity();
	}

	@Operation(summary = "Get sensor station pictures")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station pictures",
			content = @Content(schema = @Schema(implementation = PlantPicturesResponse.class))
	)
	@PublicEndpoint
	@ReadOperation
	@GetMapping("/get-sensor-station-pictures")
	public RestResponseEntity
	getSensorStationPictures(@RequestParam("sensorStationId") final UUID sensorStationId) {
		try {
			sensorStationService.findById(sensorStationId);
			return new PlantPicturesResponse(
						   sensorStationService.getPictures(sensorStationId),
						   sensorStationService.findById(sensorStationId)
			)
					.toEntity();

		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Get sensor station picture")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved sensor station picture",
			content = @Content(schema = @Schema(implementation = byte[].class))
	)
	@PublicEndpoint
	@ReadOperation
	@GetMapping(
			value = "/get-sensor-station-picture",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
	)
	public ResponseEntity<byte[]>
	getSensorStationPicture(@RequestParam("pictureId") final UUID pictureId) {
		try {
			SensorStationPicture picture = sensorStationService.getPicture(pictureId);
			String extension = picture.getPicturePath().split("\\.")[1];
			String name = picture.getPicturePath().split("\\.")[0];
			byte[] contents = sensorStationService.convertPictureToByteArray(picture);

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("image/" + extension))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + name + "\"")
					.body(contents);
		} catch (ServiceException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		}
	}

	@Operation(summary = "Get newest sensor station picture")
	@ApiResponse(
			responseCode = "200",
			description = "Successfully retrieved newest sensor station picture",
			content = @Content(schema = @Schema(implementation = byte[].class))
	)
	@AnyPermission({Permission.ADMIN, Permission.GARDENER, Permission.USER})
	@ReadOperation
	@GetMapping(
			value = "/get-newest-sensor-station-picture",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
	)
	public ResponseEntity<byte[]>
	getNewestSensorStationPicture(@RequestParam("sensorStationId") final UUID sensorStationId)
			throws Exception {
		try {
			sensorStationService.findById(sensorStationId);
			SensorStationPicture picture = sensorStationService.getNewestPicture(sensorStationId);

			String extension = picture.getPicturePath().split("\\.")[1];
			String name = picture.getPicturePath().split("\\.")[0];
			byte[] contents = sensorStationService.convertPictureToByteArray(picture);

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("image/" + extension))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + name + "\"")
					.body(contents);
		} catch (ServiceException e) {
			return ResponseEntity.status(e.getStatusCode()).build();
		}
	}

	@Operation(summary = "Delete sensor station picture")
	@ApiResponse(
			responseCode = "200", description = "Successfully deleted sensor station picture",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@WriteOperation
	@PostMapping("/delete-sensor-station-picture")
	public RestResponseEntity
	deleteSensorStationPicture(@RequestParam("pictureId") final UUID pictureId) {
		try {
			sensorStationService.deletePicture(pictureId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Delete all pictures " + pictureId)
				.toEntity();
	}

	@Operation(summary = "Delete all sensor station pictures")
	@ApiResponse(
			responseCode = "200", description = "Successfully deleted all sensor station pictures",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@WriteOperation
	@PostMapping("/delete-all-sensor-station-pictures")
	public RestResponseEntity
	deleteAllSensorStationPictures(@RequestParam("sensorStationId") final UUID sensorStationId) {
		try {
			sensorStationService.findById(sensorStationId);
			sensorStationService.deleteAllPictures(sensorStationId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Delete all pictures of SensorStation " + sensorStationId)
				.toEntity();
	}

	@Operation(summary = "Delete sensor station")
	@ApiResponse(
			responseCode = "200", description = "Successfully deleted sensor station",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@AnyPermission({Permission.ADMIN})
	@WriteOperation
	@DeleteMapping("/delete-sensor-station")
	public RestResponseEntity
	deleteSensorStation(@RequestParam("sensorStationId") final UUID sensorStationId) {
		try {
			sensorStationService.deleteSensorStation(sensorStationId);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Deleted SensorStation " + sensorStationId)
				.toEntity();
	}
}

package at.ac.uibk.plant_health.controllers;

import org.hibernate.annotations.Any;
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

@RestController
public class SensorStationController {
	@Autowired
	private SensorStationService sensorStationService;

	@AnyPermission({Permission.ADMIN, Permission.GARDENER, Permission.USER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-stations")
	public RestResponseEntity getSensorStations(Person person) {
		if (person.getPermissions().contains(Permission.ADMIN)
			|| person.getPermissions().contains(Permission.GARDENER)) {
			return new SensorStationsResponse(sensorStationService.findAll()).toEntity();
		}
		return new UserSensorStationsResponse(sensorStationService.findAll(), person).toEntity();
	}

	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-station")
	public RestResponseEntity getSensorStation(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId
	) {
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
	@PublicEndpoint
	@GetMapping("/get-sensor-station-info")
	public RestResponseEntity getSensorStationInfo(@RequestParam("sensorStationId"
	) final UUID sensorStationId) {
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

	@AnyPermission({Permission.GARDENER, Permission.ADMIN})
	@PrincipalRequired(Person.class)
	@RequestMapping(
			value = "/update-sensor-station", method = {RequestMethod.POST, RequestMethod.PUT}
	)
	public RestResponseEntity
	setSensorLimits(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam(value = "sensorStationName", required = false) final String name,
			@RequestBody final List<SensorLimits> sensorLimits
	) {
		try {
			SensorStation sensorStation = sensorStationService.findById(sensorStationId);
			sensorStationService.updateSensorStation(sensorStation, name);
			sensorStationService.setSensorLimits(sensorLimits, sensorStation, person);
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}

		return MessageResponse.builder()
				.statusCode(200)
				.message("Successfully set sensor limits")
				.toEntity();
	}

	@AnyPermission(Permission.ADMIN)
	@PostMapping("/assign-gardener-to-sensor-station")
	public RestResponseEntity assignGardenerToSensorStation(
			@RequestParam("sensorStationId") final UUID sensorStationId,
			@RequestParam("gardenerId") final UUID gardenerId
	) {
		try {
			sensorStationService.assignGardenerToSensorStation(
					sensorStationService.findById(sensorStationId), gardenerId
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

	@AnyPermission({Permission.GARDENER, Permission.ADMIN, Permission.USER})
	@PrincipalRequired(Person.class)
	@GetMapping("/get-sensor-station-data")
	public RestResponseEntity getSensorStationData(
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

	@PublicEndpoint
	@ReadOperation
	@GetMapping("/get-sensor-station-pictures")
	public RestResponseEntity getSensorStationPictures(@RequestParam("sensorStationId"
	) final UUID sensorStationId) {
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

	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@WriteOperation
	@PostMapping("/delete-sensor-station-picture")
	public RestResponseEntity deleteSensorStationPicture(@RequestParam("pictureId"
	) final UUID pictureId) {
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

	@AnyPermission({Permission.ADMIN, Permission.GARDENER})
	@WriteOperation
	@PostMapping("/delete-all-sensor-station-pictures")
	public RestResponseEntity deleteAllSensorStationPictures(@RequestParam("sensorStationId"
	) final UUID sensorStationId) {
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

	@AnyPermission({Permission.ADMIN})
	@WriteOperation
	@DeleteMapping("/delete-sensor-station")
	public RestResponseEntity deleteSensorStation(@RequestParam("sensorStationId") final UUID sensorStationId) {
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
package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationPersonReferenceService;
import at.ac.uibk.plant_health.service.SensorStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class DashBoardController {
	@Autowired
	private PersonService personService;
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private SensorStationPersonReferenceService sensorStationPersonReferenceService;
	@Autowired
	private AccessPointService accessPointService;

	@Operation(summary = "Get dashboard data")
	@ApiResponse(
			responseCode = "200", description = "Successfully retrieved dashboard data",
			content = @Content(schema = @Schema(implementation = DashBoardDataResponse.class))
	)
	@ReadOperation
	@PrincipalRequired(Person.class)
	@GetMapping("/get-dashboard")
	public RestResponseEntity
	getDashboard(Person person) {
		try {
			if (person.getPermissions().contains(Permission.ADMIN))
				return new AdminDashBoardResponse(
							   sensorStationService.findAll(),
							   accessPointService.findAllAccessPoints(), personService.getPersons(),
							   person
				)
						.toEntity();
			if (person.getPermissions().contains(Permission.GARDENER)) {
				return new GardenerDashBoardResponse(sensorStationService.findAll(), person)
						.toEntity();
			}

			return new DashBoardDataResponse(person).toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Add sensor station to dashboard")
	@ApiResponse(
			responseCode = "200", description = "Successfully added sensor station to dashboard",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@WriteOperation
	@PrincipalRequired(Person.class)
	@RequestMapping(value = "/add-to-dashboard", method = {RequestMethod.PUT, RequestMethod.POST})
	public RestResponseEntity
	addPlantToDashboard(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId
	) {
		try {
			sensorStationPersonReferenceService.addPlantToDashboard(
					person, sensorStationService.findById(sensorStationId)
			);
			return MessageResponse.builder()
					.statusCode(200)
					.message("Successfully added sensor station to dashboard")
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}

	@Operation(summary = "Remove sensor station from dashboard")
	@ApiResponse(
			responseCode = "200",
			description = "Successfully removed sensor station from dashboard",
			content = @Content(schema = @Schema(implementation = MessageResponse.class))
	)
	@DeleteOperation
	@PrincipalRequired(Person.class)
	@RequestMapping(
			value = "/remove-from-dashboard", method = {RequestMethod.DELETE, RequestMethod.POST}
	)
	public RestResponseEntity
	removeFromDashboard(
			Person person, @RequestParam("sensorStationId") final UUID sensorStationId
	) {
		try {
			sensorStationPersonReferenceService.removePlantFromDashboard(
					person, sensorStationService.findById(sensorStationId)
			);
			return MessageResponse.builder()
					.statusCode(200)
					.message("Successfully removed sensor station from dashboard")
					.toEntity();
		} catch (ServiceException e) {
			return MessageResponse.builder()
					.statusCode(e.getStatusCode())
					.message(e.getMessage())
					.toEntity();
		}
	}
}

package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
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

	@PrincipalRequired(Person.class)
	@GetMapping("/get-dashboard-data")
	public RestResponseEntity getDashboardData(Person person) {
		return new DashBoardDataResponse(person).toEntity();
	}

	@PrincipalRequired(Person.class)
	@GetMapping("/get-dashboard")
	public RestResponseEntity getDashboard(Person person) {
		try {
			if (person.getPermissions().contains(Permission.ADMIN))
				return new AdminDashBoardResponse(
							   sensorStationService.findAll(),
							   accessPointService.findAllAccessPoints(), personService.getPersons()
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

	@PrincipalRequired(Person.class)
	@RequestMapping(value = "/add-to-dashboard", method = {RequestMethod.PUT, RequestMethod.POST})
	public RestResponseEntity addPlantToDashboard(
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

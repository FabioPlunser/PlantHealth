package at.ac.uibk.plant_health.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Person;
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

	@PrincipalRequired(Person.class)
	@GetMapping("/get-dashboard-data")
	public RestResponseEntity getDashboardData(Person person) {
		return new DashBoardDataResponse(person).toEntity();
	}

	@PrincipalRequired(Person.class)
	@RequestMapping(value = "/add-to-dashboard", method = {RequestMethod.PUT, RequestMethod.POST})
	public RestResponseEntity addPlantToDashboard(
			Person person, @RequestParam("plant-id") final UUID plantId
	) {
		//		var maybeSensorStation = this.sensorStationService.findById(plantId);
		//		if (
		//			maybeSensorStation.isPresent() &&
		//			this.sensorStationPersonReferenceService.addPlantToDashboard(
		//				person, maybeSensorStation.get()
		//		)) {
		//			return MessageResponse.builder().ok().toEntity();
		//
		//		}
		//
		//		return MessageResponse.builder().notFound().message("Could not find
		// Plant").toEntity();
		throw new NotImplementedException("Need to rethink this");
	}

	@PrincipalRequired(Person.class)
	@DeleteMapping(value = "/remove-from-dashboard")
	public RestResponseEntity removeFromDashboard(
			Person person, @RequestParam("plant-id") final UUID plantId
	) {
		//		var maybeSensorStation = this.sensorStationService.findById(plantId);
		//		if (
		//			maybeSensorStation.isPresent() &&
		//			this.sensorStationPersonReferenceService.removePlantFromDashboard(
		//				person, maybeSensorStation.get()
		//		)) {
		//			return MessageResponse.builder().ok().toEntity();
		//		}
		//
		//		return MessageResponse.builder().notFound().message("Could not find
		// Plant").toEntity();
		throw new NotImplementedException("Need to rethink this");
	}
}

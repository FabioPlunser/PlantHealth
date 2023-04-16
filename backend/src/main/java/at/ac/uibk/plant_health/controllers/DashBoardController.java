package at.ac.uibk.plant_health.controllers;

import at.ac.uibk.plant_health.models.rest_responses.DashBoardDataResponse;
import at.ac.uibk.plant_health.models.rest_responses.ListResponse;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Person;

@RestController
public class DashBoardController {
	@Autowired
	private PersonRepository personRepository;

	@PrincipalRequired(Person.class)
	@GetMapping("/get-dashboard-data")
	public RestResponseEntity getDashboardData(Person person) {
		return new DashBoardDataResponse(person).toEntity();
	}

	@PrincipalRequired(Person.class)
	@RequestMapping(value = "/add-to-dashboard", method = {RequestMethod.PUT, RequestMethod.POST})
	public RestResponseEntity addPlantToDashboard(Person person
												  //            @RequestBody final UUID id
	) {
		throw new NotImplementedException();
	}

	@PrincipalRequired(Person.class)
	@RequestMapping(value = "/remove-from-dashboard", method = RequestMethod.DELETE)
	public RestResponseEntity removePlantFromDashboard(Person person
													   //            @RequestBody final UUID id
	) {
		throw new NotImplementedException();
	}
}

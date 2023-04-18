package at.ac.uibk.plant_health.controllers;

import static at.ac.uibk.plant_health.util.EndpointMatcherUtil.LOGIN_ENDPOINT;
import static at.ac.uibk.plant_health.util.EndpointMatcherUtil.LOGOUT_ENDPOINT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.rest_responses.LoginResponse;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonService;

/**
 * Controller handling the login-, and logout-process.
 *
 * @author David Rieser
 * @see at.ac.uibk.plant_health.util.EndpointMatcherUtil
 */
@SuppressWarnings("unused")
@RestController
public class LoginController {
	@Autowired
	private PersonService personService;

	/**
	 * Endpoint for the Front-End to request an Authentication Token.
	 *
	 * @param username The username of the User to create the Token for.
	 * @param password The password of the User to create the Token for.
	 * @return A Token if the user credentials are correct, otherwise an
	 *     error.
	 */
	@WriteOperation
	@PublicEndpoint
	@GetMapping(value = LOGIN_ENDPOINT)
	public RestResponseEntity getToken(
			@RequestParam("username") final String username,
			@RequestParam("password") final String password
	) {
		return login(username, password);
	}

	/**
	 * Endpoint for the Front-End to request an Authentication Token.
	 *
	 * @param username The username of the User to create the Token for.
	 * @param password The password of the User to create the Token for.
	 * @return A Token if the user credentials are correct, otherwise an
	 *     error.
	 */
	@WriteOperation
	@PublicEndpoint
	@PostMapping(value = LOGIN_ENDPOINT)
	public RestResponseEntity loginPost(
			@RequestParam("username") final String username,
			@RequestParam("password") final String password
	) {
		return login(username, password);
	}

	private RestResponseEntity login(final String username, final String password) {
		Optional<Person> maybePerson = personService.login(username, password);

		if (maybePerson.isEmpty()) {
			return MessageResponse.builder()
					.statusCode(HttpStatus.UNAUTHORIZED)
					.message("Username or Password is wrong!")
					.toEntity();
		}

		return LoginResponse.builder().ok().person(maybePerson.get()).toEntity();
	}

	/**
	 * Endpoint for the Front-End to logout.
	 * This deletes the Authentication Token stored in the database.
	 *
	 * @return A Message saying whether the Logout was successful or not.
	 */
	@DeleteOperation
	@PrincipalRequired(Person.class)
	@PostMapping(LOGOUT_ENDPOINT)
	public RestResponseEntity deleteToken(Person person) {
		if (!personService.logout(person)) {
			return MessageResponse.builder()
					.statusCode(HttpStatus.UNAUTHORIZED)
					.message("No matching Token!")
					.toEntity();
		}

		return MessageResponse.builder().ok().message("Successfully logged out!").toEntity();
	}
}
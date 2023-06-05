package at.ac.uibk.plant_health.controllers;

import static at.ac.uibk.plant_health.util.EndpointMatcherUtil.REGISTER_ENDPOINT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import at.ac.uibk.plant_health.models.annotations.AnyPermission;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.rest_responses.*;
import at.ac.uibk.plant_health.models.user.Authenticable;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonService;

/**
 * Controller handling {@link Person} related Information (e.g. creating,
 * updating, deleting {@link Person})
 *
 * @author David Rieser
 */
@SuppressWarnings("unused")
@RestController
public class PersonController {
	// region Autowired Components
	@Autowired
	private PersonService personService;
	// endregion

	// region User Creation Endpoints
	/**
	 * User Registration Endpoint for Users to create an Account by
	 * themselves.
	 *
	 * @param username The new Users username.
	 * @param password The new Users password (needs to be already hashed).
	 * @param email The new Users email.
	 * @return A RestResponse indicating whether the user could be created
	 *     or not.
	 */
	@WriteOperation
	@PublicEndpoint
	@PostMapping(REGISTER_ENDPOINT)
	public RestResponseEntity register(
			@RequestParam("username") final String username,
			@RequestParam("password") final String password,
			@RequestParam("email") final String email
	) {
		UUID token = UUID.randomUUID();
		Person person =
				new Person(username, email, password, token, (Set) Permission.defaultPermissions());

		return createUser(person);
	}

	/**
	 * User Creation Endpoint for Admins to manually create Accounts.
	 *
	 * @param username The new Users username.
	 * @param password The new Users password (needs to be already hashed).
	 * @param email The new Users email.
	 * @param permissions The Permissions the new User should have.
	 * @return A RestResponse indicating whether the user could be created
	 *     or not.
	 */
	@WriteOperation
	@AnyPermission(Permission.ADMIN)
	@PostMapping("/create-user")
	public RestResponseEntity create(
			@RequestParam("username") final String username,
			@RequestParam("password") final String password,
			@RequestParam("email") final String email,
			@RequestParam("permissions") final Set<Permission> permissions
	) {
		Person person = new Person(username, email, password, (Set) permissions);

		return createUser(person);
	}

	/**
	 * Helper Function for saving a Person and returning a corresponding
	 * RestResponse.
	 *
	 * @param person The Person to save.
	 * @return A RestResponse indicating whether the operation was
	 *     successful or not.
	 */
	private RestResponseEntity createUser(Person person) {
		if (!personService.create(person))
			return MessageResponse.builder()
					.statusCode(HttpStatus.BAD_REQUEST)
					.message("Could not create User - Username already exists!")
					.toEntity();

		return CreatedUserResponse.builder().person(person).toEntity();
	}
	// endregion

	// region Update User Endpoints
	/**
	 * Endpoint for a User to change his Account Settings.
	 *
	 * @param person Auto-injected Instance of the currently logged-in Person.
	 * @param username The new username
	 * @param email The new email
	 * @param password The new Password
	 * @return A RESTResponse indicating Success
	 */
	@WriteOperation
	@PrincipalRequired(Person.class)
	@PostMapping("/update-settings")
	public RestResponseEntity updateSettings(
			Person person, @RequestParam(name = "username", required = false) final String username,
			@RequestParam(name = "email", required = false) final String email,
			@RequestParam(name = "password", required = false) final String password
	) {
		UUID personId = person.getPersonId();
		Set<GrantedAuthority> permissions = person.getPermissions();
		return updatePerson(
				personId, username, email, Permission.fromAuthorities(permissions), password
		);
	}

	/**
	 * Endpoint for Admins to change/update a user
	 *
	 * @param personId The ID of the User to update
	 * @param username The new username
	 * @param email The new email
	 * @param password The new Password
	 * @param permissions The new Permissions
	 * @return A RESTResponse indicating Success
	 */
	@WriteOperation
	@AnyPermission(Permission.ADMIN)
	@RequestMapping(value = "/update-user", method = {RequestMethod.POST, RequestMethod.PUT})
	public RestResponseEntity updateUser(
			@RequestParam(name = "personId") final UUID personId,
			@RequestParam(name = "username", required = false) final String username,
			@RequestParam(name = "email", required = false) final String email,
			@RequestParam(name = "permissions", required = false) final Set<Permission> permissions,
			@RequestParam(name = "password", required = false) final String password
	) {
		return updatePerson(personId, username, email, permissions, password);
	}

	private RestResponseEntity updatePerson(
			final UUID personId, final String username, final String email,
			final Set<Permission> permissions, final String password
	) {
		if (personService.update(personId, username, email, permissions, password))
			return MessageResponse.builder()
					.ok()
					.message("User " + personId + " updated successfully!")
					.toEntity();

		return MessageResponse.builder()
				.notFound()
				.message("Could not update User " + personId + " - User does not exist!")
				.toEntity();
	}
	// endregion

	// region Delete User Endpoints
	/**
	 * Endpoint for Admins to delete a user.
	 *
	 * @param personId The ID of the User to delete
	 * @return A RestResponse indicating whether the operation was
	 *     successful or not.
	 */
	@DeleteOperation
	@AnyPermission(Permission.ADMIN)
	@DeleteMapping("/delete-user")
	public RestResponseEntity deleteUser(@RequestParam("personId") final UUID personId) {
		if (personService.delete(personId))
			return MessageResponse.builder()
					.ok()
					.message("User " + personId + " deleted successfully!")
					.toEntity();

		return MessageResponse.builder()
				.notFound()
				.message("Could not delete User " + personId + " - User does not exist!")
				.toEntity();
	}
	// endregion

	// region GET Endpoints
	/**
	 * Endpoint for Admins to get all users, except the calling user himself.
	 *
	 * @return A RestReponse containing a List of all users.
	 */
	@ReadOperation
	@PrincipalRequired(Person.class)
	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-all-users")
	public RestResponse getAllUsers(Person person) {
		return new ListResponse<>(
				personService.getPersons().stream().filter(p -> !p.equals(person)).toList()
		);
	}

	@ReadOperation
	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-all-gardener")
	public RestResponse getAllGardener() {
		return new ListResponse<>(personService.getGardener());
	}
	/**
	 * Endpoint for Admins to get all possible Permission so that they don't
	 * need to be changed manually on frontend.
	 *
	 * @return A List of all possible Permissions.
	 */
	@ReadOperation
	@AnyPermission(Permission.ADMIN)
	@GetMapping("/get-all-permissions")
	public RestResponse getAllPermissions() {
		return new ListResponse<>(Stream.of(Permission.values()).map(Enum::name).toList());
	}

	@ReadOperation
	@PrincipalRequired(Authenticable.class)
	@GetMapping("/get-user-permissions")
	public RestResponseEntity getUserPermissions(Person person) {
		return PermissionResponse.builder()
				.ok()
				.permissions(person.getPermissions().toArray(GrantedAuthority[] ::new))
				.toEntity();
	}
	// endregion
}

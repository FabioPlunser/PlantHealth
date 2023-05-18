package at.ac.uibk.plant_health.service.person_service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
public class TestPersonServiceGeneral {
	@Autowired
	private PersonService personService;

	@Test
	public void saveAndGetPersons() {
		// given: some demo users stored in database
		int numberOfDemoPersons = 20;
		List<Person> savedPersons = new ArrayList<>();
		for (int i = 0; i < numberOfDemoPersons; i++) {
			Person person = new Person(
					StringGenerator.username(), StringGenerator.email(), StringGenerator.password(),
					Set.of()
			);
			savedPersons.add(person);
			assertTrue(personService.create(person), "Unable to create user " + person);
		}

		// when: retrieving all demo users from database
		List<Person> foundPersons = personService.getPersons();

		// then: all saved users must be found again and attributes must be
		// identical
		for (Person person : savedPersons) {
			assertTrue(foundPersons.contains(person), "Could not find person " + person);
			Person foundPerson = foundPersons.get(foundPersons.indexOf(person));
			assertEquals(person.getPersonId(), foundPerson.getPersonId(), "Wrong id of " + person);
			assertEquals(
					person.getUsername(), foundPerson.getUsername(), "Wrong username of " + person
			);
			assertEquals(person.getEmail(), foundPerson.getEmail(), "Wrong email of " + person);
			assertEquals(
					person.getPermissions(), foundPerson.getPermissions(),
					"Wrong permissions of " + person
			);
		}
	}

	@Test
	public void createTwoIdenticalUsernames() {
		// given: one demo user already in the database
		String username = StringGenerator.username();
		Person person =
				new Person(username, StringGenerator.email(), StringGenerator.password(), Set.of());
		assertTrue(personService.create(person), "Unable to create user");

		// when: creating a second user with exactly the same username
		Person duplicatePerson =
				new Person(username, StringGenerator.email(), StringGenerator.password(), Set.of());

		// then: it should not be possible to create that second user
		assertFalse(
				personService.create(duplicatePerson),
				"Second user with identical username was created"
		);
	}

	@Test
	public void getPersonById() {
		// given: demo user in database
		Person person = new Person(
				StringGenerator.username(), StringGenerator.email(), StringGenerator.password(),
				Set.of()
		);
		assertTrue(personService.create(person), "Unable to create user for test");
		UUID id = person.getPersonId();

		// when: retrieving user from database by id
		Optional<Person> maybePerson = personService.findById(id);

		// then: retrieved user must be correct
		assertTrue(maybePerson.isPresent(), "Did not find user by id");
		assertEquals(
				person, maybePerson.get(),
				"Got user " + maybePerson.get() + " when user " + person + " was expected"
		);
	}

	@Test
	public void updatePerson() {
		// given: demo user in database
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		String email = StringGenerator.email();
		Person person = new Person(username, email, password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");

		// when: updating the person
		String newUsername = StringGenerator.username();
		String newPassword = StringGenerator.password();
		Set<Permission> newPermissions = Set.of(Permission.ADMIN, Permission.USER);
		assertTrue(
				personService.update(person, newUsername, null, newPassword, newPermissions),
				"Could not update user"
		);

		// then: logging in should be possible with new credentials only and
		// other attributes must be correct
		Optional<Person> maybePerson = personService.login(newUsername, newPassword);
		assertTrue(maybePerson.isPresent(), "Could not login with new credentials");
		Optional<Person> maybeOldCredentialsPerson = personService.login(username, password);
		assertTrue(maybeOldCredentialsPerson.isEmpty(), "Could still login with old credentials");
		assertEquals(
				newPermissions, maybePerson.get().getPermissions(),
				"Permissions have not been updated"
		);
	}

	@Test
	public void updatePersonViaCreate() {
		// given: demo user in database
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		String email = StringGenerator.email();
		Person person = new Person(username, email, password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");
		UUID id = person.getPersonId();

		// when: changing the person by interfering with the model directly
		person.setUsername("new");
		person.setPassword("new");
		person.setPermissions(Set.of(Permission.USER, Permission.ADMIN));

		// then: saving the modification via create() should not be possible
		assertFalse(personService.create(person), "Changed person got created again");
		Optional<Person> maybePerson = personService.findById(id);
		assertTrue(maybePerson.isPresent(), "Unable to find original person in repository");
		Person foundPerson = maybePerson.get();
		assertEquals(username, foundPerson.getUsername(), "Name was changed");
		assertTrue(personService.login(username, password).isPresent(), "Could not login anymore");
		assertEquals(email, foundPerson.getEmail(), "Email was changed");
		assertEquals(Set.of(), foundPerson.getPermissions(), "Permissions have been changed");
	}

	@Test
	public void deletePerson() {
		// given: demo user in database
		Person person = new Person(
				StringGenerator.username(), StringGenerator.email(), StringGenerator.password(),
				Set.of()
		);
		assertTrue(personService.create(person), "Unable to create user for test");

		// when: deleting that user
		assertTrue(personService.delete(person.getPersonId()), "Could not delete user");

		// then: user should not exist in database anymore
		assertFalse(personService.getPersons().contains(person), "User still in database");
	}
}

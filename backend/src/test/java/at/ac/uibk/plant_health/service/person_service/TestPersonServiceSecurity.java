package at.ac.uibk.plant_health.service.person_service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
public class TestPersonServiceSecurity {
	@Autowired
	private PersonService personService;
	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void loginWithValidCredentials() {
		// given: demo user in database (and additional anonymous user)
		int numberOfOtherPersons = 20;
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user " + person);
		for (int i = 0; i < numberOfOtherPersons; i++) {
			assertTrue(
					personService.create(new Person(
							"otherPerson-TestLoginWithValidCredentials-" + (i + 1),
							StringGenerator.email(), StringGenerator.password(), Set.of()
					)),
					"Unable to create user " + person
			);
		}

		// when: logging in with that users credentials
		Optional<Person> maybePerson = personService.login(username, password);
		assertTrue(maybePerson.isPresent(), "Unable to log in");

		// then: returned user must be correct
		assertEquals(person, maybePerson.get(), "Got the wrong user");
	}

	@Test
	public void loginWithInvalidCredentials() {
		// given: demo user in database
		int numberOfOtherPersons = 20;
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user " + person);

		// when:
		//  logging in with completely wrong credentials
		Optional<Person> maybePersonAllWrong =
				personService.login("wrong-username", "wrong-password");
		//  logging in with wrong username
		Optional<Person> maybePersonUsernameWrong = personService.login("wrong-username", password);
		//  logging in with wrong password
		Optional<Person> maybePersonPasswordWrong = personService.login(username, "wrong-password");

		// then: login should never be possible
		assertTrue(
				maybePersonAllWrong.isEmpty(), "Could login with completely different credentials"
		);
		assertTrue(maybePersonUsernameWrong.isEmpty(), "Could login with wrong username");
		assertTrue(maybePersonPasswordWrong.isEmpty(), "Could login with wrong password");
	}

	@Test
	public void getPersonByToken() {
		// given: demo user in database
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");

		// when: logging in as user and retrieving token
		Optional<Person> maybePerson = personService.login(username, password);
		System.out.println(personRepository.findAll()
								   .stream()
								   .map(p -> String.format("%s %s", p.getUsername(), p.getToken()))
								   .collect(Collectors.joining(",")));
		assertTrue(maybePerson.isPresent(), "Could not login");
		UUID token = maybePerson.get().getToken();

		// then: user returned by handing over token must be original user
		Optional<Person> maybePersonByToken = personService.findByUsernameAndToken(username, token);
		assertTrue(maybePersonByToken.isPresent(), "Did not find user by token");
		assertEquals(
				person, maybePersonByToken.get(),
				"Got user " + maybePersonByToken.get() + " when user " + person + " was expected"
		);
	}

	@Test
	public void logout() {
		// given: demo user in database, logged in
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");
		Optional<Person> maybePerson = personService.login(username, password);
		assertTrue(maybePerson.isPresent(), "Could not login");
		Person loggedInPerson = maybePerson.get();
		UUID token = loggedInPerson.getToken();
		MockAuthContext.setLoggedInUser(loggedInPerson);

		// when: logging out
		assertTrue(personService.logout(person), "Could not log out");

		// then: retrieving user by token should not be possible anymore
		Optional<Person> maybeLoggedOutPerson =
				personService.findByUsernameAndToken(username, token);
		assertTrue(maybeLoggedOutPerson.isEmpty(), "Token still valid after logout");
	}

	@Test
	public void logoutWithToken() {
		// given: demo user in database, logged in
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");
		Optional<Person> maybePerson = personService.login(username, password);
		assertTrue(maybePerson.isPresent(), "Could not login");
		UUID token = maybePerson.get().getToken();
		MockAuthContext.setLoggedInUser(maybePerson.get());

		// when: logging out with token directly
		assertTrue(personService.logout(person), "Could not log out");

		// then: retrieving user by token should not be possible anymore
		Optional<Person> maybeLoggedOutPerson =
				personService.findByUsernameAndToken(username, token);
		assertTrue(maybeLoggedOutPerson.isEmpty(), "Token still valid after logout");
	}

	@Test
	public void testPasswordHashingWithNullUpdate() {
		// given: demo user in database, logged in
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");
		person = personService.findById(person.getId()).get();

		// when: saving user again using old Password
		String hashedPassword = person.getPassword();
		assertTrue(
				personService.update(person, null, null, null), "Unable to create user for test"
		);

		// then: password is not hashed again
		assertEquals(hashedPassword, person.getPassword());
		// then: The Password still matches
		assertTrue(passwordEncoder.matches(password, hashedPassword));
		assertTrue(passwordEncoder.matches(password, person.getPassword()));
	}

	@Test
	public void testPasswordHashingWithUpdate() {
		// given: demo user in database, logged in
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Person person = new Person(username, StringGenerator.email(), password, Set.of());
		assertTrue(personService.create(person), "Unable to create user for test");
		person = personService.findById(person.getId()).get();

		// when: saving user again using new Password
		String hashedPassword = person.getPassword();
		assertTrue(
				personService.update(person, null, password, null), "Unable to create user for test"
		);

		// then: password is hashed again
		assertNotEquals(hashedPassword, person.getPassword());
		// then: The Password still matches
		assertTrue(passwordEncoder.matches(password, hashedPassword));
		assertTrue(passwordEncoder.matches(password, person.getPassword()));
	}
}

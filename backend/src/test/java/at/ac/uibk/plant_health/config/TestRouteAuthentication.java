package at.ac.uibk.plant_health.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.config.controller.TestController;
import at.ac.uibk.plant_health.models.user.Authenticable;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestRouteAuthentication {
	// region Autowired + Fields
	@Autowired
	private PersonService personService;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PersonRepository personRepository;

	@Value("${swa.token.expiration-duration:1h}")
	private Duration tokenExpirationDuration;

	private String TEST_ANONYMOUS_ENDPOINT() {
		return TestController.TEST_PERMISSION_ANONYMOUS;
	}
	private String TEST_API_ENDPOINT() {
		return TestController.TEST_PERMISSION_API;
	}
	private String TEST_ADMIN_ENDPOINT() {
		return TestController.TEST_PERMISSION_ADMIN;
	}
	// endregion

	// region Helper Methods
	private Person createUserWithToken() {
		return createUserWithToken(false);
	}

	private Person createUserWithToken(boolean alsoAdmin) {
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Set<GrantedAuthority> permissions =
				alsoAdmin ? Permission.allAuthorities() : Permission.defaultAuthorities();
		Person person = new Person(
				username, StringGenerator.email(), password, UUID.randomUUID(), permissions
		);
		assertTrue(personService.create(person), "Unable to create user");
		return person;
	}
	// endregion

	// region Anonymous Route Tests
	@Test
	public void testAnonymousAccessingAnonymousRouteWithOutCredentials() throws Exception {
		// given: No created Persons

		// when: Accessing an Anonymous Route without Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ANONYMOUS_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}

	@Test
	public void testAnonymousAccessingAnonymousRouteWithCredentials() throws Exception {
		// given: A Person not stored in the database
		Person notSavedPerson = new Person("", "", "", UUID.randomUUID(), Set.of());

		// when: Accessing an Anonymous Page with Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ANONYMOUS_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(notSavedPerson))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}

	@Test
	public void testUserAccessingAnonymousRoute() throws Exception {
		// given: A Person without Admin-Permission
		Person person = createUserWithToken(false);

		// when: Accessing an Anonymous Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ANONYMOUS_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}

	@Test
	public void testAdminAccessingAnonymousRoute() throws Exception {
		// given: A Person with Admin-Permission
		Person person = createUserWithToken(true);

		// when: Accessing an Anonymous Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ANONYMOUS_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}
	// endregion

	// region Api Route Tests
	// region Api Route Tests
	@Test
	public void testAnonymousAccessingApiRouteWithOutCredentials() throws Exception {
		// given: No created Persons

		// when: Accessing an Api Route without Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authentication Exception resulting in a 401 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}

	@Test
	public void testAnonymousAccessingApiRouteWithCredentials() throws Exception {
		// given: A Person not stored in the database
		Person notSavedPerson = new Person("", "", "", UUID.randomUUID(), Set.of());

		// when: Accessing an Admin Page with Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(notSavedPerson))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authentication Exception resulting in a 401 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}

	@Test
	public void testUserAccessingApiRoute() throws Exception {
		// given: A Person without Admin-Permission
		Person person = createUserWithToken(false);

		// when: Accessing an Admin Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}

	@Test
	public void testAdminAccessingApiRoute() throws Exception {
		// given: A Person with Admin-Permission
		Person person = createUserWithToken(true);

		// when: Accessing an Admin Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}
	// endregion
	// endregion

	// region Admin Route Tests
	@Test
	public void testAnonymousAccessingAdminRouteWithOutCredentials() throws Exception {
		// given: No created Persons

		// when: Accessing an Admin Page without Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ADMIN_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authentication Exception resulting in a 401 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}

	@Test
	public void testAnonymousAccessingAdminRouteWithCredentials() throws Exception {
		// given: No created Persons

		// when: Accessing an Admin Page with Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ADMIN_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(AUTHORIZATION,
										AuthGenerator.generateToken(
												StringGenerator.username(), UUID.randomUUID()
										))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authentication Exception resulting in a 401 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}

	@Test
	public void testUserAccessingAdminRoute() throws Exception {
		// given: A Person without Admin-Permission
		Person person = createUserWithToken(false);

		// when: Accessing an Admin Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ADMIN_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(AUTHORIZATION, AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authorization Exception resulting in a 403 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}

	@Test
	public void testAdminAccessingAdminRoute() throws Exception {
		// given: A Person with Admin-Permission
		Person person = createUserWithToken(true);

		// when: Accessing an Admin Page with Credentials
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ADMIN_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(AUTHORIZATION, AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect that the Page is returned
		)
				.andExpectAll(status().isOk());
	}
	// endregion

	// region Test Token Expiration
	private void setTokenCreationDate(Authenticable authenticable, LocalDateTime creationDate)
			throws NoSuchFieldException {
		Field tokenCreationDateField = Authenticable.class.getDeclaredField("tokenCreationDate");
		tokenCreationDateField.setAccessible(true);
		ReflectionUtils.setField(tokenCreationDateField, authenticable, creationDate);
	}

	@Test
	public void testNotExpiredToken() throws Exception {
		// given: A Person created with a Token
		Person person = createUserWithToken();

		// when: Accessing a secured Page with the expired Token
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an OK Response
		)
				.andExpectAll(status().isOk());
	}

	@Test
	public void testExpiredToken() throws Exception {
		// given: A Person created with a Token
		Person person = createUserWithToken();

		// given: Setting the Token Creation Date to be expired
		setTokenCreationDate(person, LocalDateTime.now().minus(tokenExpirationDuration));
		personRepository.updateToken(person);

		// when: Accessing a secured Page with the expired Token
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_API_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Unauthorized Response
		)
				.andExpectAll(status().isUnauthorized());
	}
	// endregion

	// region Test Required Principle
	@Test
	public void test() throws Exception {
		// given: No created Persons

		// when: Accessing an Admin Page without Credentials (anonymous)
		mockMvc.perform(MockMvcRequestBuilders.get(TEST_ADMIN_ENDPOINT())
								.header(USER_AGENT, "MockTests")
								.contentType(MediaType.APPLICATION_JSON)
						// then: Expect an Authentication Exception resulting in a 401 Error Code
		)
				.andExpectAll(status().is(
						Matchers.allOf(Matchers.greaterThan(300), Matchers.lessThan(500))
				));
	}
	// endregion
}
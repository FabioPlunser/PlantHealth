package at.ac.uibk.plant_health.controllers.log_controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.LogRepository;
import at.ac.uibk.plant_health.service.LogService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.LocalDateTimeJsonParser;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestLogController {
	@Autowired
	private LogService logService;
	@Autowired
	private PersonService personService;
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	private static final int TIME_TOLERANCE = 1;
	@Autowired
	private LogRepository logRepository;

	private Person createUserAndLogin(boolean alsoAdmin) {
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Set<GrantedAuthority> permissions = new java.util.HashSet<>(Set.of(Permission.USER));
		if (alsoAdmin) {
			permissions.add(Permission.ADMIN);
		}
		Person person = new Person(username, StringGenerator.email(), password, permissions);
		assertTrue(personService.create(person), "Unable to create user");
		return (Person
		) MockAuthContext.setLoggedInUser(personService.login(username, password).orElse(null));
	}

	@BeforeEach
	public void setup() {
		logRepository.deleteAll();
	}

	@Test
	public void emptyAuditLog() throws Exception {
		Person person = createUserAndLogin(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-logs")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("start", LocalDateTime.MIN.toString())
								.param("end", LocalDateTime.MAX.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.logs").isArray(), jsonPath("$.logs").isEmpty()
				);
	}

	@Test
	public void singleLog() throws Exception {
		Person person = createUserAndLogin(true);

		Log log = new Log(Log.LogLevel.INFO, "Test Message");
		logService.log(log);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-logs")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("start", LocalDateTime.MIN.toString())
								.param("end", LocalDateTime.MAX.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.logs").isArray(),
						jsonPath("$.logs[0]").exists(), jsonPath("$.logs[1]").doesNotExist(),

						jsonPath("$.logs[0].severity")
								.value(Matchers.equalTo(log.getSeverity().toString())),
						jsonPath("$.logs[0].timeStamp")
								.value(new LocalDateTimeJsonParser(
										log.getTimeStamp(), Duration.ofSeconds(TIME_TOLERANCE)
								)),
						jsonPath("$.logs[0].message").value(Matchers.equalTo(log.getMessage())),
						jsonPath("$.logs[0].className").isEmpty(),
						jsonPath("$.logs[0].callerId").isEmpty()
				);
	}

	@Test
	public void singleLogWithCaller() throws Exception {
		Person person = createUserAndLogin(true);

		var uuid = UUID.randomUUID();
		Log log = new Log(Log.LogLevel.INFO, "Test Message", "Test Class", uuid.toString());
		logService.log(log);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-logs")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("start", LocalDateTime.MIN.toString())
								.param("end", LocalDateTime.MAX.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.logs").isArray(),
						jsonPath("$.logs[0]").exists(), jsonPath("$.logs[1]").doesNotExist(),

						jsonPath("$.logs[0].severity")
								.value(Matchers.equalTo(log.getSeverity().toString())),
						jsonPath("$.logs[0].timeStamp")
								.value(new LocalDateTimeJsonParser(
										log.getTimeStamp(), Duration.ofSeconds(TIME_TOLERANCE)
								)),
						jsonPath("$.logs[0].message").value(Matchers.equalTo(log.getMessage())),
						jsonPath("$.logs[0].className").value(Matchers.equalTo(log.getClassName())),
						jsonPath("$.logs[0].callerId").value(Matchers.equalTo(uuid.toString()))
				);
	}
}

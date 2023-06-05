package at.ac.uibk.plant_health.controllers.person_controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static at.ac.uibk.plant_health.util.EndpointMatcherUtil.REGISTER_ENDPOINT;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.PersonRepository;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationPersonReferenceService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.EndpointMatcherUtil;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TestPersonControllerGeneral {
	@Autowired
	private PersonService personService;
	@Autowired
	private AccessPointService accessPointService;
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private SensorStationPersonReferenceService sensorStationPersonReferenceService;
	@Autowired
	private EndpointMatcherUtil endpointMatcherUtil;
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private Person createUserAndLogin(boolean alsoAdmin, boolean alsoGardener) {
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Set<GrantedAuthority> permissions = new java.util.HashSet<>(Set.of(Permission.USER));
		if (alsoGardener) {
			permissions.add(Permission.GARDENER);
		}
		if (alsoAdmin) {
			permissions.add(Permission.ADMIN);
		}
		Person person = new Person(username, StringGenerator.email(), password, permissions);
		assertTrue(personService.create(person), "Unable to create user");
		return (Person
		) MockAuthContext.setLoggedInUser(personService.login(username, password).orElse(null));
	}

	@Test
	public void testRegister() throws Exception {
		String username = "testRegisterUser";
		String password = "password";
		String email = "test@planthealth.at";

		// run request
		mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_ENDPOINT)
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.param("username", username)
								.param("password", password)
								.param("email", email)
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Person person = personRepository.findByUsername(username).orElseThrow();

		Assertions.assertAll(
				()
						-> Assertions.assertTrue(
								passwordEncoder.matches(password, person.getPassword())
						),
				() -> Assertions.assertEquals(email, person.getEmail())
		);
	}

	@Test
	public void testCreateUser() throws Exception {
		// create user and login
		Person admin = createUserAndLogin(true, false);

		String username = "testCreateUser";
		String password = "password";
		String email = "test@planthealth.at";
		Set<Permission> permissions = Set.of(Permission.USER);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/create-user")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(admin))
								.param("username", username)
								.param("password", password)
								.param("email", email)
								.param("permissions",
									   String.join(
											   ",",
											   permissions.stream()
													   .map(Permission::toString)
													   .toList()
									   )))
				.andExpectAll(status().isOk());

		Person person = personRepository.findByUsername(username).orElseThrow();

		Assertions.assertAll(
				()
						-> Assertions.assertTrue(
								passwordEncoder.matches(password, person.getPassword())
						),
				()
						-> Assertions.assertEquals(email, person.getEmail()),
				() -> Assertions.assertEquals(permissions, person.getPermissions())
		);
	}

	@Test
	public void testUpdateSettings() throws Exception {
		Person user = createUserAndLogin(false, false);

		String username = "testUpdateSettings";
		String password = "password";
		String email = "test@planthealth.at";

		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/update-settings")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(user))
								.param("username", username)
								.param("password", password)
								.param("email", email)
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Person person = personRepository.findByUsername(username).orElseThrow();

		Assertions.assertAll(
				()
						-> Assertions.assertTrue(
								passwordEncoder.matches(password, person.getPassword())
						),
				() -> Assertions.assertEquals(email, person.getEmail())
		);
	}

	@Test
	public void testUpdateUser() throws Exception {
		Person user = createUserAndLogin(false, false);
		Person admin = createUserAndLogin(true, false);

		String username = "testUpdateUser";
		String password = "password";
		String email = "test@planthealth.at";
		Set<Permission> permissions = Set.of(Permission.GARDENER);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/update-user")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(admin))
								.param("personId", String.valueOf(user.getId()))
								.param("username", username)
								.param("password", password)
								.param("email", email)
								.param("permissions",
									   String.join(
											   ",",
											   permissions.stream()
													   .map(Permission::toString)
													   .toList()
									   ))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Person person = personRepository.findByUsername(username).orElseThrow();

		Assertions.assertAll(
				()
						-> Assertions.assertTrue(
								passwordEncoder.matches(password, person.getPassword())
						),
				()
						-> Assertions.assertEquals(email, person.getEmail()),
				() -> Assertions.assertEquals(permissions, person.getPermissions())
		);
	}

	@Test
	public void deleteUser() throws Exception {
		// create user and login
		Person personToDelete = createUserAndLogin(false, false);
		Person admin = createUserAndLogin(true, false);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-user")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(admin))
								.param("personId", String.valueOf(personToDelete.getPersonId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		// check if person is deleted
		assertTrue(personService.findById(personToDelete.getId()).isEmpty());
	}

	@Test
	public void deleteUserWithPlantInDashboard() throws Exception {
		// create user and login
		Person personToDelete = createUserAndLogin(false, false);
		Person admin = createUserAndLogin(true, false);

		// create access point and sensor station
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());
		String macAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(macAddress, 4);
		sensorStation.setAccessPoint(accessPoint);
		sensorStationService.save(sensorStation);

		// add sensor station to dashboard
		sensorStationPersonReferenceService.addPlantToDashboard(personToDelete, sensorStation);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-user")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(admin))
								.param("personId", String.valueOf(personToDelete.getPersonId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		// check if person is deleted
		assertTrue(personService.findById(personToDelete.getId()).isEmpty());

		// check if sensor station and access point still exist - exception would be thrown
		// otherwise
		accessPointService.findBySelfAssignedId(selfAssignedId);
		sensorStationService.findByBdAddress(macAddress);
	}
}
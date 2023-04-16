package at.ac.uibk.plant_health.controllers.access_point_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.SetupH2Console;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith({SetupH2Console.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAccessPointController {
	@Autowired
	private AccessPointService accessPointService;
	@Autowired
	private PersonService personService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private AccessPointRepository accessPointRepository;

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

	@Test
	void testAccessPointRegister() throws Exception {
		UUID accessPointId = UUID.randomUUID();
		// register access point expect accessPoint is locked
		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.param("accessPointId", String.valueOf(accessPointId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().is(401));

		// unlock access point and try again
		accessPointService.setUnlocked(true, accessPointId);

		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.param("accessPointId", String.valueOf(accessPointId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk(), jsonPath("$.token").exists());
	}

	@Test
	void testGetAccessPoints() throws Exception {
		Person person = createUserAndLogin(true);

		for (int i = 0; i < 10; i++) {
			UUID accessPointId = UUID.randomUUID();
			accessPointService.register(accessPointId, "Office1");
		}

		List<AccessPoint> accessPointList = accessPointService.findAllAccessPoints();

		mockMvc.perform(MockMvcRequestBuilders.get("/get-access-points")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.items").isArray(),
						jsonPath("$.items").value(Matchers.hasSize(accessPointList.size())),
						jsonPath("$.items[*].selfAssignedId")
								.value(Matchers.containsInAnyOrder(
										accessPointList.stream()
												.map(d -> d.getSelfAssignedId().toString())
												.toArray(String[] ::new)
								))

				);
	}

	@Test
	void testSetLockAccessPoint() throws Exception {
		Person person = createUserAndLogin(true);
		UUID accessPointId = UUID.randomUUID();
		accessPointService.register(accessPointId, "Office1");

		// testing
		// unlock access point
		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlocked-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPointId))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		// get accessPointToken once accessPoint is unlocked
		Optional<AccessPoint> maybeAccessPoint =
				accessPointRepository.findBySelfAssignedId(accessPointId);
		AccessPoint accessPoint = null;
		if (maybeAccessPoint.isPresent()) {
			accessPoint = maybeAccessPoint.get();
		}
		System.out.println(accessPoint.getAccessToken());
		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPointId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.token").exists(),
						jsonPath("$.token").value(accessPoint.getAccessToken().toString())
				);
	}

	@Test
	void testGetAccessPointConfig() throws Exception {
		UUID accessPointId = UUID.randomUUID();
		accessPointService.register(accessPointId, "Office1");
		accessPointService.setUnlocked(true, accessPointId);

		AccessPoint accessPoint = accessPointRepository.findBySelfAssignedId(accessPointId).get();

		mockMvc.perform(MockMvcRequestBuilders.get("/get-access-point-config")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
	}
}

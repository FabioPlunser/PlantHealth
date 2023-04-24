package at.ac.uibk.plant_health.controllers.sensor_station_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Random;
import java.util.Set;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.SensorDataRepository;
import at.ac.uibk.plant_health.repositories.SensorRepository;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestSensorStationController {
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private PersonService personService;
	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper = new ObjectMapper();
	private Random rand = new Random();

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
	void setUnlockSensorStation() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 255);
		sensorStationService.save(sensorStation);
		sensorStation = sensorStationService.findByBdAddress(bdAddress).get();

		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlock-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
		sensorStation = sensorStationService.findByBdAddress(bdAddress).get();
		assertTrue(sensorStation.isUnlocked());
	}
}

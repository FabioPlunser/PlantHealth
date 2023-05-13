package at.ac.uibk.plant_health.controllers.dash_board_controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.hamcrest.core.AnyOf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationPersonReferenceService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.LocalDateTimeJsonParser;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestDashBoardController {
	@Autowired
	private PersonService personService;
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private AccessPointService accessPointService;
	@Autowired
	private SensorStationPersonReferenceService sensorStationPersonReferenceService;
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	private static final int TIME_TOLERANCE = 1;

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
	public void getPlantsOnDashboard() throws Exception {
		Person person = createUserAndLogin(false);

		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);
		SensorStation s2 = new SensorStation(StringGenerator.macAddress(), 2);

		s1.setName("SensorStation 1");
		s2.setName("SensorStation 2");
		accessPointService.foundNewSensorStation(accessPoint, List.of(s1, s2));

		s1 = sensorStationService.findByBdAddress(s1.getBdAddress());
		s2 = sensorStationService.findByBdAddress(s2.getBdAddress());

		sensorStationPersonReferenceService.addPlantToDashboard(person, s1);
		sensorStationPersonReferenceService.addPlantToDashboard(person, s2);

		AnyOf<String> nameMatcher =
				Matchers.anyOf(Matchers.equalTo(s1.getName()), Matchers.equalTo(s2.getName()));

		mockMvc.perform(MockMvcRequestBuilders.get("/get-dashboard-data")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.sensorStations").isArray(),

						jsonPath("$.sensorStations[0]").exists(),
						jsonPath("$.sensorStations[1]").exists(),
						jsonPath("$.sensorStations[2]").doesNotExist(),

						jsonPath("$.sensorStations[0].name").value(nameMatcher),
						jsonPath("$.sensorStations[1].name").value(nameMatcher)
				);
	}

	@Test
	public void getDataFromDashboard() throws Exception {
		Person person = createUserAndLogin(false);
		SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);

		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		s1.setName("SensorStation 1");
		s1.setAccessPoint(accessPoint);
		sensorStationService.save(s1);
		sensorStationPersonReferenceService.addPlantToDashboard(person, s1);

		Sensor sensor = new Sensor("Test", "°C");
		SensorData data1 = new SensorData(LocalDateTime.now(), 1, 'n', sensor, s1);
		SensorData data2 = new SensorData(data1.getTimeStamp().plusMinutes(5), 2, 'n', sensor, s1);

		sensorStationService.addSensorData(s1, data1);
		sensorStationService.addSensorData(s1, data2);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-dashboard-data")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk(), jsonPath("$.sensorStations").isArray());
	}

	@Test
	public void addPlantToDashboard() throws Exception {
		Person person = createUserAndLogin(false);

		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);
		accessPointService.foundNewSensorStation(accessPoint, List.of(s1));

		s1 = sensorStationService.findByBdAddress(s1.getBdAddress());
		s1.setName("SensorStation 1");

		mockMvc.perform(MockMvcRequestBuilders.post("/add-to-dashboard")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", s1.getDeviceId().toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		person = personService.findById(person.getPersonId()).get();

		var references = person.getSensorStationPersonReferences();

		assertEquals(1, references.size());
		assertEquals(true, references.get(0).isInDashboard());
		assertEquals(s1.getDeviceId(), references.get(0).getSensorStation().getDeviceId());
	}

	@Test
	public void removePlantFromDashboard() throws Exception {
		Person person = createUserAndLogin(false);

		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		SensorStation s1 = new SensorStation(StringGenerator.macAddress(), 1);
		accessPointService.foundNewSensorStation(accessPoint, List.of(s1));

		s1 = sensorStationService.findByBdAddress(s1.getBdAddress());
		s1.setName("SensorStation 1");
		sensorStationService.save(s1);

		sensorStationPersonReferenceService.addPlantToDashboard(person, s1);

		mockMvc.perform(MockMvcRequestBuilders.delete("/remove-from-dashboard")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", s1.getDeviceId().toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		person = personService.findById(person.getPersonId()).get();

		var references = person.getSensorStationPersonReferences();

		assertEquals(0, references.size());
	}
}

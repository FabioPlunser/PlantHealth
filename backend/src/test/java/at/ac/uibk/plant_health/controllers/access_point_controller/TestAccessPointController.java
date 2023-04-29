package at.ac.uibk.plant_health.controllers.access_point_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.hamcrest.Matchers;
import org.json.JSONObject;
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

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.Device;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import at.ac.uibk.plant_health.repositories.SensorDataRepository;
import at.ac.uibk.plant_health.repositories.SensorRepository;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;
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
	private SensorStationService sensorStationService;
	@Autowired
	private PersonService personService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private SensorRepository sensorRepository;

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
	void accessPointRegister() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		// register access point expect accessPoint is locked
		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.param("selfAssignedId", String.valueOf(selfAssignedId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().is(401));

		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		// unlock access point and try again
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());
		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.param("selfAssignedId", String.valueOf(selfAssignedId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk(), jsonPath("$.token").exists());
	}

	@Test
	void getAccessPoints() throws Exception {
		Person person = createUserAndLogin(true);

		for (int i = 0; i < 10; i++) {
			UUID accessPointId = UUID.randomUUID();
			accessPointService.register(accessPointId, "Office " + i);
		}

		List<AccessPoint> accessPointList = accessPointService.findAllAccessPoints();

		mockMvc.perform(MockMvcRequestBuilders.get("/get-access-points")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.accessPoints").isArray(),
						jsonPath("$.accessPoints").value(Matchers.hasSize(accessPointList.size())),
						jsonPath("$.accessPoints[*].accessPointId")
								.value(Matchers.containsInAnyOrder(
										accessPointList.stream()
												.map(d -> d.getDeviceId().toString())
												.toArray(String[] ::new)
								)),
						jsonPath("$.accessPoints[*].unlocked")
								.value(Matchers.containsInAnyOrder(accessPointList.stream()
																		   .map(Device::isUnlocked)
																		   .toArray(Boolean[] ::new)
								)),
						jsonPath("$.accessPoints[*].roomName")
								.value(Matchers.containsInAnyOrder(
										accessPointList.stream()
												.map(AccessPoint::getRoomName)
												.toArray(String[] ::new)
								)),
						jsonPath("$.accessPoints[*].scanActive")
								.value(Matchers.containsInAnyOrder(
										accessPointList.stream()
												.map(AccessPoint::getScanActive)
												.toArray(Boolean[] ::new)
								)),
						jsonPath("$.accessPoints[*].transferInterval")
								.value(Matchers.containsInAnyOrder(
										accessPointList.stream()
												.map(AccessPoint::getTransferInterval)
												.toArray(Integer[] ::new)
								))
				);
	}

	@Test
	void setUnlockAccessPoint() throws Exception {
		Person person = createUserAndLogin(true);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");

		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlocked-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPoint.getDeviceId()))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		//
		mockMvc.perform(MockMvcRequestBuilders.post("/register-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("selfAssignedId", String.valueOf(selfAssignedId))
								.param("roomName", "Office1")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.token").exists(),
						jsonPath("$.token").value(accessPoint.getAccessToken().toString())
				);
	}

	@Test
	void getAccessPointConfig() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		accessPoint = accessPointRepository.findByDeviceId(accessPoint.getDeviceId()).get();

		mockMvc.perform(MockMvcRequestBuilders.get("/get-access-point-config")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.roomName").value(accessPoint.getRoomName())
				);
	}

	@Test
	void foundSensorStations() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		ArrayNode sensorStations = mapper.createArrayNode();

		for (int i = 0; i < 10; i++) {
			ObjectNode sensorStation = mapper.createObjectNode();
			sensorStation.put("bdAddress", StringGenerator.macAddress());
			sensorStation.put("dipSwitchId", 255 - i);
			sensorStations.addPOJO(sensorStation);
		}

		mockMvc.perform(MockMvcRequestBuilders.put("/found-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.content(sensorStations.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
	}

	@Test
	void scanForSensorStations() throws Exception {
		Person person = createUserAndLogin(true);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		mockMvc.perform(MockMvcRequestBuilders.post("/scan-for-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPoint.getDeviceId()))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		accessPoint = accessPointRepository.findByDeviceId(accessPoint.getDeviceId()).get();
		assertTrue(accessPoint.getScanActive());
	}

	@Test
	void setTransferInterval() throws Exception {
		Person person = createUserAndLogin(true);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		int transferInterval = 10;

		mockMvc.perform(MockMvcRequestBuilders.post("/set-access-point-transfer-interval")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPoint.getDeviceId()))
								.param("transferInterval", String.valueOf(transferInterval))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		accessPoint = accessPointRepository.findByDeviceId(accessPoint.getDeviceId()).get();
		assertEquals(transferInterval, accessPoint.getTransferInterval());
	}

	@Test
	void transferData() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		int sensorStationsCount = 2;
		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		ArrayNode sensorStations = mapper.createArrayNode();

		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "°C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		for (int i = 0; i < sensorStationsCount; i++) {
			String BdAddress = StringGenerator.macAddress();
			SensorStation sS = new SensorStation(BdAddress, 255 - i);
			sensorStationService.save(sS);

			ObjectNode sensorStation = mapper.createObjectNode();
			sensorStation.put("bdAddress", BdAddress);
			sensorStation.put("dipSwitchId", 255 - i);
			sensorStation.put("connectionAlive", rand.nextBoolean());

			ArrayNode sensorData = mapper.createArrayNode();
			for (int j = 0; j < sensorMap.size(); j++) {
				ObjectNode data = mapper.createObjectNode();
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				data.put("timeStamp", now.format(formatter));
				data.put("value", rand.nextDouble());
				data.put("alarm", "h");

				ObjectNode sensor = mapper.createObjectNode();
				sensor.put("type", sensorMap.keySet().toArray()[j].toString());
				sensor.put("unit", sensorMap.values().toArray()[j].toString());
				data.set("sensor", sensor);
				sensorData.addPOJO(data);
			}
			sensorStation.set("sensorData", sensorData);
			sensorStations.add(sensorStation);
		}

		mockMvc.perform(MockMvcRequestBuilders.post("/transfer-data")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.content(sensorStations.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		assertEquals(sensorMap.size() * sensorStationsCount, sensorDataRepository.findAll().size());
		assertEquals(sensorMap.size(), sensorRepository.findAll().size());
	}
}

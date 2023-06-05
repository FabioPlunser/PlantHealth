package at.ac.uibk.plant_health.controllers.access_point_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import at.ac.uibk.plant_health.models.plant.SensorData;
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

		List<AccessPoint> accessPointList = accessPointService.findAllAccessPoints()
													.stream()
													.filter(ap -> !ap.isDeleted())
													.toList();

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
	void setUnlockedAccessPoint() throws Exception {
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

		mockMvc.perform(MockMvcRequestBuilders.post("/found-sensor-stations")
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
		// precondition AccessPoint is registered and connected
		Person person = createUserAndLogin(true);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());
		accessPoint = accessPointService.findById(accessPoint.getDeviceId());
		accessPointService.setLastConnection(accessPoint);

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
	void updateAccessPoint() throws Exception {
		Person person = createUserAndLogin(true);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		mockMvc.perform(MockMvcRequestBuilders.post("/update-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPoint.getDeviceId()))
								.param("roomName", "Office2")
								.param("transferInterval", "10")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		assertEquals(10, accessPoint.getTransferInterval());
		assertEquals("Office2", accessPoint.getRoomName());
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

		List<String> bdAddresses = new ArrayList<>();
		for (int i = 0; i < sensorStationsCount; i++) {
			bdAddresses.add(StringGenerator.macAddress());
			SensorStation sS = new SensorStation(bdAddresses.get(i), 255 - i);
			sS.setUnlocked(true);
			sensorStationService.save(sS);

			ObjectNode sensorStation = mapper.createObjectNode();
			sensorStation.put("bdAddress", bdAddresses.get(i));
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

		List<SensorData> availableData =
				sensorDataRepository.findAll()
						.stream()
						.filter(d -> bdAddresses.contains(d.getSensorStation().getBdAddress()))
						.toList();
		assertEquals(sensorMap.size() * sensorStationsCount, availableData.size());
	}

	@Test
	void deleteAccessPoint() throws Exception {
		Person person = createUserAndLogin(true);
		// access point registers
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		// one sensor station assigned
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStation.setAccessPoint(accessPoint);
		sensorStationService.save(sensorStation);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(accessPoint.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		// check if access point is removed
		assertTrue(accessPointService.findById(accessPoint.getDeviceId()).isDeleted());
		assertNull(accessPointService.findById(accessPoint.getDeviceId()).getSelfAssignedId());

		// check if assigned sensor station is also deleted
		assertTrue(sensorStationService.findById(sensorStation.getDeviceId()).isDeleted());
	}

	@Test
	public void testSetUnlockedAccessPointWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true);
		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlocked-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(UUID.randomUUID()))
								.param("unlocked", String.valueOf(false))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	public void testScanForSensorStationsWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true);
		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/scan-for-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(UUID.randomUUID()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	public void testSetAccessPointTransferIntervalWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true);
		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/set-access-point-transfer-interval")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(UUID.randomUUID()))
								.param("transferInterval", String.valueOf(10))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	public void testUpdateAccessPointWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true);
		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/update-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(UUID.randomUUID()))
								.param("roomName", "")
								.param("transferInterval", String.valueOf(10))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	public void testTransferDataWithLockedSensorStation() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		ArrayNode sensorStations = mapper.createArrayNode();

		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "°C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		SensorStation sS = new SensorStation(StringGenerator.macAddress(), 255);
		sS.setUnlocked(false);
		sensorStationService.save(sS);

		ObjectNode sensorStation = mapper.createObjectNode();
		sensorStation.put("bdAddress", sS.getBdAddress());
		sensorStation.put("dipSwitchId", 255);
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

		mockMvc.perform(MockMvcRequestBuilders.post("/transfer-data")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.content(sensorStations.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isConflict());
	}

	@Test
	public void testTransferDataWithoutSensorStation() throws Exception {
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");
		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPointService.setUnlocked(true, accessPoint.getDeviceId());

		accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		ArrayNode sensorStations = mapper.createArrayNode();
		ObjectNode sensorStation = mapper.createObjectNode();
		sensorStation.set("sensorData", null);
		sensorStations.add(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.post("/transfer-data")
								.header(HttpHeaders.USER_AGENT, "AccessPoint")
								.header(HttpHeaders.AUTHORIZATION,
										"{ \"token\":\"" + accessPoint.getAccessToken().toString()
												+ "\"}")
								.content(sensorStations.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	public void testDeleteAccessPointWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true);
		// run request
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-access-point")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("accessPointId", String.valueOf(UUID.randomUUID()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}
}

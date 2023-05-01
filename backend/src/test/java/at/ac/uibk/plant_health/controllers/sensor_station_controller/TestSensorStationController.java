package at.ac.uibk.plant_health.controllers.sensor_station_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;
import jakarta.persistence.Access;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TestSensorStationController {
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private AccessPointService accessPointService;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private PersonService personService;
	@Autowired
	private PlantPictureRepository plantPictureRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper = new ObjectMapper();
	private Random rand = new Random();

	private final String picture =
			"iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAIAAADTED8xAAADMElEQVR4nOzVwQnAIBQFQYXff81RUkQCOyDj1YOPnbXWPmeTRef+/3O/OyBjzh3CD95BfqICMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMO0TAAD//2Anhf4QtqobAAAAAElFTkSuQmCC";
	PlantPicture plantPicture;
	@Value("${swa.pictures.path}")
	private String picturesPath;

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
	void getSensorStations() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported multiple sensorStations
		int sensorStationCount = 5;
		for (int i = 0; i < sensorStationCount; i++) {
			String bdAddress = StringGenerator.macAddress();
			SensorStation sensorStation = new SensorStation(bdAddress, 255 - i);
			sensorStationService.save(sensorStation);
		}
		List<SensorStation> sensorStations =
				sensorStationService.findAll().stream().filter(s -> !s.isDeleted()).toList();
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.sensorStations").exists(),
						jsonPath("$.sensorStations.length()").value(sensorStations.size())
				);
	}

	@Test
	void getSensorStationInfo() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 1);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Rose");
		sensorStationService.save(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-info")
								.contentType(MediaType.APPLICATION_JSON)
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.name").exists(),
						jsonPath("$.roomName").exists(),
						jsonPath("$.name").value(sensorStation.getName()),
						jsonPath("$.roomName").value(accessPoint.getRoomName())
				);
	}

	@Test
	void setUnlockSensorStation() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 1);
		sensorStationService.save(sensorStation);
		sensorStation = sensorStationService.findByBdAddress(bdAddress);

		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlock-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertTrue(sensorStation.isUnlocked());
	}

	@Test
	void setSensorLimitsAdmin() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(sensorStation);

		// precondition sensorStation has at least one sensor
		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "°C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		ArrayNode limits = mapper.createArrayNode();

		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			sensorRepository.save(sensor);
			ObjectNode sensorJson = mapper.createObjectNode();
			sensorJson.put("type", sensorMap.keySet().toArray()[i].toString());
			sensorJson.put("unit", sensorMap.values().toArray()[i].toString());

			ObjectNode limit = mapper.createObjectNode();
			limit.putPOJO("sensor", sensorJson);
			limit.put("upperLimit", rand.nextFloat());
			limit.put("lowerLimit", rand.nextFloat());
			limit.put("thresholdDuration", rand.nextInt(1000));
			limits.add(limit);
		}

		mockMvc.perform(MockMvcRequestBuilders.post("/set-sensor-limits")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.content(limits.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		List<SensorLimits> sensorLimits = sensorLimitsRepository.findAll();
		assertEquals(sensorMap.size(), sensorLimits.size());
		assertEquals(sensorLimits.size(), sensorStation.getSensorLimits().size());
		assertEquals(sensorLimits, sensorStation.getSensorLimits());
	}

	private void createPicture(SensorStation sensorStation) {
		List<PlantPicture> plantPictures = new ArrayList<>();
		try {
			byte[] imageByte = Base64.decodeBase64(picture);
			String picturePath = picturesPath + UUID.randomUUID() + ".png";
			Path path = Paths.get(picturePath);
			plantPicture = new PlantPicture(sensorStation, picturePath, LocalDateTime.now());
			plantPictureRepository.save(plantPicture);
			Files.createDirectories(path.getParent());
			Files.write(path, imageByte);
			plantPictures.add(plantPicture);
			sensorStation.setPlantPictures(plantPictures);
			sensorStationService.save(sensorStation);

		} catch (Exception e) {
			throw new RuntimeException("Error");
		}
	}

	private void deleteAllPictures(SensorStation sensorStation) throws Exception {
		List<PlantPicture> pictures = sensorStation.getPlantPictures();
		try {
			for (PlantPicture picture1 : pictures) {
				Path path = Paths.get(picture1.getPicturePath());
				Files.delete(path);
			}
			sensorStation.setPlantPictures(new ArrayList<>());
			sensorStationService.save(sensorStation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void uploadPicture() throws Exception {
		// precondition accessPoint exists
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 2);
		sensorStationService.save(sensorStation);

		MockMultipartFile file = new MockMultipartFile(
				"picture", "test.png", MediaType.IMAGE_PNG_VALUE, picture.getBytes()
		);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/upload-sensor-station-picture")
								.file(file)
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpectAll(status().isOk());

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(1, sensorStation.getPlantPictures().size());

		deleteAllPictures(sensorStation);
		assertEquals(0, sensorStation.getPlantPictures().size());
	}

	@Test
	void getPictures() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 3);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Plant1");
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-pictures")
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpectAll(
						status().isOk(), jsonPath("$.pictures").exists(),
						jsonPath("$.pictures").isArray(),
						jsonPath("$.roomName").value(accessPoint.getRoomName()),
						jsonPath("$.plantName").value(sensorStation.getName()),
						jsonPath("$.pictures[0].timeStamp").exists(),
						jsonPath("$.pictures[0].pictureId").exists()
				);

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(1, sensorStation.getPlantPictures().size());

		sensorStation.getPlantPictures().forEach(p -> System.out.println(p.getPicturePath()));

		deleteAllPictures(sensorStation);
		assertEquals(0, sensorStation.getPlantPictures().size());
	}

	@Test
	void getSensorStationPicture() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 3);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Plant1");
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-picture")
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("pictureId", String.valueOf(plantPicture.getPictureId()))
								.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM))
				.andExpectAll(status().isOk());

		deleteAllPictures(sensorStation);
		assertEquals(0, sensorStation.getPlantPictures().size());
	}

	@Test
	void deletePictureAdmin() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		List<PlantPicture> pictures = plantPictureRepository.findAll();
		PlantPicture picture1 = pictures.get(0);

		assertEquals(pictures.size(), sensorStation.getPlantPictures().size());

		mockMvc.perform(MockMvcRequestBuilders.post("/delete-sensor-station-picture")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("pictureId", String.valueOf(picture1.getPictureId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		assertEquals(0, sensorStation.getPlantPictures().size());
	}

	@Test
	void deleteAllPicturesAdmin() throws Exception {
		Person person = createUserAndLogin(true);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(sensorStation);

		int pictureCount = 10;

		List<PlantPicture> plantPictures = new ArrayList<>();
		for (int i = 0; i < pictureCount; i++) {
			try {
				byte[] imageByte = Base64.decodeBase64(picture);
				String picturePath = picturesPath + UUID.randomUUID() + ".png";
				Path path = Paths.get(picturePath);
				plantPicture = new PlantPicture(sensorStation, picturePath, LocalDateTime.now());
				plantPictureRepository.save(plantPicture);
				Files.createDirectories(path.getParent());
				Files.write(path, imageByte);
				plantPictures.add(plantPicture);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		sensorStation.setPlantPictures(plantPictures);
		sensorStationService.save(sensorStation);

		List<PlantPicture> pictures = plantPictureRepository.findAll();
		assertEquals(pictures.size(), sensorStation.getPlantPictures().size());

		mockMvc.perform(MockMvcRequestBuilders.post("/delete-all-sensor-station-pictures")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		assertEquals(0, sensorStation.getPlantPictures().size());
	}
}

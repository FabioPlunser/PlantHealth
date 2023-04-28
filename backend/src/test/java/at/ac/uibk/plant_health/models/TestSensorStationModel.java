package at.ac.uibk.plant_health.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestSensorStationModel {
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private PersonService personService;

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
	void saveSensorStation() {
		SensorStation sensorStation = new SensorStation("48-42", 255);
		sensorStationRepository.save(sensorStation);
		assertNotNull(sensorStationRepository.findAll());
	}

	@Test
	void setSensorLimits() {
		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "°C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		Person person = createUserAndLogin(true);

		SensorStation sensorStation = new SensorStation("48-42", 255);
		sensorStationRepository.save(sensorStation);

		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			sensorRepository.save(sensor);
		}

		List<SensorLimits> sensorLimitsList = new ArrayList<>();
		for (int i = 0; i < sensorMap.size(); i++) {
			SensorLimits sensorLimits = new SensorLimits(
					LocalDateTime.now(), rand.nextFloat(), rand.nextFloat(), rand.nextInt(),
					sensorRepository.findAll().get(i), person, sensorStation
			);
			sensorLimitsRepository.save(sensorLimits);
			sensorLimitsList.add(sensorLimits);
			sensorStationRepository.save(sensorStation);
		}

		//		sensorStation.setSensorLimits(sensorLimitsList);

		assertNotNull(sensorStationRepository.findAll());
		assertNotNull(sensorRepository.findAll());
		assertNotNull(sensorLimitsRepository.findAll());

		sensorStation = sensorStationRepository.findByBdAddress(sensorStation.getBdAddress()).get();

		assertEquals(sensorMap.size(), sensorRepository.findAll().size());
		assertEquals(sensorMap.size(), sensorRepository.findAll().size());
		assertEquals(
				sensorStation, sensorStationRepository.findById(sensorStation.getDeviceId()).get()
		);
		System.out.println(sensorStation.getSensorLimits());
		assertEquals(sensorMap.size(), sensorStation.getSensorLimits().size());
	}
}

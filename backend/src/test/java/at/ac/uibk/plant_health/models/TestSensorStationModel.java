package at.ac.uibk.plant_health.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestSensorStationModel {
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private AccessPointRepository accessPointRepository;

	@Test
	void saveSensorStation() {
		SensorStation sensorStation = new SensorStation("48-42", 255);
		sensorStationRepository.save(sensorStation);
		assertNotNull(sensorStationRepository.findAll());
	}
}

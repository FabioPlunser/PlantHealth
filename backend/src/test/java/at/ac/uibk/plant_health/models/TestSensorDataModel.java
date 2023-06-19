package at.ac.uibk.plant_health.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.repositories.SensorDataRepository;
import at.ac.uibk.plant_health.repositories.SensorRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;
import at.ac.uibk.plant_health.util.SetupH2Console;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestSensorDataModel {
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;

	@Test
	void testCreateAndSaveSensorData() {
		Random rand = new SecureRandom();

		SensorStation station = new SensorStation(StringGenerator.macAddress(), 1);
		sensorStationRepository.save(station);

		List<SensorData> sensorDataList = new ArrayList<>();

		Sensor sensor;
		Optional<Sensor> maybeSensor;
		if ((maybeSensor = sensorRepository.findByType("TEMPERATURE")).isPresent()) {
			sensor = maybeSensor.get();
		} else {
			sensor = new Sensor("TEMPERATURE", "C");
			sensorRepository.save(sensor);
		}

		SensorData sensorD = new SensorData(
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), rand.nextFloat(), "n", sensor,
				station
		);
		sensorDataRepository.save(sensorD);

		sensorDataList.add(sensorD);

		station.setSensorData(sensorDataList);
		sensorStationRepository.save(station);

		assertEquals(
				sensorDataList,
				sensorStationRepository.findById(station.getDeviceId()).get().getSensorData()
		);
	}
}

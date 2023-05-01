package at.ac.uibk.plant_health.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import at.ac.uibk.plant_health.repositories.SensorStationRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestAccessPointModel {
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;

	// AccessPoint lifecycle:
	// AccessPoint registers itself to the backend
	// AccessPoint gets configuration
	// AccessPoint found SensorStations
	// AccessPoint sends sensorstation data to backend
	@BeforeEach
	void setup() {
		sensorStationRepository.deleteAll();
		accessPointRepository.deleteAll();
	}

	/*
	@Test
	void testRegisterAccessPoint() {
		// given roomName and id
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "TestRoom", 10, false);
		accessPointRepository.save(accessPoint);

		assertEquals(
				accessPoint.getDeviceId(),
				accessPointRepository.findById(accessPoint.getDeviceId()).get().getDeviceId()
		);
		assertFalse(accessPointRepository.findAll().isEmpty());
	}

	@Test
	void changeAccessPointConfiguration() {
		// given AccessPoint is registered
		UUID selfAssignedId = UUID.randomUUID();
		AccessPoint accessPoint = new AccessPoint(selfAssignedId, "TestRoom", 10, false);
		accessPointRepository.save(accessPoint);

		assertEquals(selfAssignedId, accessPoint.getSelfAssignedId());

		AccessPoint savedAccessPoint =
				accessPointRepository.findById(accessPoint.getDeviceId()).get();
		assertEquals(accessPoint.getDeviceId(), savedAccessPoint.getDeviceId());
		assertEquals(accessPoint.getSelfAssignedId(), savedAccessPoint.getSelfAssignedId());

		// unlock access point search for sensor stations
		savedAccessPoint.setUnlocked(true);
		savedAccessPoint.setScanActive(true);
		accessPointRepository.save(savedAccessPoint);

		assertTrue(accessPointRepository.findById(accessPoint.getDeviceId()).get().isUnlocked());
		assertTrue(accessPointRepository.findById(accessPoint.getDeviceId()).get().isScanActive());
	}

	@Test
	@Transactional
	void addFoundSensorStations() {
		// given registered AccessPoint, found SensorStations
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "TestRoom", 10, false);
		accessPointRepository.save(accessPoint);

		// given list of found sensorStations
		List<SensorStation> sensorStations = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String BD_ADDR = String.format("11:22:33:44:55:6%d", i);
			sensorStations.add(new SensorStation(BD_ADDR, 255 - i));
		}

		// set accessPoint per SensorStation
		for (SensorStation ss : sensorStations) {
			ss.setAccessPoint(accessPoint);
			sensorStationRepository.save(ss);
		}

		// set sensorStations of accessPoint
		AccessPoint foundAccessPoint =
				accessPointRepository.findById(accessPoint.getDeviceId()).get();
		foundAccessPoint.setSensorStations(sensorStations);
		accessPointRepository.save(foundAccessPoint);

		assertEquals(
				sensorStations,
				accessPointRepository.findById(accessPoint.getDeviceId()).get().getSensorStations()
		);
	}

	@Test
	@Transactional
	void removeSensorStationsFromAccessPoint() {
		// given registered AccessPoint, found SensorStations
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "TestRoom", 10, false);
		accessPointRepository.save(accessPoint);

		// given list of found sensorStations
		List<SensorStation> sensorStations = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String BD_ADDR = String.format("11:22:33:44:55:6%d", i);
			sensorStations.add(new SensorStation(BD_ADDR, 255 - i));
		}

		// set accessPoint per SensorStation
		for (SensorStation ss : sensorStations) {
			ss.setAccessPoint(accessPoint);
			sensorStationRepository.save(ss);
		}

		// set sensorStations of accessPoint
		AccessPoint foundAccessPoint =
				accessPointRepository.findById(accessPoint.getDeviceId()).get();
		foundAccessPoint.setSensorStations(sensorStations);
		accessPointRepository.save(foundAccessPoint);

		// remove sensor station from access point
		AccessPoint foundAccessPoint2 =
				accessPointRepository.findById(accessPoint.getDeviceId()).get();
		foundAccessPoint2.getSensorStations().remove(2);
		foundAccessPoint2.getSensorStations().get(2).setAccessPoint(null);
		accessPointRepository.save(foundAccessPoint2);

		assertEquals(
				4,
				accessPointRepository.findById(accessPoint.getDeviceId())
						.get()
						.getSensorStations()
						.size()
		);
		assertEquals(5, sensorStationRepository.findAll().size());
	}
	*/
}

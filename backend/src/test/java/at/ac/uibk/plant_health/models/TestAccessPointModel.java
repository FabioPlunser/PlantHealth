package at.ac.uibk.plant_health.models;

import static org.junit.jupiter.api.Assertions.*;

import at.ac.uibk.plant_health.repositories.SensorDataRepository;
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
	@Autowired
	private SensorDataRepository sensorDataRepository;

	// AccessPoint lifecycle:
	// AccessPoint registers itself to the backend
	// AccessPoint gets configuration
	// AccessPoint found SensorStations
	// AccessPoint sends sensorstation data to backend
	@BeforeEach
	@Transactional
	void setup() {
		// Delete all Data from the DB keeping in mind foreign key Constraints.
		sensorDataRepository.deleteAll();
		for (SensorStation s : sensorStationRepository.findAll()) {
			// SensorStation is owning Side
			// Ensure that owning side is empty, so
			s.setAccessPoint(null);
			sensorStationRepository.save(s);
		}
		sensorStationRepository.deleteAll();
		accessPointRepository.deleteAll();
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
		assertTrue(accessPointRepository.findById(accessPoint.getDeviceId()).get().getScanActive());
	}

	@Test
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
	void removeSensorStationsFromAccessPoint() {
		// given registered AccessPoint, found SensorStations
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "TestRoom", 10, false);
		accessPointRepository.save(accessPoint);

		// given list of found sensorStations
		List<SensorStation> sensorStations = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String BD_ADDR = String.format("11:22:33:44:5%d:66", i);
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
		sensorStationRepository.save(foundAccessPoint2.getSensorStations().get(2));

		assertEquals(
				4,
				accessPointRepository.findById(accessPoint.getDeviceId())
						.get()
						.getSensorStations()
						.size()
		);
	}
}

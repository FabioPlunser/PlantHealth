package at.ac.uibk.plant_health.repositories;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import org.springframework.transaction.annotation.Transactional;

public interface SensorStationPersonReferenceRepository
		extends CrudRepository<SensorStationPersonReference, UUID> {
	@Override
	List<SensorStationPersonReference> findAll();

	@Transactional
	@Modifying
	@Query("delete from SensorStationPersonReference s where s.person = ?1 and s.sensorStation = ?2")
	int deleteByPersonAndSensorStation(Person person, SensorStation sensorStation);
}

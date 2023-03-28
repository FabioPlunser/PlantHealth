package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;

public interface SensorStationRepository extends CrudRepository<SensorStation, UUID> {
	@Override
	List<SensorStation> findAll();
}

package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.user.Person;

public interface SensorRepository extends CrudRepository<Sensor, UUID> {
	@Override
	List<Sensor> findAll();

	Optional<Sensor> findByType(String type);
}

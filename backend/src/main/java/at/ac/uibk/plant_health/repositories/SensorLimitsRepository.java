package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Person;

public interface SensorLimitsRepository extends CrudRepository<SensorLimits, UUID> {
	@Override
	List<SensorLimits> findAll();
}

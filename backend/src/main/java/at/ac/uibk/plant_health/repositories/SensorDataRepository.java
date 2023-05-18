package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.plant.SensorData;

public interface SensorDataRepository extends CrudRepository<SensorData, UUID> {
	@Override
	List<SensorData> findAll();
}

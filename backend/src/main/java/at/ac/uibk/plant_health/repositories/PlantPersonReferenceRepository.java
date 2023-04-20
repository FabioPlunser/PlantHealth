package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;

public interface PlantPersonReferenceRepository
		extends CrudRepository<SensorStationPersonReference, UUID> {
	@Override
	List<SensorStationPersonReference> findAll();
}

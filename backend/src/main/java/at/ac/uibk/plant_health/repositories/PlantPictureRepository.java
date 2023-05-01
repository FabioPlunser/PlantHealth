package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.*;

import at.ac.uibk.plant_health.models.plant.PlantPicture;

public interface PlantPictureRepository extends CrudRepository<PlantPicture, UUID> {
	@Override
	List<PlantPicture> findAll();
}

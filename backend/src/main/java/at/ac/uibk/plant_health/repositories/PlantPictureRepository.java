package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.PlantPictureId;

public interface PlantPictureRepository extends CrudRepository<PlantPicture, PlantPictureId> {
	@Override
	List<PlantPicture> findAll();
}

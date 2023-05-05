package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;

public interface PlantPictureRepository extends CrudRepository<SensorStationPicture, UUID> {
	@Override
	List<SensorStationPicture> findAll();

	SensorStationPicture findDistinctFirstBySensorStationOrderByTimeStampDesc(
			SensorStation sensorStation
	);
}

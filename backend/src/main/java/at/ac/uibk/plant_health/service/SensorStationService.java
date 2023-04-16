package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.repositories.*;

@Service
public class SensorStationService {
	@Autowired
	private SensorStationRepository sensorStationRepository;

	@Autowired
	private PlantPictureRepository plantPictureRepository;

	@Autowired
	private PlantPersonReferenceRepository plantPersonReferenceRepository;

	public List<SensorStation> findAll() {
		return sensorStationRepository.findAll();
	}

	public SensorStation save(SensorStation sensorStation) {
		try {
			return sensorStationRepository.save(sensorStation);
		} catch (Exception e) {
			return null;
		}
	}

	public List<String> getPlantPictures(SensorStation plant) {
		// TODO
		return List.of();
	}

	public boolean uploadPlantPicture(UUID plantId, String picture) {
		// TODO
		return false;
	}

	public List<SensorStation> findAllPlants() {
		// TODO
		return List.of();
	}

	public boolean setSensorLimits(List<SensorLimits> sensorLimits) {
		// TODO
		return false;
	}

	public boolean setTransferInterval(int transferInterval) {
		// TODO
		return false;
	}

	public boolean deletePicture(PlantPicture plantPicture) {
		// TODO
		return false;
	}

	public boolean createQrCode(UUID qrCodeId) {
		// TODO
		return false;
	}
}

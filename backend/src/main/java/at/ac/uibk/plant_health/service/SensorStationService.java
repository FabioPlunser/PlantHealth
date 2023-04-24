package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.repositories.*;

@Service
public class SensorStationService {
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private PlantPictureRepository plantPictureRepository;
	@Autowired
	private SensorStationPersonReferenceRepository sensorStationPersonReferenceRepository;

	public Optional<SensorStation> findById(UUID id) {
		return this.sensorStationRepository.findById(id);
	}

	public List<SensorStation> findAll() {
		return sensorStationRepository.findAll();
	}

	public List<SensorStation> findLocked() {
		return sensorStationRepository.findByIsUnlockedAndIsDeleted(false, false);
	}

	public Optional<SensorStation> findByQrCode(UUID qrCode) {
		return sensorStationRepository.findByQrCodeId(qrCode);
	}

	public Optional<SensorStation> findByBdAddress(String bdAddress) {
		return sensorStationRepository.findByBdAddress(bdAddress);
	}

	public SensorStation save(SensorStation sensorStation) {
		try {
			return sensorStationRepository.save(sensorStation);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean sensorStationExists(UUID sensorStationId) {
		try {
			Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	/**
	 * Set unlock status of sensor station.
	 * @param unlocked
	 * @param sensorStationId
	 * @return
	 */
	public boolean setUnlocked(boolean unlocked, UUID sensorStationId) {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty()) return false;

		SensorStation sensorStation = maybeSensorStation.get();
		sensorStation.setUnlocked(unlocked);
		return save(sensorStation) != null;
	}

	public List<String> getPlantPictures(SensorStation plant) {
		// TODO
		return List.of();
	}

	public boolean uploadPlantPicture(UUID plantId, String picture) {
		return false;
	}

	public List<SensorStation> findAllPlants() {
		return this.sensorStationRepository.findByIsUnlockedAndIsDeleted(true, false);
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

	@Transactional
	public boolean addSensorData(SensorStation sensorStation, SensorData data) {
		if (data == null || sensorStation == null) return false;

		Sensor sensor = data.getSensor();
		Optional<Sensor> maybeSensor = sensorRepository.findByType(sensor.getType());
		if (maybeSensor.isPresent()) {
			sensor = maybeSensor.get();
		} else {
			System.out.println(sensor);
			sensor = sensorRepository.save(sensor);
		}

		data.setSensor(sensor);
		data.setSensorStation(sensorStation);
		this.sensorDataRepository.save(data);

		return true;
	}

	@Transactional
	public boolean addSensorData(SensorStation sensorStation, List<SensorData> dataList) {
		if (dataList == null || sensorStation == null) return false;

		for (SensorData data : dataList) {
			if (!this.addSensorData(sensorStation, data)) {
				return false;
			};
		}

		return true;
	}
}

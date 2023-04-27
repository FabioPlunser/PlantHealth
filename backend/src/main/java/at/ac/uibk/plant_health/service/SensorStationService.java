package at.ac.uibk.plant_health.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Person;
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
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;

	public Optional<SensorStation> findById(UUID id) {
		return this.sensorStationRepository.findById(id);
	}

	public List<SensorStation> findAll() {
		return sensorStationRepository.findAll();
	}

	public List<SensorStation> findLocked() {
		return sensorStationRepository.findByIsUnlockedAndIsDeleted(false, false);
	}

	public Optional<SensorStation> findByBdAddress(String bdAddress) {
		return sensorStationRepository.findByBdAddress(bdAddress);
	}

	@Value("${swa.pictures.path}")
	private String picturesPath;

	public SensorStation save(SensorStation sensorStation) {
		try {
			return sensorStationRepository.save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Could not save SensorStation", 500);
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
	@Transactional
	public boolean setUnlocked(boolean unlocked, UUID sensorStationId) {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty()) return false;

		SensorStation sensorStation = maybeSensorStation.get();
		sensorStation.setUnlocked(unlocked);
		return save(sensorStation) != null;
	}

	/**
	 * Get pictures of SensorStation
	 * @param sensorStationId
	 * @return list of base64 encoded pictures
	 */
	@Transactional
	public List<String> getPictures(UUID sensorStationId) {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty())
			throw new IllegalArgumentException("SensorStation could not be found");

		SensorStation sensorStation = maybeSensorStation.get();
		List<PlantPicture> pictures = sensorStation.getPlantPictures();
		List<String> picturesBase64 = new ArrayList<>();

		try {
			for (PlantPicture picture : pictures) {
				Path path = Paths.get(picturesPath + picture.getPictureName());
				byte[] pictureBytes = Files.readAllBytes(path);
				String base64 = Base64.encodeBase64String(pictureBytes);
				picturesBase64.add(base64);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return picturesBase64;
	}

	/**
	 * uploadPicture
	 * @param picture base64 encoded picture
	 * @return true if upload was successful
	 */
	@Transactional
	public boolean uploadPicture(String picture, UUID sensorStationId) {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty())
			throw new IllegalArgumentException("SensorStation could not be found");
		SensorStation sensorStation = maybeSensorStation.get();
		PlantPicture plantPicture = null;
		try {
			byte[] imageByte = Base64.decodeBase64(picture);
			String pictureName = UUID.randomUUID() + ".png";
			Path path = Paths.get(picturesPath + pictureName);
			plantPicture = new PlantPicture(sensorStation, pictureName, LocalDateTime.now());
			Files.createDirectories(path.getParent());
			Files.write(path, imageByte);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		plantPictureRepository.save(plantPicture);
		List<PlantPicture> sensorStationPictures = sensorStation.getPlantPictures();
		sensorStationPictures.add(plantPicture);
		sensorStation.setPlantPictures(sensorStationPictures);
		return save(sensorStation) != null;
	}

	/**
	 * Set sensor limits of sensor station.
	 * @param sensorLimits
	 * @param sensorStationId
	 * @return
	 */
	public void setSensorLimits(
			List<SensorLimits> sensorLimits, UUID sensorStationId, Person person
	) throws ServiceException {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty())
			throw new ServiceException("SensorStation not found", 500);
		SensorStation sensorStation = maybeSensorStation.get();
		List<Sensor> possibleSensors = sensorRepository.findAll();
		for (SensorLimits limits : sensorLimits) {
			System.out.println(limits.getSensor());
			if (!possibleSensors.contains(limits.getSensor()))
				throw new ServiceException("Sensor not found", 500);
			limits.setGardener(person);
			limits.setSensorStation(sensorStation);
			limits.setTimeStamp(LocalDateTime.now());
		}
		sensorLimitsRepository.saveAll(sensorLimits);
		sensorStation.setSensorLimits(sensorLimits);
		save(sensorStation);
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

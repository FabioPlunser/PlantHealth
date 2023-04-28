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
	// @Transactional
	public void setSensorLimits(
			List<SensorLimits> sensorLimits, UUID sensorStationId, Person person
	) throws ServiceException {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty())
			throw new ServiceException("SensorStation not found", 500);
		SensorStation sensorStation = maybeSensorStation.get();
		for (SensorLimits limit : sensorLimits) {
			Optional<Sensor> sensor = sensorRepository.findByType(limit.getSensor().getType());
			if (sensor.isEmpty())
				throw new ServiceException(
						"Sensor " + limit.getSensor().getType() + " not found", 500
				);
			limit.setSensor(sensor.get());
			limit.setGardener(person);
			limit.setSensorStation(sensorStation);
			limit.setTimeStamp(LocalDateTime.now());
			sensorLimitsRepository.save(limit);
			//			sensorStation.addSensorLimit(limit);
			//			save(sensorStation);
		}

		Optional<SensorStation> maybeSensorStation2 = findById(sensorStationId);
		if (maybeSensorStation2.isEmpty())
			throw new ServiceException("SensorStation not found", 500);
		SensorStation sensorStation2 = maybeSensorStation2.get();
		System.out.println(
				"\u001B[33m"
				+ "SensorLimitService" + sensorStation.getSensorLimits() + "\u001B[0m"
		);
	}

	/**
	 * Delete specific picture of sensor station
	 * @param pictureId
	 * @return
	 * @throws ServiceException
	 */
	public void deletePicture(UUID pictureId) throws ServiceException {
		Optional<PlantPicture> maybePicture = plantPictureRepository.findById(pictureId);
		if (maybePicture.isEmpty()) throw new ServiceException("Picture does not exist", 404);
		PlantPicture picture = maybePicture.get();

		SensorStation sensorStation = picture.getSensorStation();
		try {
			Path path = Paths.get(picturesPath + picture.getPictureName());
			Files.delete(path);
			plantPictureRepository.delete(picture);
			sensorStation.getPlantPictures().remove(picture);
			save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Failed to delete pictue of the server", 500);
		}
	}

	/**
	 * Delete all pictures of a sensor station
	 * @param sensorStationId
	 * @throws ServiceException
	 */
	public void deleteAllPictures(UUID sensorStationId) throws ServiceException {
		Optional<SensorStation> maybeSensorStation = findById(sensorStationId);
		if (maybeSensorStation.isEmpty())
			throw new ServiceException("Couldn't find sensor station", 404);

		SensorStation sensorStation = maybeSensorStation.get();
		List<PlantPicture> pictures = new ArrayList<>(sensorStation.getPlantPictures());
		try {
			for (PlantPicture picture : pictures) {
				Path path = Paths.get(picturesPath + picture.getPictureName());
				Files.delete(path);
				plantPictureRepository.delete(picture);
				sensorStation.getPlantPictures().remove(picture);
			}
			save(sensorStation);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("Failed to delete pictue of the server", 500);
		}
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

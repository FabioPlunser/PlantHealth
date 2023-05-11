package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;
import jakarta.transaction.Transactional;

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
	@Value("${swa.pictures.path}")
	private String picturesPath;

	public SensorStation findById(UUID id) throws ServiceException {
		Optional<SensorStation> maybeSensorStation = this.sensorStationRepository.findById(id);
		if (maybeSensorStation.isEmpty()) {
			throw new ServiceException("Could not find SensorStation", 404);
		}
		return maybeSensorStation.get();
	}

	public List<SensorStation> findAll() {
		return sensorStationRepository.findAll();
	}

	public SensorStation findByBdAddress(String bdAddress) throws ServiceException {
		Optional<SensorStation> maybeSensorStation =
				this.sensorStationRepository.findByBdAddress(bdAddress);
		if (maybeSensorStation.isEmpty()) {
			throw new ServiceException("Could not find SensorStation", 404);
		}
		return maybeSensorStation.get();
	}

	public SensorStation save(SensorStation sensorStation) throws ServiceException {
		try {
			return sensorStationRepository.save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Could not save SensorStation", 500);
		}
	}

	/**
	 * Set unlock status of sensor station.
	 * @param unlocked
	 * @param sensorStationId
	 * @return
	 * @throws ServiceException
	 */
	public void setUnlocked(boolean unlocked, UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		sensorStation.setUnlocked(unlocked);
		save(sensorStation);
	}

	/**
	 * Set sensor limits of sensor station.
	 * @param sensorLimits
	 * @param sensorStation
	 * @return
	 */
	@Transactional
	public void setSensorLimits(
			List<SensorLimits> sensorLimits, SensorStation sensorStation, Person person
	) throws ServiceException {
		if (!sensorStation.isUnlocked()) throw new ServiceException("SensorStation is locked", 403);
		if (sensorStation.isDeleted()) throw new ServiceException("SensorStation is deleted", 403);
		for (SensorLimits limit : sensorLimits) {
			Optional<Sensor> maybeSensor = sensorRepository.findByType(limit.getSensor().getType());
			if (maybeSensor.isEmpty())
				throw new ServiceException(
						"Sensor " + limit.getSensor().getType() + " not found", 500
				);
			Sensor sensor = maybeSensor.get();
			SensorLimits newLimit = new SensorLimits(
					LocalDateTime.now(), limit.getLowerLimit(), limit.getUpperLimit(),
					limit.getThresholdDuration(), sensor, person, sensorStation
			);
			try {
				sensorLimitsRepository.save(newLimit);
			} catch (Exception e) {
				throw new ServiceException("Could not save sensor limits", 500);
			}
		}
		//		List<SensorLimits> allLimits = sensorLimitsRepository.findAll();
		//		sensorStation.setSensorLimits(allLimits);
		//		save(sensorStation);
	}

	/**
	 * Update name of SensorStation
	 * @param sensorStation
	 * @param name
	 */
	public void updateSensorStation(SensorStation sensorStation, String name) {
		sensorStation.setName(name);
		save(sensorStation);
	}

	/**
	 * Get pictures of SensorStation
	 * @param sensorStationId
	 * @return list of base64 encoded pictures
	 */
	public List<SensorStationPicture> getPictures(UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		return sensorStation.getSensorStationPictures();
	}

	public SensorStationPicture getPicture(UUID pictureId) throws ServiceException {
		Optional<SensorStationPicture> maybePicture = plantPictureRepository.findById(pictureId);
		if (maybePicture.isEmpty()) throw new ServiceException("Picture does not exist", 404);
		return maybePicture.get();
	}

	public SensorStationPicture getNewestPicture(UUID sensorStationId) throws ServiceException {
		try {
			SensorStation sensorStation = findById(sensorStationId);
			return plantPictureRepository.findDistinctFirstBySensorStationOrderByTimeStampDesc(
					sensorStation
			);
		} catch (Exception e) {
			throw new ServiceException("Could not get newest picture", 500);
		}
	}

	/**
	 * uploadPicture
	 * @param picture base64 encoded picture
	 * @return true if upload was successful
	 */
	public void uploadPicture(MultipartFile picture, UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		SensorStationPicture plantPicture = null;
		try {
			String extension = Objects.requireNonNull(picture.getContentType()).split("/")[1];
			String picturePath = picturesPath + UUID.randomUUID() + "." + extension;
			Path path = Paths.get(picturePath);

			plantPicture =
					new SensorStationPicture(sensorStation, picturePath, LocalDateTime.now());
			plantPictureRepository.save(plantPicture);

			Files.createDirectories(path.getParent());
			Files.write(path, picture.getBytes());
		} catch (Exception e) {
			throw new ServiceException("Could not save picture", 500);
		}

		List<SensorStationPicture> sensorStationPictures = sensorStation.getSensorStationPictures();
		sensorStationPictures.add(plantPicture);
		sensorStation.setSensorStationPictures(sensorStationPictures);
		save(sensorStation);
	}

	public byte[] convertPictureToByteArray(SensorStationPicture picture) throws ServiceException {
		try {
			return Files.readAllBytes(Paths.get(picture.getPicturePath()));
		} catch (Exception e) {
			throw new ServiceException("Could not get picture resource", 500);
		}
	}

	public String getPictureName(UUID pictureId) throws ServiceException {
		SensorStationPicture picture = getPicture(pictureId);
		return picture.getPicturePath().split("/")[2];
	}

	/**
	 * Delete specific picture of sensor station
	 * @param pictureId
	 * @return
	 * @throws ServiceException
	 */
	public void deletePicture(UUID pictureId) throws ServiceException {
		Optional<SensorStationPicture> maybePicture = plantPictureRepository.findById(pictureId);
		if (maybePicture.isEmpty()) throw new ServiceException("Picture does not exist", 404);
		SensorStationPicture picture = maybePicture.get();
		SensorStation sensorStation = picture.getSensorStation();

		try {
			Path path = Paths.get(picture.getPicturePath());
			Files.delete(path);
			plantPictureRepository.delete(picture);
			sensorStation.getSensorStationPictures().remove(picture);
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
		SensorStation sensorStation = findById(sensorStationId);
		List<SensorStationPicture> pictures =
				new ArrayList<>(sensorStation.getSensorStationPictures());
		try {
			for (SensorStationPicture picture : pictures) {
				try {
					Path path = Paths.get(picture.getPicturePath());
					Files.delete(path);
					plantPictureRepository.delete(picture);
				} catch (Exception e) {
					plantPictureRepository.delete(picture);
					throw new ServiceException("Picture already deleted from server", 500);
				}

				sensorStation.getSensorStationPictures().remove(picture);
			}
			save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Failed to delete picture of the server", 500);
		}
	}

	public void addSensorData(SensorStation sensorStation, SensorData data)
			throws ServiceException {
		if (data == null || sensorStation == null) throw new ServiceException("Invalid data", 400);
		Sensor sensor = data.getSensor();
		Optional<Sensor> maybeSensor = sensorRepository.findByType(sensor.getType());
		if (maybeSensor.isPresent()) {
			sensor = maybeSensor.get();
		} else {
			sensor = sensorRepository.save(sensor);
		}

		data.setSensor(sensor);
		data.setSensorStation(sensorStation);
		this.sensorDataRepository.save(data);
	}

	public void addSensorData(SensorStation sensorStation, List<SensorData> dataList)
			throws ServiceException {
		if (dataList == null || sensorStation == null)
			throw new ServiceException("Invalid data", 400);

		for (SensorData data : dataList) {
			try {
				this.addSensorData(sensorStation, data);
			} catch (ServiceException e) {
				throw new ServiceException("Could not save sensor data", 500);
			}
		}
	}

	public void isDeleted(SensorStation sensorStation) throws ServiceException {
		if (sensorStation.isDeleted()) {
			throw new ServiceException("Sensor station is deleted", 400);
		}
	}
}

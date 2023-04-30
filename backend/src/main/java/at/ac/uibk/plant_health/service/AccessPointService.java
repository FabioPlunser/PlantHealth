package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import jakarta.persistence.Access;

@Service
public class AccessPointService {
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorStationService sensorStationService;

	public Optional<AccessPoint> findById(UUID id) {
		return this.accessPointRepository.findById(id);
	}

	public Optional<AccessPoint> findBySelfAssignedId(UUID id) {
		return this.accessPointRepository.findBySelfAssignedId(id);
	}

	public int updateLastConnection() {
		return this.accessPointRepository.updateIsConnectedByLastConnection();
	}

	/**
	 * Get all AccessPoints.
	 * @return List of AccessPoints
	 */
	public List<AccessPoint> findAllAccessPoints() {
		return accessPointRepository.findAll();
	}

	/**
	 * Register a new AccessPoint.
	 * Check if AccessPoint is Unlocked.
	 * @param selfAssignedId
	 * @param roomName
	 * @return
	 */
	public boolean register(UUID selfAssignedId, String roomName) {
		if (selfAssignedId == null || roomName == null) return false;

		return this.create(selfAssignedId, roomName);
	}

	/**
	 * Save the given AccessPoint.
	 * If the AccessPoint with the given ID already exists, don't save it and return false.
	 * returns false if accessPoint already exists
	 * @param selfAssignedId
	 * @param roomName
	 */
	public boolean create(UUID selfAssignedId, String roomName) {
		if (selfAssignedId == null && roomName == null) {
			return false;
		}
		if (isAccessPointRegisteredBySelfAssignedId(selfAssignedId)) {
			return false;
		}
		AccessPoint accessPoint = new AccessPoint(selfAssignedId, roomName, false);
		return save(accessPoint) != null;
	}

	/**
	 * Get the AccessPoint with the given selfAssignedId.
	 * @param selfAssignedId
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean isUnlockedBySelfAssignedId(UUID selfAssignedId) throws IllegalArgumentException {
		Optional<AccessPoint> maybeAccessPoint =
				accessPointRepository.findBySelfAssignedId(selfAssignedId);
		if (maybeAccessPoint.isEmpty())
			throw new IllegalArgumentException(
					"AccessPoint with ID " + selfAssignedId + " does not exist."
			);
		AccessPoint accessPoint = maybeAccessPoint.get();
		return accessPoint.isUnlocked();
	}

	/**
	 * Get the AccessPoint with the given deviceId.
	 * @param deviceId
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean isUnlockedByDeviceId(UUID deviceId) throws IllegalArgumentException {
		Optional<AccessPoint> maybeAccessPoint = accessPointRepository.findById(deviceId);
		if (maybeAccessPoint.isEmpty())
			throw new IllegalArgumentException(
					"AccessPoint with ID " + deviceId + " does not exist."
			);
		AccessPoint accessPoint = maybeAccessPoint.get();
		return accessPoint.isUnlocked();
	}

	/**
	 * Check if the AccessPoint with the given ID is registered.
	 * @param selfAssignedId
	 * @return
	 */
	public boolean isAccessPointRegisteredBySelfAssignedId(UUID selfAssignedId) {
		Optional<AccessPoint> maybeAccessPoint =
				accessPointRepository.findBySelfAssignedId(selfAssignedId);
		return maybeAccessPoint.isPresent();
	}

	public boolean isAccessPointRegisteredByDeviceId(UUID deviceId) throws ServiceException {
		Optional<AccessPoint> maybeAccessPoint = accessPointRepository.findById(deviceId);
		return maybeAccessPoint.isPresent();
	}

	/**
	 * Save the given AccessPoint.
	 * If the AccessPoint with the given ID already exists, update its Config and return false.
	 *
	 * @param accessPoint The AccessPoint to save.
	 * @return the saved AccessPoint
	 */
	public AccessPoint save(AccessPoint accessPoint) {
		try {
			return accessPointRepository.save(accessPoint);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Start a scan for SensorStations.
	 * @param deviceId
	 * @return true if the scan flag was set, false otherwise
	 */
	public boolean startScan(UUID deviceId) {
		AccessPoint accessPoint = getAccessPointByDeviceId(deviceId);
		accessPoint.setScanActive(true);
		return save(accessPoint) != null;
	}

	/**
	 * Get the AccessPoint's access token.
	 * @param selfAssignedId
	 * @return
	 */
	public UUID getAccessPointAccessToken(UUID selfAssignedId) {
		AccessPoint accessPoint = getAccessPointBySelfAssignedId(selfAssignedId);
		return accessPoint.getAccessToken();
	}

	/**
	 *
	 * @param unlocked
	 * @param deviceId
	 * @return true if the AccessPoint was unlocked, false otherwise
	 */
	public boolean setUnlocked(boolean unlocked, UUID deviceId) {
		AccessPoint accessPoint = null;
		try {
			accessPoint = getAccessPointByDeviceId(deviceId);
		} catch (IllegalArgumentException e) {
			return false;
		}

		if (unlocked) {
			UUID token = accessPoint.getAccessToken();

			if (token == null) {
				token = UUID.randomUUID();
			}

			accessPoint.setAccessToken(token);
		} else {
			accessPoint.setAccessToken(null);
		}
		accessPoint.setUnlocked(unlocked);
		return save(accessPoint) != null;
	}

	public boolean foundNewSensorStation(
			AccessPoint accessPoint, List<SensorStation> sensorStationList
	) {
		sensorStationList.forEach(sensorStation -> {
			sensorStation.setAccessPoint(accessPoint);
			sensorStationService.save(sensorStation);
		});
		accessPoint.setSensorStations(sensorStationList);
		accessPoint.setScanActive(false);
		return save(accessPoint) != null;
	}

	/**
	 * Set the transfer interval of the AccessPoint with the given ID.
	 * @param accessPointId
	 * @param interval
	 * @throws ServiceException
	 */
	public void setTransferInterval(UUID accessPointId, int interval) throws ServiceException {
		AccessPoint accessPoint = getAccessPointByDeviceId(accessPointId);
		accessPoint.setTransferInterval(interval);
		save(accessPoint);
	}

	/**
	 * Set data of list of SensorStations.
	 * @param sensorStations
	 * @return true if the data was set, false otherwise
	 */
	@Transactional
	public boolean setSensorStationData(List<SensorStation> sensorStations) {
		for (SensorStation sensorStation : sensorStations) {
			Optional<SensorStation> maybeSensorStation =
					sensorStationService.findByBdAddress(sensorStation.getBdAddress());
			if (maybeSensorStation.isEmpty()) return false;
			SensorStation dbSensorStation = maybeSensorStation.get();
			if (!sensorStationService.addSensorData(
						dbSensorStation, sensorStation.getSensorData()
				)) {
				return false;
			};
		}
		return true;
	}

	/**
	 * Find an AccessPoint by its selfAssignedId.
	 * @param selfAssignedId
	 * @return AccessPoint
	 */
	public AccessPoint getAccessPointBySelfAssignedId(UUID selfAssignedId) {
		Optional<AccessPoint> maybeAccessPoint =
				accessPointRepository.findBySelfAssignedId(selfAssignedId);
		if (maybeAccessPoint.isEmpty())
			throw new ServiceException(
					"AccessPoint with ID " + selfAssignedId + " does not exist.", 404
			);
		return maybeAccessPoint.get();
	}

	/**
	 * Find an AccessPoint by its deviceId.
	 * @param deviceId
	 * @return
	 */
	public AccessPoint getAccessPointByDeviceId(UUID deviceId) {
		Optional<AccessPoint> maybeAccessPoint = accessPointRepository.findById(deviceId);
		if (maybeAccessPoint.isEmpty())
			throw new ServiceException("AccessPoint with ID " + deviceId + "does not exist", 404);
		return maybeAccessPoint.get();
	}
}

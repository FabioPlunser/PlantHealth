package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.repositories.AccessPointRepository;
import jakarta.transaction.Transactional;

@Service
public class AccessPointService {
	@Autowired
	private AccessPointRepository accessPointRepository;
	@Autowired
	private SensorStationService sensorStationService;

	/**
	 * Find the AccessPoint with the given ID.
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public AccessPoint findById(UUID id) throws ServiceException {
		Optional<AccessPoint> maybeAccessPoint = this.accessPointRepository.findById(id);
		if (maybeAccessPoint.isEmpty()) {
			throw new ServiceException("Could not find AccessPoint", 404);
		}
		return maybeAccessPoint.get();
	}

	/**
	 * Find the AccessPoint with the given selfAssignedId.
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public AccessPoint findBySelfAssignedId(UUID id) throws ServiceException {
		Optional<AccessPoint> maybeAccessPoint =
				this.accessPointRepository.findBySelfAssignedId(id);
		if (maybeAccessPoint.isEmpty()) {
			throw new ServiceException("Could not find AccessPoint", 404);
		}
		return maybeAccessPoint.get();
	}

	/**
	 * Update the lastConnection of the given AccessPoint.
	 * @return
	 */
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
	 * @param selfAssignedId The selfAssignedId of the AccessPoint to register.
	 * @param roomName The roomName of the AccessPoint to register.
	 * @throws ServiceException if the AccessPoint could not be registered.
	 */
	@AuditLogAnnotation(successMessage = "Registered AccessPoint {selfAssignedId} {roomName}")
	public void register(UUID selfAssignedId, String roomName) throws ServiceException {
		if (selfAssignedId == null || roomName == null) {
			throw new ServiceException("Could not register AccessPoint", 400);
		}

		this.create(selfAssignedId, roomName);
	}

	/**
	 * Save the given AccessPoint.
	 * If the AccessPoint with the given ID already exists, don't save it and return false.
	 * returns false if accessPoint already exists
	 * @param selfAssignedId The selfAssignedId of the AccessPoint to save.
	 * @param roomName The roomName of the AccessPoint to save.
	 * @throws ServiceException if the AccessPoint could not be saved.
	 */
	public void create(UUID selfAssignedId, String roomName) throws ServiceException {
		if (selfAssignedId == null && roomName == null) {
			throw new ServiceException("Could not create AccessPoint", 400);
		}
		try {
			// AccessPoint already exists quietly abort
			setLastConnection(findBySelfAssignedId(selfAssignedId));
			return;
		} catch (ServiceException e) {
			// AccessPoint does not exist
		}
		AccessPoint accessPoint = new AccessPoint(selfAssignedId, roomName, false);
		save(accessPoint);
		//		setLastConnection(findBySelfAssignedId(selfAssignedId));
	}

	/**
	 * Save the given AccessPoint.
	 * If the AccessPoint with the given ID already exists, update its Config and return false.
	 *
	 * @param accessPoint The AccessPoint to save.
	 * @return the saved AccessPoint
	 * @throws ServiceException if the AccessPoint could not be saved.
	 */
	public AccessPoint save(AccessPoint accessPoint) throws ServiceException {
		try {
			return accessPointRepository.save(accessPoint);
		} catch (Exception e) {
			throw new ServiceException("Could not save AccessPoint", 400);
		}
	}

	/**
	 * Get the AccessPoint with the given selfAssignedId.
	 * @param selfAssignedId The selfAssignedId of the AccessPoint to get.
	 * @throws ServiceException if the AccessPoint is locked.
	 */
	public void isUnlockedBySelfAssignedId(UUID selfAssignedId) throws ServiceException {
		AccessPoint accessPoint = findBySelfAssignedId(selfAssignedId);
		if (!accessPoint.isUnlocked()) {
			throw new ServiceException("AccessPoint is locked", 401);
		}
	}

	/**
	 * Get the AccessPoint with the given deviceId.
	 * @param deviceId The deviceId of the AccessPoint to get.
	 * @throws ServiceException if the AccessPoint is locked.
	 */
	public void isUnlockedByDeviceId(UUID deviceId) throws ServiceException {
		AccessPoint accessPoint = findById(deviceId);
		if (!accessPoint.isUnlocked()) {
			throw new ServiceException("AccessPoint is locked", 401);
		}
	}

	/**
	 * Get the AccessPoint's access token.
	 * @param selfAssignedId
	 * @return
	 */
	public UUID getAccessPointAccessToken(UUID selfAssignedId) {
		AccessPoint accessPoint = findBySelfAssignedId(selfAssignedId);
		setLastConnection(accessPoint);
		return accessPoint.getAccessToken();
	}

	/**
	 *
	 * @param unlocked
	 * @param deviceId
	 * @return true if the AccessPoint was unlocked, false otherwise
	 */
	@AuditLogAnnotation(successMessage = "Unlocked AccessPoint {deviceId}")
	public void setUnlocked(boolean unlocked, UUID deviceId) throws ServiceException {
		AccessPoint accessPoint = null;
		accessPoint = findById(deviceId);

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
		save(accessPoint);
	}

	/**
	 * Save and set found SensorStations of the given AccessPoint.
	 * @param accessPoint AccessPoint which found the SensorStations.
	 * @param sensorStationList List of found SensorStations.
	 * @throws ServiceException if the SensorStations could not be saved.
	 */
	public void foundNewSensorStation(
			AccessPoint accessPoint, List<SensorStation> sensorStationList
	) throws ServiceException {
		for (SensorStation sensorStation : sensorStationList) {
			if (sensorStationService.sensorStationExists(sensorStation.getBdAddress()) != null) {
				SensorStation dbSensorStation =
						sensorStationService.findByBdAddress(sensorStation.getBdAddress());
				dbSensorStation.setDipSwitchId(sensorStation.getDipSwitchId());
				dbSensorStation.setAccessPoint(accessPoint);
				dbSensorStation.setConnected(true);
				sensorStationService.save(dbSensorStation);
				continue;
			}
			sensorStation.setConnected(true);
			sensorStation.setAccessPoint(accessPoint);
			sensorStationService.save(sensorStation);
		}
		accessPoint.setSensorStations(sensorStationList);
		accessPoint.setScanActive(false);
		setLastConnection(accessPoint);
		save(accessPoint);
	}

	/**
	 * Start a scan for SensorStations.
	 * @param deviceId The deviceId of the AccessPoint to start the scan for.
	 * @throws ServiceException if the AccessPoint could not be found.
	 */
	@AuditLogAnnotation(successMessage = "Set AccessPoint, {deviceId}, scan to {scanActive}")
	public void setScan(UUID deviceId, boolean scanActive) throws ServiceException {
		AtomicReference<AccessPoint> accessPoint = new AtomicReference<>(findById(deviceId));
		if (!accessPoint.get().isConnected()) {
			throw new ServiceException("AccessPoint is not connected", 400);
		}
		accessPoint.get().setScanActive(scanActive);
		save(accessPoint.get());

		ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
		ex.schedule(() -> {
			try {
				accessPoint.set(findById(deviceId));
				accessPoint.get().setScanActive(false);
				save(accessPoint.get());
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}, 5, TimeUnit.MINUTES);
	}

	/**
	 * Update the AccessPoint's room name and transfer interval.
	 * @param deviceId
	 * @param roomName
	 * @param transferInterval
	 * @throws ServiceException
	 */
	@AuditLogAnnotation(
			successMessage = "Updated AccessPoint, {deviceId}, to {roomName}, {transferInterval}"
	)
	public void
	updateAccessPointInfo(UUID deviceId, String roomName, int transferInterval)
			throws ServiceException {
		AccessPoint accessPoint = findById(deviceId);
		accessPoint.setRoomName(roomName);
		accessPoint.setTransferInterval(transferInterval);
		save(accessPoint);
	}

	/**
	 * Set data of list of SensorStations.
	 * @param sensorStations List of SensorStations to set data for.
	 * @throws ServiceException if sensorData could not be set.
	 */
	public void setSensorStationData(List<SensorStation> sensorStations, AccessPoint accessPoint)
			throws ServiceException {
		try {
			for (SensorStation sensorStation : sensorStations) {
				SensorStation dbSensorStation =
						sensorStationService.findByBdAddress(sensorStation.getBdAddress());
				if (!dbSensorStation.isUnlocked())
					throw new ServiceException("SensorStation is locked", 409);
				sensorStationService.addSensorData(dbSensorStation, sensorStation.getSensorData());
			}
			setLastConnection(accessPoint);
		} catch (ServiceException s) {
			throw s;
		} catch (Exception e) {
			throw new ServiceException("Could not set SensorStation data.", 500);
		}
	}

	/**
	 * Set the last connection of the AccessPoint to now.
	 * @param accesspoint
	 */
	public void setLastConnection(AccessPoint accesspoint) {
		accesspoint.setLastConnection(LocalDateTime.now());
		accesspoint.setConnected(true);
		save(accesspoint);
	}

	/**
	 * Deletes an AccessPoint
	 * @param accessPointId
	 * @throws ServiceException
	 */
	@AuditLogAnnotation(successMessage = "Deleted AccessPoint {accessPointId}")
	public void deleteAccessPoint(UUID accessPointId) throws ServiceException {
		Optional<AccessPoint> maybeAccessPoint = accessPointRepository.findById(accessPointId);
		if (maybeAccessPoint.isEmpty()) {
			throw new ServiceException("AccessPoint not found", 404);
		}
		AccessPoint accessPoint = maybeAccessPoint.get();
		if (accessPoint.isDeleted()) {
			throw new ServiceException("AccessPoint already deleted", 404);
		}
		accessPoint.setDeleted(true);
		accessPoint.setUnlocked(false);
		accessPoint.setSelfAssignedId(null);
		accessPoint.setAccessToken(null);
		for (SensorStation sensorStation : accessPoint.getSensorStations()) {
			sensorStationService.deleteSensorStation(sensorStation.getDeviceId());
		}
		accessPointRepository.save(accessPoint);
	}

	/**
	 * Get all SensorStations of an AccessPoint
	 * @param accessPointId
	 * @throws ServiceException
	 */
	public List<SensorStation> getAccessPointSensorStations(UUID accessPointId)
			throws ServiceException {
		AccessPoint accessPoint = findById(accessPointId);
		return accessPoint.getSensorStations();
	}

	/**
	 * Set all new sensor stations of an access point to reported
	 * @param accessPoints
	 * @throws ServiceException
	 */
	public void setAccessPointSensorStationsReported(List<AccessPoint> accessPoints)
			throws ServiceException {
		for (AccessPoint accessPoint : accessPoints) {
			for (SensorStation sensorStation : accessPoint.getSensorStations()) {
				if (sensorStation.isReported()) continue;
				sensorStation.setReported(true);
				sensorStationService.save(sensorStation);
			}
		}
	}
}

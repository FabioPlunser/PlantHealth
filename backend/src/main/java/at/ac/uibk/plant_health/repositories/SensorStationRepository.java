package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;

public interface SensorStationRepository extends CrudRepository<SensorStation, UUID> {
	@Override
	List<SensorStation> findAll();

	@Override
	Optional<SensorStation> findById(UUID deviceId);

	List<SensorStation> findByIsUnlockedAndIsDeleted(boolean isUnlocked, boolean isDeleted);

	Optional<SensorStation> findByBdAddress(String bdAddress);

	default List<SensorStation> findNewForGardener(Person gardener) {
		return findByGardenerAndSensorStationPersonReferences_PersonAndIsUnlockedTrueAndIsDeletedFalseAndSensorStationPersonReferences_IsAssignedFalseAndSensorStationPersonReferences_InDashboardFalse(
				gardener, gardener
		);
	}

	default List<SensorStation> findNewForUser(Person person) {
		return findByIsUnlockedTrueAndIsDeletedFalseAndSensorStationPersonReferences_PersonAndSensorStationPersonReferences_InDashboardFalse(
				person
		);
	}

	List<SensorStation>
	findByGardenerAndSensorStationPersonReferences_PersonAndIsUnlockedTrueAndIsDeletedFalseAndSensorStationPersonReferences_IsAssignedFalseAndSensorStationPersonReferences_InDashboardFalse(
			Person gardener, Person person
	);

	List<SensorStation>
	findByIsUnlockedTrueAndIsDeletedFalseAndSensorStationPersonReferences_PersonAndSensorStationPersonReferences_InDashboardFalse(
			Person person
	);
}

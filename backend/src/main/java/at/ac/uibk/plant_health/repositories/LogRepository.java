package at.ac.uibk.plant_health.repositories;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import at.ac.uibk.plant_health.models.Log;

public interface LogRepository extends CrudRepository<Log, UUID> {
	List<Log> findByTimeStampBetween(LocalDateTime timeStampStart, LocalDateTime timeStampEnd);
}

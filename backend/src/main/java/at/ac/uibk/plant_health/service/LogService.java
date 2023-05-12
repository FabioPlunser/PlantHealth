package at.ac.uibk.plant_health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.repositories.LogRepository;

@Service
public class LogService {
	@Autowired
	private LogRepository logRepository;

	public List<Log> findBetween(LocalDateTime start, LocalDateTime end) {
		return logRepository.findByTimeStampBetween(end, start);
	}

	public List<Log> findAll() {
		return logRepository.findAll();
	}

	public boolean log(Log log) {
		try {
			log.setTimeStamp(LocalDateTime.now());
			this.logRepository.save(log);
			return true;
		} catch (Exception e) {
			// If Logging failed don't rethrow as this would do more harm than good.
			// Return false to inform the Caller if they want to know.
		}
		return false;
	}
}

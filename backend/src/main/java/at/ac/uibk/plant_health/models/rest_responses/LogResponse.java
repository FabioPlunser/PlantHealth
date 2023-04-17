package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.MODULE)
public class LogResponse extends RestResponse implements Serializable {
	private List<InnerLog> logs;

	public LogResponse(List<Log> logs) {
		this.logs = logs.stream().map(InnerLog::new).toList();
	}

	private class InnerLog {
		public Log.LogLevel severity;
		public LocalDateTime timestamp;
		public String message;
		public String className;
		public String callerId;

		public InnerLog(Log log) {
			this.severity = log.getSeverity();
			this.timestamp = log.getTimeStamp();
			this.message = log.getMessage();
			this.className = log.getClassName();
			this.callerId = log.getCallerId();
		}
	}
}
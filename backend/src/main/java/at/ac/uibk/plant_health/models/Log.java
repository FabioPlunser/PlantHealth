package at.ac.uibk.plant_health.models;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Log implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "log_id", nullable = false)
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	private UUID logId;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "severity", nullable = false)
	private LogLevel severity;

	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@Column(name = "time_stamp", nullable = false)
	private LocalDateTime timeStamp;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "message", nullable = false)
	private String message;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "caller_type", nullable = true)
	private String className;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "caller_id", nullable = true)
	private String callerId;

	public Log(LogLevel level, String message) {
		this(level, message, null, null);
	}

	public Log(LogLevel level, String message, String className, String callerId) {
		this.severity = level;
		this.message = message;
		this.timeStamp = LocalDateTime.now();
		this.callerId = callerId;
		this.className = className;
	}

	public enum LogLevel {
		FATAL(32),
		ERROR(16),
		WARN(8),
		INFO(4),
		DEBUG(2),
		TRACE(1),
		ALL(63),
		OFF(0);

		@Getter
		private final int level;
		LogLevel(int value) {
			this.level = value;
		}
	}

	@Converter(autoApply = true)
	public static class LogLevelConverter implements AttributeConverter<LogLevel, String> {
		@Override
		public String convertToDatabaseColumn(LogLevel logLevel) {
			if (logLevel == null) {
				return null;
			}
			return logLevel.toString();
		}

		@Override
		public LogLevel convertToEntityAttribute(String logLevel) {
			if (logLevel == null) {
				return null;
			}

			return LogLevel.valueOf(logLevel);
		}
	}
}

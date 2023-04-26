package at.ac.uibk.plant_health.models.plant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorLimits implements Serializable {
	@Id
	@Column(name = "time_stamp", nullable = false)
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timeStamp;

	@JdbcTypeCode(SqlTypes.FLOAT)
	@Column(name = "upper_limit", nullable = false)
	private float upperLimit;

	@JdbcTypeCode(SqlTypes.FLOAT)
	@Column(name = "lower_limit", nullable = false)
	private float lowerLimit;

	/**
	 * in seconds
	 */
	@JdbcTypeCode(SqlTypes.FLOAT)
	@Column(name = "threshold_duration", nullable = false)
	private int thresholdDuration;

	@ManyToOne
	@JoinColumn(name = "sensor_type", nullable = false)
	private Sensor sensor;

	@ManyToOne(optional = false)
	@JoinColumn(name = "gardener_id", nullable = false)
	private Person gardener;

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "sensor_station_id", nullable = false)
	private SensorStation sensorStation;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Override
	public String toString() {
		return "SensorLimits [timeStamp=" + timeStamp + ", aboveLimit=" + upperLimit
				+ ", belowLimit=" + lowerLimit + ", sensor=" + sensor + ", gardener=" + gardener
				+ ", sensorStation=" + sensorStation + ", isDeleted=" + isDeleted + "]";
	}
}
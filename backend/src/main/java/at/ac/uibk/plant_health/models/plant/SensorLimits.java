package at.ac.uibk.plant_health.models.plant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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
	@Column(name = "sensor_limits_id", nullable = false)
	@JdbcTypeCode(SqlTypes.UUID)
	@JsonIgnore
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID sensorLimitsId;

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
	@JdbcTypeCode(SqlTypes.INTEGER)
	@Column(name = "threshold_duration", nullable = false)
	private int thresholdDuration;

	//	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(optional = false)
	@JoinColumn(name = "sensor_type", nullable = false)
	private Sensor sensor;

	@JsonIgnore
	@Fetch(FetchMode.SELECT)
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

	public SensorLimits(
			LocalDateTime timeStamp, float upperLimit, float lowerLimit, int thresholdDuration,
			Sensor sensor, Person gardener, SensorStation sensorStation
	) {
		super();
		this.timeStamp = timeStamp;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
		this.thresholdDuration = thresholdDuration;
		this.sensor = sensor;
		this.gardener = gardener;
		this.sensorStation = sensorStation;
	}

	@Override
	public String toString() {
		return "SensorLimits [timeStamp=" + timeStamp + ", aboveLimit=" + upperLimit
				+ ", belowLimit=" + lowerLimit + ", thresholdDuration=" + thresholdDuration
				+ ", sensor=" + sensor + ", gardener=" + gardener + ", isDeleted=" + isDeleted
				+ "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SensorLimits that = (SensorLimits) o;
		return Objects.equals(sensorLimitsId, that.sensorLimitsId)
				&& Objects.equals(gardener, that.gardener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sensorLimitsId, gardener);
	}
}
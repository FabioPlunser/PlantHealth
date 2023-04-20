package at.ac.uibk.plant_health.models.plant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

import at.ac.uibk.plant_health.models.device.SensorStation;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(SensorData.SensorDataId.class)
public class SensorData implements Serializable {
	static class SensorDataId implements Serializable {
		private LocalDateTime timeStamp;
		private Sensor sensor;
	}
	@Id
	@Column(name = "time_stamp", nullable = false)
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	private LocalDateTime timeStamp;

	@JdbcTypeCode(SqlTypes.DOUBLE)
	@Column(name = "sensor_value", nullable = false)
	private double value;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "above_limit", nullable = false)
	private boolean aboveLimit;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "below_limit", nullable = false)
	private boolean belowLimit;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "alarm", nullable = false)
	private String alarm;

	@Id
	@ManyToOne
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@ManyToOne(optional = false)
	@JoinColumn(name = "sensor_station_id", nullable = false)
	private SensorStation sensorStation;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	public SensorData(
			LocalDateTime timeStamp, double value, boolean above, boolean below, String alarm,
			Sensor sensor, SensorStation sensorStation
	) {
		this.timeStamp = timeStamp;
		this.value = value;
		this.belowLimit = below;
		this.aboveLimit = above;
		this.alarm = alarm;
		this.sensor = sensor;
		this.sensorStation = sensorStation;
	}

	@JsonIgnore
	public char getAlarm() {
		if (this.aboveLimit) {
			return 'h';
		} else if (this.belowLimit) {
			return 'l';
		}
		return 'n';
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof SensorData)) {
			return false;
		}
		SensorData other = (SensorData) obj;
		return this.timeStamp.equals(other.timeStamp) && this.sensor.equals(other.sensor);
	}

	@Override
	public int hashCode() {
		return this.timeStamp.hashCode() + this.sensor.hashCode();
	}
	@Override
	public String toString() {
		return "SensorData [timeStamp=" + timeStamp + ", value=" + value
				+ ", aboveLimit=" + aboveLimit + ", belowLimit=" + belowLimit + ", sensor=" + sensor
				+ ", isDeleted=" + isDeleted + "]";
	}
}
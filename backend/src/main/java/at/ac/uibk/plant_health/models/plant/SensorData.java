package at.ac.uibk.plant_health.models.plant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorData implements Serializable {
	@Id
	@Column(name = "sensor_data_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private UUID sensorDataId;

	@Column(name = "time_stamp", nullable = false)
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timeStamp;

	@JdbcTypeCode(SqlTypes.FLOAT)
	@Column(name = "sensor_value", nullable = false)
	private float value;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "sensor_alarm", nullable = false)
	private String alarm;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "above_limit", nullable = false)
	private boolean aboveLimit;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "below_limit", nullable = false)
	private boolean belowLimit;

	@ManyToOne
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "sensor_station_id", nullable = false)
	private SensorStation sensorStation;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	public SensorData(
			LocalDateTime timeStamp, float value, String alarm, Sensor sensor,
			SensorStation sensorStation
	) {
		this.timeStamp = timeStamp;
		this.value = value;
		this.alarm = alarm;
		switch (alarm) {
			case "h" -> {
				this.aboveLimit = true;
				this.belowLimit = false;
			}
			case "l" -> {
				this.aboveLimit = false;
				this.belowLimit = true;
			}
			default -> {
				this.aboveLimit = false;
				this.belowLimit = false;
			}
		}

		this.sensor = sensor;
		this.sensorStation = sensorStation;
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
		return "SensorData{"
				+ "timeStamp=" + timeStamp + ", value=" + value + ", aboveLimit=" + aboveLimit
				+ ", belowLimit=" + belowLimit + ", alarm='" + alarm + '\'' + ", sensor=" + sensor
				+ ", isDeleted=" + isDeleted + '}';
	}

}
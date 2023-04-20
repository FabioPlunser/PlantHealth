package at.ac.uibk.plant_health.models.plant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Sensor implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "sensor_id", nullable = false)
	private UUID sensorId;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "sensor_type", nullable = false, unique = true)
	private String type;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "sensor_unit", nullable = false)
	private String unit;

	public Sensor(String type, String unit) {
		this.type = type;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Sensor sensor = (Sensor) o;
		return Objects.equals(sensorId, sensor.sensorId) && Objects.equals(type, sensor.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sensorId, type);
	}

	@Override
	public String toString() {
		return "Sensor{"
				+ "sensorId=" + sensorId + ", type='" + type + '\'' + ", unit='" + unit + '\''
				+ '}';
	}
}

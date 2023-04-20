package at.ac.uibk.plant_health.models.plant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Sensor {
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
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Sensor)) {
			return false;
		}
		Sensor other = (Sensor) obj;
		return this.sensorId.equals(other.sensorId);
	}

	@Override
	public int hashCode() {
		return sensorId.hashCode();
	}
}

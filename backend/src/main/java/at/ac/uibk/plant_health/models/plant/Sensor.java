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

	public Sensor(String type) {
		this.type = type;
	}
}

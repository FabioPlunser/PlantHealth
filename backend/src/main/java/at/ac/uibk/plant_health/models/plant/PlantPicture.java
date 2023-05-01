package at.ac.uibk.plant_health.models.plant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(PlantPictureId.class)
public class PlantPicture {
	@Id
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "picture_id", nullable = false)
	private UUID pictureId;

	@Id
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@ManyToOne(optional = false)
	@PrimaryKeyJoinColumn(referencedColumnName = "sensor_station_id")
	private SensorStation sensorStationId;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "picture_path", nullable = false)
	private String picturePath;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;
}

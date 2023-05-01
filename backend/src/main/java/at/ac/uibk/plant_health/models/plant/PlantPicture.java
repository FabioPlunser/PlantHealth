package at.ac.uibk.plant_health.models.plant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantPicture {
	@Id
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	@Column(name = "picture_id", nullable = false)
	private UUID pictureId;

	@JsonIgnore
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@ManyToOne(optional = false)
	@JoinColumn(name = "sensor_station_id", nullable = false)
	private SensorStation sensorStation;

	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "picture_path", nullable = false)
	private String picturePath;

	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@Column(name = "time_stamp", nullable = false)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timeStamp;

	public PlantPicture(SensorStation sensorStation, String picturePath, LocalDateTime timeStamp) {
		this.sensorStation = sensorStation;
		this.picturePath = picturePath;
		this.timeStamp = timeStamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlantPicture that = (PlantPicture) o;
		return Objects.equals(pictureId, that.pictureId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pictureId);
	}

	@Override
	public String toString() {
		return "PlantPicture{"
				+ "pictureName='" + picturePath + '\'' + ", timeStamp=" + timeStamp + '}';
	}
}

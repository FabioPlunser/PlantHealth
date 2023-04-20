package at.ac.uibk.plant_health.models.plant;

import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import at.ac.uibk.plant_health.models.device.SensorStation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PlantPictureId implements Serializable {
	private SensorStation sensorStationId;

	private UUID pictureId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		PlantPictureId that = (PlantPictureId) o;
		return sensorStationId != null && Objects.equals(sensorStationId, that.sensorStationId);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
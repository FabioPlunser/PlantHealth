package at.ac.uibk.plant_health.models.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "sensor_station")
// NOTE: This changes the name of the "id"-Column inherited from Device to "sensor_station_id"
@AttributeOverride(name = "id", column = @Column(name = "sensor_station_id"))
public class SensorStation extends Device implements Serializable {
	@Column(name = "bd_address", unique = true)
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	private String bdAddress = null;

	@Column(name = "plant_name")
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	private String name = null;

	@JdbcTypeCode(SqlTypes.INTEGER)
	@Column(name = "dip_switch_id", nullable = false)
	private int dipSwitchId;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "access_point_id")
	private AccessPoint accessPoint;

	@OneToMany(mappedBy = "sensorStation", fetch = FetchType.EAGER)
	private List<SensorData> sensorData = new ArrayList<>();

	@OneToMany(mappedBy = "sensorStation", fetch = FetchType.EAGER, orphanRemoval = true)
	private List<SensorLimits> sensorLimits = new ArrayList<>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "sensorStation", orphanRemoval = true)
	private List<SensorStationPersonReference> sensorStationPersonReferences = new ArrayList<>();

	@OneToMany(
			fetch = FetchType.EAGER, mappedBy = "sensorStation", orphanRemoval = true,
			cascade = CascadeType.ALL
	)
	private List<PlantPicture> plantPictures = new ArrayList<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Set.of(DeviceType.SENSOR_STATION);
	}

	public SensorStation(String bdAddress, int dipSwitchId) {
		super();
		this.bdAddress = bdAddress;
		this.dipSwitchId = dipSwitchId;
	}

	public void addSensorStationReference(SensorStationPersonReference s) {
		this.sensorStationPersonReferences.add(s);
	}

	@Override
	public String toString() {
		return "SensorStation [bdAddress=" + bdAddress + ", name=" + name + ", dipSwitchId="
				+ dipSwitchId + ", accessPoint=" + accessPoint + ", sensorStationPersonReferences="
				+ sensorStationPersonReferences + ", sensorData=" + sensorData + "]";
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), bdAddress);
	}
}

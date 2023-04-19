package at.ac.uibk.plant_health.models.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import at.ac.uibk.plant_health.models.IdentifiedEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class Device implements UserDetails, IdentifiedEntity {
	// region Fields
	@Id
	// NOTE: Classes that extend this should create a Getter with
	//       @JsonInclude to rename the ID for JSON-Serialisation.
	@JsonIgnore
	@Setter(AccessLevel.PRIVATE)
	@JdbcTypeCode(SqlTypes.NVARCHAR)
	@Column(name = "device_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID deviceId;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_unlocked", nullable = false)
	private boolean isUnlocked = false;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_connected", nullable = false)
	private boolean isConnected = false;

	@JdbcTypeCode(SqlTypes.BOOLEAN)
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;
	// endregion

	// region equals, hashCode, toString
	@Override
	public boolean equals(Object o) {
		return (this == o)
				|| ((o instanceof Device a) && (this.deviceId != null)
					&& (this.deviceId.equals(a.deviceId)));
	}

	@Override
	public int hashCode() {
		// NOTE: This will intentionally throw an Exception if the Id is null.
		return this.deviceId.hashCode();
	}
	// endregion

	// region UserDetails Implementation
	@Override
	public String getUsername() {
		return this.toString();
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return this.isUnlocked;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}
	// endregion

	@JsonIgnore
	public String getStringIdentification() {
		return this.deviceId.toString();
	}
}

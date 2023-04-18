package at.ac.uibk.plant_health.models.device;

import org.springframework.security.core.GrantedAuthority;

public enum DeviceType implements GrantedAuthority {
	SENSOR_STATION,
	ACCESS_POINT;

	@Override
	public String getAuthority() {
		return this.toString();
	}
}

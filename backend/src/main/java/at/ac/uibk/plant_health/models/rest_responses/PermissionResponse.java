package at.ac.uibk.plant_health.models.rest_responses;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PermissionResponse extends RestResponse implements Serializable {
	private final GrantedAuthority[] permissions;

	public PermissionResponse(GrantedAuthority[] permissions) {
		super();
		this.permissions = permissions;
	}
}

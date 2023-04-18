package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.MODULE)
public class PermissionResponse extends RestResponse implements Serializable {
	private GrantedAuthority[] permissions;

	public PermissionResponse(GrantedAuthority[] permissions) {
		super();
		this.permissions = permissions;
	}
}

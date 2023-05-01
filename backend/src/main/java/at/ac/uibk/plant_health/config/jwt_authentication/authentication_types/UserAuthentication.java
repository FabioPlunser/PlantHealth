package at.ac.uibk.plant_health.config.jwt_authentication.authentication_types;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthentication extends TokenAuthentication {
	private String username;

	public UserAuthentication(UUID token, String username) {
		super(token);
		this.username = username;
	}
}

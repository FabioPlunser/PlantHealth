package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TokenResponse extends RestResponse implements Serializable {
	private final UUID token;

	public TokenResponse(UUID token) {
		this.token = token;
	}
}

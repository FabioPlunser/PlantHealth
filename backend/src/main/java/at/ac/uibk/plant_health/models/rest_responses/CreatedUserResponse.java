package at.ac.uibk.plant_health.models.rest_responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.models.user.Authenticable;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CreatedUserResponse extends RestResponse implements Serializable {
	private final UUID id;
	private final String username;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final UUID token;
	private final Set<GrantedAuthority> permissions;

	public CreatedUserResponse(Authenticable authenticable) {
		this.id = authenticable.getId();
		this.username = authenticable.getUsername();
		this.token = authenticable.getToken();
		this.permissions = authenticable.getPermissions();
	}

	// region Builder Customization
	public abstract static class CreatedUserResponseBuilder<
			C extends CreatedUserResponse, B extends CreatedUserResponseBuilder<C, B>>
			extends RestResponseBuilder<C, B> {
		public CreatedUserResponseBuilder<C, B> person(Person person) {
			this.id = person.getPersonId();
			this.username = person.getUsername();
			this.token = person.getToken();
			this.permissions = person.getPermissions();
			return this;
		}
	}
	// endregion
}

package at.ac.uibk.plant_health.config.jwt_authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.TokenAuthentication;
import at.ac.uibk.plant_health.models.IdentifiedEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper Class for accessing security Information contained in the current
 * request.
 *
 * @author David Rieser
 * @version 1.1
 */
// All your constructors are belong to us!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthContext {
	/**
	 * Helper Method for accessing the current Authentication Token.
	 * Currently, the only supported AuthenticationToken is a
	 * UsernamePasswordAuthenticationToken.
	 *
	 * @return The AuthenticationToken sent with the current request (if a
	 *     valid one was sent).
	 * @see UsernamePasswordAuthenticationToken
	 */
	public static Optional<UsernamePasswordAuthenticationToken> getAuthentication() {
		// Get the Authentication set by the FilterChain.
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// Ensure that the Authentication succeeded by ensuring it is not
		// null and that the Authentication is not anonymous.
		boolean isAuthenticated = authentication instanceof UsernamePasswordAuthenticationToken;

		if (!isAuthenticated) return Optional.empty();

		return Optional.of((UsernamePasswordAuthenticationToken) authentication);
	}

	public static Optional<Object> getPrincipal() {
		return getAuthentication().map(Authentication::getPrincipal);
	}

	public static Optional<IdentifiedEntity> getIdentifiedPrincipal() {
		Optional<Object> principal = getPrincipal();

		if (principal.isPresent() && principal.get() instanceof IdentifiedEntity) {
			return principal.map(i -> (IdentifiedEntity) i);
		}

		return Optional.empty();
	}

	protected static Optional<UUID> getToken() {
		Optional<Object> details = getAuthentication().map(Authentication::getDetails);

		if (details.isPresent() && details.get() instanceof TokenAuthentication t) {
			return Optional.ofNullable(t.getToken());
		}

		return Optional.empty();
	}
}

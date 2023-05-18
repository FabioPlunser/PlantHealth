package at.ac.uibk.plant_health.config.jwt_authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.TokenAuthentication;
import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.TokenExpiredException;
import at.ac.uibk.plant_health.models.user.Authenticable;
import at.ac.uibk.plant_health.service.LoginService;

/**
 * Class responsible for checking that the Session Token exists for the
 * given User.
 * <br/>
 * The Token is provided by the {@link jakarta.servlet.Filter}.
 *
 * @author David Rieser
 * @see AbstractUserDetailsAuthenticationProvider
 * @see
 *     org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
 */
@Component
public class JwtTokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	@Autowired
	private LoginService loginService;

	@Value("${swa.token.expiration-duration:1h}")
	private Duration tokenExpirationDuration;

	@Override
	protected void additionalAuthenticationChecks(
			UserDetails userDetails,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
	) {
		// All Checks are done in retrieveUser
	}

	@Override
	protected UserDetails retrieveUser(
			String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
	) {
		Assert.isInstanceOf(
				RequestInfo.class, usernamePasswordAuthenticationToken,
				()
						-> this.messages.getMessage(
								"JwtTokenAuthenticationProvider.onlySupports",
								"Only RequestInfo is supported"
						)
		);

		RequestInfo info = (RequestInfo) usernamePasswordAuthenticationToken;
		TokenAuthentication tokenAuthentication = info.getToken();

		// Try to find the User with the given Session Token
		Optional<? extends UserDetails> maybeUser = loginService.login(tokenAuthentication);

		if (maybeUser.isEmpty()) {
			throw new InsufficientAuthenticationException(
					String.format("Could not find Id %s", tokenAuthentication.getToken())
			);
		}

		UserDetails user = maybeUser.get();

		if (user instanceof Authenticable authenticable) {
			checkTokenExpired(authenticable);
			return authenticable;
		} else if (user instanceof AccessPoint accessPoint) {
			if (!accessPoint.isUnlocked()) {
				throw new InsufficientAuthenticationException(String.format(
						"AccessPoint %s is not unlocked", tokenAuthentication.getToken()
				));
			}
			return accessPoint;
		} else if (user instanceof SensorStation sensorStation) {
			if (!sensorStation.isUnlocked()) {
				throw new InsufficientAuthenticationException(String.format(
						"AccessPoint %s is not unlocked", tokenAuthentication.getToken()
				));
			}
			return sensorStation;
		}

		throw new BadCredentialsException(formatTokenError(tokenAuthentication.getToken()));
	}

	private static String formatTokenError(UUID token) {
		return String.format("Cannot find user with authentication token: <%s>", token.toString());
	}

	private void checkTokenExpired(Authenticable authenticable) throws TokenExpiredException {
		LocalDateTime expirationDate =
				(LocalDateTime) tokenExpirationDuration.addTo(authenticable.getTokenCreationDate());
		if (expirationDate.isBefore(LocalDateTime.now()))
			throw new TokenExpiredException(String.format("Token expired at %s", expirationDate));
	}
}

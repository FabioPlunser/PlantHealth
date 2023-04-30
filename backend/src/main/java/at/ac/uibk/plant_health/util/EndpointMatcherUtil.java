package at.ac.uibk.plant_health.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import at.ac.uibk.plant_health.models.user.Permission;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Helper Class for keeping track of the Endpoints and their respective required {@link
 * Permission}s.
 *
 * @author David Rieser
 */
@Component
public class EndpointMatcherUtil {
	// region Constants and @Value-Injected Properties
	// region Base Routes
	@Getter
	@Value(API_BASE_VALUE)
	private String apiBaseRoute;
	@Getter
	@Value(ADMIN_BASE_VALUE)
	private String adminBaseRoute;
	@Getter
	@Value(ERROR_BASE_VALUE)
	private String errorBaseRoute;
	// endregion

	// region Public API Endpoints
	private static final String API_BASE_VALUE = "${swa.api.base:/api}";
	private static final String ADMIN_BASE_VALUE = "${swa.admin.base:/admin}";
	private static final String ERROR_BASE_VALUE = "${swa.error.base:/error}";

	public static final String LOGIN_ENDPOINT = "/login";
	public static final String LOGOUT_ENDPOINT = "/logout";
	public static final String REGISTER_ENDPOINT = "/register";

	@Getter
	@Value(LOGIN_ENDPOINT)
	private String apiLoginEndpoint;
	@Getter
	@Value(LOGOUT_ENDPOINT)
	private String apiLogoutEndpoint;
	@Getter
	@Value(REGISTER_ENDPOINT)
	private String apiRegisterEndpoint;
	// endregion

	// region Error Endpoints
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ErrorEndpoints {
		public static final String TOKEN_EXPIRED_ERROR_ENDPOINT = "/token-expired";
		public static final String AUTHENTICATION_ERROR_ENDPOINT = "/unauthorized";
		public static final String AUTHORIZATION_ERROR_ENDPOINT = "/forbidden";
		public static final String NOT_FOUND_ERROR_ENDPOINT = "/notFound";
		public static final String ERROR_ENDPOINT = "/error";
	}

	private final String[] errorEndpoints =
			// Get all Error Routes defined in this Class using Runtime Reflection
			Arrays.stream(ErrorEndpoints.class.getDeclaredFields())
					// Only get static Fields of type <String> which contain the Error
					// Endpoints.
					.filter(ReflectionUtil.isAssignableFromPredicate(String.class))
					.filter(ReflectionUtil::isStaticField)
					// Get the Endpoints
					.map(ReflectionUtil::<String>getStaticFieldValueTyped)
					.toArray(String[] ::new);
	// endregion
	// endregion

	public String toApiEndpoint(String route) {
		return String.format("%s/%s", this.apiBaseRoute, route);
	}

	public String toAdminEndpoint(String route) {
		return String.format("%s/%s", this.adminBaseRoute, route);
	}
}

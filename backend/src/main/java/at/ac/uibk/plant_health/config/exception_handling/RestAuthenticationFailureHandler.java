package at.ac.uibk.plant_health.config.exception_handling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import at.ac.uibk.plant_health.controllers.error_controllers.SwaErrorController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handler for {@link AuthenticationException}s inside the filters of the
 * Security Chain.
 *
 * @author David Rieser
 * @see AuthenticationException
 * @see AuthenticationFailureHandler
 */
@Component
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
	@Autowired
	private SwaErrorController errorController;

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		errorController.handleErrorManual(request, response, authException);
	}
}

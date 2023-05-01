package at.ac.uibk.plant_health.controllers.error_controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

import at.ac.uibk.plant_health.models.exceptions.TokenExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller Advice for catching Exceptions and sending the appropriate
 * Responses.
 *
 * @author David Rieser
 */
@RestControllerAdvice
@SuppressWarnings("unused")
public class SwaExceptionHandlerController extends ResponseEntityExceptionHandler {
	@Autowired
	private SwaErrorController errorController;

	@ExceptionHandler(TokenExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public void handleTokenExpiredException(
			HttpServletRequest request, HttpServletResponse response,
			TokenExpiredException authException
	) throws IOException {
		errorController.handleErrorManual(request, response, authException);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public void handleAuthenticationException(
			HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		errorController.handleErrorManual(request, response, authException);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public void handleAccessDeniedException(
			HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		errorController.handleErrorManual(request, response, accessDeniedException);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleException(
			HttpServletRequest request, HttpServletResponse response, Exception exception
	) throws IOException {
		errorController.handleErrorManual(request, response, exception);
	}
}

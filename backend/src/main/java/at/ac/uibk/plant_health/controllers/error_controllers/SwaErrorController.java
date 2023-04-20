package at.ac.uibk.plant_health.controllers.error_controllers;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import static at.ac.uibk.plant_health.util.EndpointMatcherUtil.ErrorEndpoints.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.io.PrintWriter;

import at.ac.uibk.plant_health.models.annotations.PublicEndpoint;
import at.ac.uibk.plant_health.models.exceptions.TokenExpiredException;
import at.ac.uibk.plant_health.models.rest_responses.MessageResponse;
import at.ac.uibk.plant_health.models.rest_responses.RedirectResponse;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.util.EndpointMatcherUtil;
import at.ac.uibk.plant_health.util.SerializationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller for sending Error Responses.
 *
 * @author David Rieser
 */
@Controller
@SuppressWarnings("unused")
public class SwaErrorController implements ErrorController {
	@Autowired
	private EndpointMatcherUtil endpointMatcherUtil;

	private RedirectResponse generateRedirectFromException(Exception exception) {
		return generateRedirectFromException(404, exception);
	}

	private RedirectResponse generateRedirectFromException(int status, Exception exception) {
		return RedirectResponse.builder()
				.redirectLocation(String.format(
						"%s?status=%d&header=%s&message=%s",
						endpointMatcherUtil.getErrorBaseRoute(), status,
						exception.getClass().getSimpleName(), exception.getMessage()
				))
				.build();
	}

	@ResponseBody
	@ReadOperation
	@PublicEndpoint
	@RequestMapping(value = AUTHENTICATION_ERROR_ENDPOINT, method = {GET, POST, PUT, PATCH, DELETE})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public RestResponseEntity handleAuthenticationError(
			HttpServletRequest request, HttpServletResponse response,
			AuthenticationException accessDeniedException
	) {
		return MessageResponse.builder()
				.message("Authentication failed!")
				.statusCode(HttpStatus.UNAUTHORIZED)
				.toEntity();
	}

	@ResponseBody
	@ReadOperation
	@PublicEndpoint
	@RequestMapping(value = TOKEN_EXPIRED_ERROR_ENDPOINT, method = {GET, POST, PUT, PATCH, DELETE})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public RestResponseEntity handleTokenExpiredError(
			HttpServletRequest request, HttpServletResponse response,
			TokenExpiredException tokenExpiredException
	) {
		return MessageResponse.builder()
				.message(tokenExpiredException.getMessage())
				.statusCode(HttpStatus.UNAUTHORIZED)
				.toEntity();
	}

	@ResponseBody
	@ReadOperation
	@PublicEndpoint
	@RequestMapping(value = AUTHORIZATION_ERROR_ENDPOINT, method = {GET, POST, PUT, PATCH, DELETE})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public RestResponseEntity handleAuthorizationError(
			HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) {
		return MessageResponse.builder()
				.message("Insufficient Privileges!")
				.statusCode(HttpStatus.FORBIDDEN)
				.toEntity();
	}

	@ResponseBody
	@ReadOperation
	@PublicEndpoint
	@RequestMapping(value = NOT_FOUND_ERROR_ENDPOINT, method = {GET, POST, PUT, PATCH, DELETE})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public RestResponseEntity handleNotFoundError(
			HttpServletRequest request, HttpServletResponse response
	) {
		return MessageResponse.builder()
				.message("Endpoint not found!")
				.statusCode(HttpStatus.NOT_FOUND)
				.toEntity();
	}

	@ResponseBody
	@ReadOperation
	@PublicEndpoint
	@RequestMapping(value = ERROR_ENDPOINT, method = {GET, POST, PUT, PATCH, DELETE})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public RestResponseEntity handleError(
			HttpServletRequest request, HttpServletResponse response, Exception exception
	) {
		return MessageResponse.builder()
				.internalError()
				.statusCode(HttpStatus.UNAUTHORIZED)
				.toEntity();
	}

	public void handleErrorManual(
			HttpServletRequest request, HttpServletResponse response, Exception exception
	) throws IOException {
		RestResponseEntity responseEntity;

		if (exception instanceof AuthenticationException authenticationException) {
			if (exception instanceof TokenExpiredException tokenExpiredException) {
				responseEntity = handleTokenExpiredError(request, response, tokenExpiredException);
			} else {
				responseEntity =
						handleAuthenticationError(request, response, authenticationException);
			}
		} else if (exception instanceof AccessDeniedException accessDeniedException) {
			responseEntity = handleAuthorizationError(request, response, accessDeniedException);
		} else {
			responseEntity = handleError(request, response, exception);
		}

		try {
			// Set the Response Status to the stored one
			response.setStatus(responseEntity.getStatusCode().value());
			// Add any custom Headers from the entity
			responseEntity.getHeaders().forEach(
					(name, values) -> values.forEach(value -> response.addHeader(name, value))
			);
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

			// Write the Body of the Request
			String responseBody = SerializationUtil.serializeJSON(responseEntity.getBody());
			if (responseBody != null) {
				try (PrintWriter writer = response.getWriter()) {
					writer.write(responseBody);
				}
			}
		} catch (JsonProcessingException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}

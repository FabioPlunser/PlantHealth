package at.ac.uibk.plant_health.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.UserAuthentication;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper Class for Conversion Method that should have slightly modified
 * behaviour.
 *
 * @author David Rieser
 */
// All your Constructors are belong to us!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionUtil {
	/**
	 * Helper Method for converting a String into a UUID without throwing an
	 * Exception.
	 *
	 * @param maybeToken The String that should be converted into a Token.
	 * @return The Token if it could be converted, null otherwise.
	 */
	public static UUID tryConvertUUID(String maybeToken) {
		try {
			return UUID.fromString(maybeToken);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper Method for parsing a JSON Web Token from a String.
	 *
	 * @param input The input to parse into a {@link UserAuthentication}.
	 * @return The parsed {@link UserAuthentication}, or empty if the parsing failed.
	 */
	public static Optional<UserAuthentication> tryConvertJwtTokenOptional(String input) {
		return Optional.ofNullable(tryConvertJwtToken(input));
	}

	public static <T> T tryConvertJson(String input, Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(input, clazz);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * Helper Method for parsing a JSON Web Token from a String.
	 *
	 * @param input The input to parse into a {@link UserAuthentication}.
	 * @return The parsed {@link UserAuthentication}, or null if the parsing failed.
	 */
	public static UserAuthentication tryConvertJwtToken(String input) {
		try {
			return new ObjectMapper().readValue(input, UserAuthentication.class);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * Helper Method for converting a String into a UUID without throwing an
	 * Exception.
	 *
	 * @param maybeToken The String that should be converted into a Token.
	 * @return The Token if it could be converted, an empty Optional
	 *     otherwise.
	 */
	public static Optional<UUID> tryConvertUUIDOptional(String maybeToken) {
		return Optional.ofNullable(tryConvertUUID(maybeToken));
	}
}

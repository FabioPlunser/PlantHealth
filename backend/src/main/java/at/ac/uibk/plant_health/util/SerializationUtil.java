package at.ac.uibk.plant_health.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// All your Constructors are belong to us!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SerializationUtil {
	/**
	 * Convert a dynamic Object into it's String Representation using
	 * Jackson.
	 *
	 * @param o The Object to convert
	 * @return The serialized Representation of the Object (or null if an
	 *     Error occurred).
	 */
	public static String serializeJSON(Object o) {
		try {
			return new ObjectMapper()
					.enable(SerializationFeature.INDENT_OUTPUT)
					.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}

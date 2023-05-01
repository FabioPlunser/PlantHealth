package at.ac.uibk.plant_health.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.UUID;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.UserAuthentication;
import at.ac.uibk.plant_health.models.user.Person;

public class AuthGenerator {
	public static String generateToken(Person person) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(generateJwtToken(person));
	}

	public static String generateToken(String username, UUID token) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(new Person(username, "", "", token, Set.of()));
	}

	public static UserAuthentication generateJwtToken(Person person) {
		if (person.getToken() == null) throw new NullPointerException("Token of Person was null");
		return new UserAuthentication(person.getToken(), person.getUsername());
	}
}

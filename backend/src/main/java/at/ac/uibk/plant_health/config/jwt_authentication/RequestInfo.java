package at.ac.uibk.plant_health.config.jwt_authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import at.ac.uibk.plant_health.config.jwt_authentication.authentication_types.TokenAuthentication;

public class RequestInfo extends UsernamePasswordAuthenticationToken {
	public RequestInfo(String userAgent, TokenAuthentication token) {
		super(userAgent, token);
	}

	public String getUserAgent() {
		return (String) this.getPrincipal();
	}

	public TokenAuthentication getToken() {
		return (TokenAuthentication) this.getCredentials();
	}
}

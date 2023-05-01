package at.ac.uibk.plant_health.models.exceptions;

import org.springframework.security.core.AuthenticationException;

public class TokenExpiredException extends AuthenticationException {
	public TokenExpiredException() {
		super("Token Expired!");
	}

	public TokenExpiredException(String msg) {
		super(msg);
	}

	public TokenExpiredException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

package at.ac.uibk.plant_health.models.annotations.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

import at.ac.uibk.plant_health.config.jwt_authentication.AuthContext;
import at.ac.uibk.plant_health.models.annotations.PrincipalRequired;
import at.ac.uibk.plant_health.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Order(Constants.INJECTION_ASPECT_ORDER)
@Component
@Slf4j
public class PrincipalRequiredAspect {
	@Autowired
	private HttpServletRequest request;

	@Around("@annotation(at.ac.uibk.plant_health.models.annotations.PrincipalRequired)")
	public Object doSomething(ProceedingJoinPoint jp) throws Throwable {
		Class<?> requiredPrinciple = ((MethodSignature) jp.getSignature())
											 .getMethod()
											 .getAnnotation(PrincipalRequired.class)
											 .value();

		Object principle = AuthContext.getPrincipal().orElse(null);
		if (principle == null)
			throw new AccessDeniedException("No Principle was found");

		if (requiredPrinciple.isAssignableFrom(principle.getClass())) {
			Object[] args = jp.getArgs();
			boolean methodIsStatic = jp.getThis() != null;
			Class<?>[] parameters = ((MethodSignature) jp.getSignature()).getParameterTypes();

			for (int i = methodIsStatic ? 0 : 1; i < parameters.length; i++) {
				if (requiredPrinciple.isAssignableFrom(parameters[i])) {
					log.debug(
							"Replaced Param %i with Principle of Type %s", i,
							requiredPrinciple.getSimpleName()
					);
					args[i] = principle;
				}
			}

			return jp.proceed(args);
		}

		String errorMessage = String.format(
				"Required Principle %s but got %s", requiredPrinciple.getSimpleName(),
				principle.getClass()
		);

		log.warn(errorMessage);

		throw new AccessDeniedException(errorMessage);
	}
}
package at.ac.uibk.plant_health.models.annotations.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import at.ac.uibk.plant_health.config.jwt_authentication.AuthContext;
import at.ac.uibk.plant_health.models.IdentifiedEntity;
import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation;
import at.ac.uibk.plant_health.service.LogService;
import at.ac.uibk.plant_health.util.Constants;

@Aspect
@Order(Constants.LOGGING_ASPECT_ORDER)
@Component
public class AuditLogAspect {
	@Autowired
	private LogService logService;

	@Around("@annotation(at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation)")
	public Object auditLog(ProceedingJoinPoint jp) throws Throwable {
		MethodSignature sig = (MethodSignature) jp.getSignature();

		Object ret;
		String formatMessage;
		Log.LogLevel logLevel;
		Throwable exception = null;
		AuditLogAnnotation annotation = sig.getMethod().getAnnotation(AuditLogAnnotation.class);

		try {
			ret = jp.proceed();
			formatMessage = annotation.successMessage();
			logLevel = annotation.successLogLevel();
		} catch (Throwable e) {
			exception = e;
			formatMessage = annotation.errorMessage();
			logLevel = annotation.errorLogLevel();
			ret = null;
		}

		if (formatMessage != null && !formatMessage.isBlank()) {
			Optional<IdentifiedEntity> principle = AuthContext.getIdentifiedPrincipal();

			String className = principle.map(Object::getClass).map(Class::getName).orElse(null);
			String callerId = principle.map(IdentifiedEntity::getStringIdentification).orElse(null);

			String[] parameterNames = sig.getParameterNames();
			Object[] parameters = jp.getArgs();

			Stream<Pair<String, Optional<Object>>> obj = Stream.of(Pair.of("this", Optional.ofNullable(jp.getThis())));
			Stream<Pair<String, Optional<Object>>> params = IntStream.range(0, parameters.length)
								  .mapToObj(
										  i
										  -> Pair.of(
												  parameterNames[i],
												  Optional.ofNullable(parameters[i])
										  )
								  );
			Stream<Pair<String, Optional<Object>>> return_value = Stream.of(Pair.of("!", Optional.ofNullable(ret)));

			List<Pair<String, Optional<Object>>> paramMap = Stream.concat(
				Stream.concat(obj, params), return_value
			).toList();

			var message = formatMessage(formatMessage, paramMap);

			Log log = new Log(logLevel, message, className, callerId);
			logService.log(log);
		}

		if (exception != null) throw exception;
		return ret;
	}

	private String formatMessage(
			String formatMessage, List<Pair<String, Optional<Object>>> parameters
	) {
		StringBuilder builder = new StringBuilder();

		boolean inFormatString = false;
		int i = 0, l = i;
		while (i < formatMessage.length()) {
			if (formatMessage.charAt(i) == '{' && !inFormatString
				&& !(formatMessage.charAt(i - 1) == '\\')) {
				builder.append(formatMessage, l, i);
				l = i + 1;
				inFormatString = true;
			} else if (formatMessage.charAt(i) == '}' && inFormatString && !(formatMessage.charAt(i - 1) == '\\')) {
				builder.append(getSubstitution(formatMessage.substring(l, i), parameters));
				l = i + 1;
				inFormatString = false;
			}
			i++;
		}

		if (!inFormatString) {
			builder.append(formatMessage, l, i);
		}

		return builder.toString();
	}

	private String getSubstitution(
			String formatString, List<Pair<String, Optional<Object>>> parameters
	) {
		List<String> props = Arrays.stream(formatString.split("\\.")).toList();
		Optional<Object> val = parameters.stream()
									   .filter(p -> p.getFirst().equals(props.get(0)))
									   .findFirst()
									   .flatMap(Pair::getSecond);

		if (val.isPresent()) {
			Object obj = val.get();

			for (String prop : skipFirst(props)) {
				Field field = findField(obj.getClass(), prop);
				if (field == null) {
					obj = "";
					break;
				}
				try {
					boolean isPrivate = Modifier.isPrivate(field.getModifiers());
					if (isPrivate) field.setAccessible(true);
					obj = field.get(obj);
					if (isPrivate) field.setAccessible(false);
				} catch (IllegalAccessException e) {
					obj = "";
					break;
				}
			}

			return obj.toString();
		}

		return "";
	}

	private Field findField(Class<?> clazz, String fieldName) {
		while (clazz != null) {
			try {
				// Check if Field is declared on the current Class.
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				/* If the Field could not be found on the current Class, try the Superclass. */
			}
			clazz = clazz.getSuperclass();
		}

		// Until no more Superclasses remain
		return null;
	}

	// See: https://stackoverflow.com/a/5711332
	public static <T> Iterable<T> skipFirst(final Iterable<T> c) {
		return () -> {
			Iterator<T> i = c.iterator();
			i.next();
			return i;
		};
	}
}

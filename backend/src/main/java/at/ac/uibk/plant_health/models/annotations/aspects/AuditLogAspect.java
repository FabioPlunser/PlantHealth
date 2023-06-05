package at.ac.uibk.plant_health.models.annotations.aspects;

import at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import at.ac.uibk.plant_health.config.jwt_authentication.AuthContext;
import at.ac.uibk.plant_health.models.IdentifiedEntity;
import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.service.LogService;
import at.ac.uibk.plant_health.util.Constants;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Order(Constants.LOGGING_ASPECT_ORDER)
@Component
public class AuditLogAspect {
	@Autowired
	private LogService logService;

	@Autowired
	private HttpServletRequest request;

	@Around("@annotation(at.ac.uibk.plant_health.models.annotations.AuditLogAnnotation)")
	public Object auditLog(ProceedingJoinPoint jp) throws Throwable {
		MethodSignature sig = (MethodSignature) jp.getSignature();

		Object ret;
		String formatMessage;
		Log.LogLevel logLevel;
		AuditLogAnnotation annotation = sig
				.getMethod()
				.getAnnotation(AuditLogAnnotation.class);

		try {
			ret = jp.proceed();
			formatMessage = annotation.successMessage();
			logLevel = annotation.successLogLevel();
		} catch (Exception e) {
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

			List<Pair<String, Object>> paramMap = IntStream.range(0, parameters.length)
					.mapToObj(i -> Pair.of(parameterNames[i], parameters[i]))
					.toList();

			var message = formatMessage(formatMessage, paramMap);

			Log log = new Log(logLevel, message, className, callerId);
			logService.log(log);
		}

		return ret;
	}

	private String formatMessage(String formatMessage, List<Pair<String, Object>> parameters) {
		StringBuilder builder = new StringBuilder();

		boolean inFormatString = false;
		int i = 0, l = i;
		while (i < formatMessage.length()) {
			if (formatMessage.charAt(i) == '{' && !inFormatString && !(formatMessage.charAt(i - 1) == '\\')) {
				builder.append(formatMessage.substring(l, i));
				l = i + 1;
				inFormatString = true;
			} else if (formatMessage.charAt(i) == '}' && inFormatString && !(formatMessage.charAt(i - 1) == '\\')) {
				builder.append(getSubstitution(formatMessage.substring(l, i), parameters));
				l = i + 1;
				inFormatString = false;
			}
			i ++;
		}

		if (!inFormatString) {
			builder.append(formatMessage.substring(l, i));
		}

		String message = builder.toString();

		return message;
	}

	private String getSubstitution(String formatString, List<Pair<String, Object>> parameters) {
		List<String> props = Arrays.stream(formatString.split("\\.")).toList();
		Optional<Object> val = parameters.stream()
				.filter(p -> p.getFirst().equals(props.get(0)))
				.findFirst()
				.map(Pair::getSecond);

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
					if (isPrivate) {
						field.setAccessible(true);
					}
					obj = field.get(obj);
					field.setAccessible(false);
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
				var field = clazz.getDeclaredField(fieldName);
				return field;
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
		return new Iterable<T>() {
			@Override public Iterator<T> iterator() {
				Iterator<T> i = c.iterator();
				i.next();
				return i;
			}
		};
	}
}

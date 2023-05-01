package at.ac.uibk.plant_health.models.annotations.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

	@Around("execution(* (@org.springframework.web.bind.annotation.RestController *).*(..))")
	public Object logEndpoints(ProceedingJoinPoint jp) throws Throwable {
		Optional<IdentifiedEntity> principle = AuthContext.getIdentifiedPrincipal();

		String className;
		String callerId;

		if (principle.isPresent()) {
			className = principle.get().getClass().getName();
			callerId = principle.map(IdentifiedEntity::getStringIdentification).orElse(null);
		} else {
			className = null;
			callerId = null;
		}

		String endpoint = jp.getSignature().getName();
		String controller = jp.getTarget().getClass().getSimpleName();
		String message = null;
		Throwable ex = null;

		Object ret;
		try {
			ret = jp.proceed();
			if (ret instanceof RestResponseEntity e) {
				HttpStatusCode code = e.getStatusCode();
				if (!(code.is2xxSuccessful() || code.is3xxRedirection())) {
					message = "Error in Endpoint " + controller + "." + endpoint;
				}
			}
			if (message == null) {
				message = "Successful Call of Endpoint " + controller + "." + endpoint;
			}
		} catch (Throwable t) {
			ex = t;
			message = "Error in Endpoint " + controller + "." + endpoint;
			ret = null;
		}

		Log log = new Log(Log.LogLevel.INFO, message, className, callerId);
		logService.log(log);

		if (ex != null) throw ex;
		return ret;
	}
}

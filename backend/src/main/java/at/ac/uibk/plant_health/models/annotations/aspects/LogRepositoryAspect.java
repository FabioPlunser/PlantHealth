package at.ac.uibk.plant_health.models.annotations.aspects;

import at.ac.uibk.plant_health.config.jwt_authentication.AuthContext;
import at.ac.uibk.plant_health.models.IdentifiedEntity;
import at.ac.uibk.plant_health.models.Log;
import at.ac.uibk.plant_health.models.rest_responses.RestResponseEntity;
import at.ac.uibk.plant_health.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class LogRepositoryAspect {
    @Autowired
    private LogService logService;

    @Autowired
    private HttpServletRequest request;

    @Around("execution(* (@org.springframework.web.bind.annotation.RestController *).*(..))")
    public Object logRepositories(ProceedingJoinPoint jp) throws Throwable {
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

        String query = jp.getSignature().getName();
        String repository = jp.getTarget().getClass().getSimpleName();
        String message = null;
        Throwable ex = null;

        Object ret;
        try {
            ret = jp.proceed();
            message = "Successfully called " + repository + "." + query;
        } catch (Throwable t) {
            ex = t;
            message = "Error in Repository Method " + repository + "." + query;
            ret = null;
        }

        Log log = new Log(Log.LogLevel.INFO, message, className, callerId);
        logService.log(log);

        if (ex != null) throw ex;
        return ret;
    }
}

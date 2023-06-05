package at.ac.uibk.plant_health.models.annotations;

import at.ac.uibk.plant_health.models.Log;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogAnnotation {
    String successMessage() default "";
    Log.LogLevel successLogLevel() default Log.LogLevel.INFO;
    String errorMessage() default "";
    Log.LogLevel errorLogLevel() default Log.LogLevel.ERROR;
}

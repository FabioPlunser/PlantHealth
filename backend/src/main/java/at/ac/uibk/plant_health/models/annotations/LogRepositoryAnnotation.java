package at.ac.uibk.plant_health.models.annotations;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRepositoryAnnotation {
}

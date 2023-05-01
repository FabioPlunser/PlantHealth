package at.ac.uibk.plant_health.models.annotations;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrincipalRequired {
	Class<?> value();
}

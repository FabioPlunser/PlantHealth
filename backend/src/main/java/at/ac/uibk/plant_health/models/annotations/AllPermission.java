package at.ac.uibk.plant_health.models.annotations;

import java.lang.annotation.*;

import at.ac.uibk.plant_health.models.user.Permission;

@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllPermission {
	Permission[] value();
}

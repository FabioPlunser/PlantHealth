package at.ac.uibk.plant_health.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Helper Class for the {@link EndpointMatcherUtil}'s Reflection based Routes.
 *
 * @author David Rieser
 */
// All your Constructors are belong to us!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtil {
	/**
	 * Check if the given field is static.
	 *
	 * @param field The Runtime Field
	 * @return true if the field is static, false otherwise.
	 */
	public static boolean isStaticField(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	/**
	 * Get the value of a static Field.
	 *
	 * @param field The Runtime Field
	 * @return The value of the field (or null if the field is not static).
	 */
	@SneakyThrows
	public static Object getStaticFieldValue(Field field) {
		return isStaticField(field) ? field.get(null) : null;
	}

	/**
	 * Get the value of a static Field as the given type.
	 *
	 * @implNote This method does no type checking, so please ensure that the types can be
	 * converted or catch the arising Exception.
	 * @param field The Runtime Field
	 * @return The value of the field (or null if the field is not static).
	 */
	@SneakyThrows
	public static <T> T getStaticFieldValueTyped(Field field) {
		return (T) getStaticFieldValue(field);
	}

	/**
	 * Check if the given field can be assigned to a Variable of the given type.
	 *
	 * @param field The Runtime Field
	 * @param clazz The class of the variable are trying to assign the field to.
	 * @return true if the variable can be assigned from the given field, false otherwise.
	 */
	public static boolean isAssignableFrom(Field field, Class<?> clazz) {
		return field.getType().isAssignableFrom(clazz);
	}

	/**
	 * Creates a Predicate that checks if the given field can be assigned to a Variable of the
	 * given type.
	 *
	 * @param clazz The class of the variable are trying to assign the field to.
	 * @return A Predicate that returns true if the variable can be assigned from the given
	 *         field, false otherwise.
	 */
	public static Predicate<Field> isAssignableFromPredicate(Class<?> clazz) {
		return field -> field.getType().isAssignableFrom(clazz);
	}
}

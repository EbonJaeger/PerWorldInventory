package me.gnat008.perworldinventory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static java.lang.String.format;

/**
 * Offers reflection functionality to set up tests. Use only when absolutely necessary.
 */
public final class ReflectionTestUtils {

    private ReflectionTestUtils() {}

    public static <T> Field getField(Class<?> clazz, T instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            throw new UnsupportedOperationException(format("Could not get field '%s' for instance '%s' of class '%s'",
                    fieldName, instance, clazz.getName()), ex);
        }
    }

    /**
     * Gets a field's value.
     *
     * @param clazz the class on which the field is declared
     * @param fieldName the field name
     * @param instance the instance to get it from (null for static fields)
     * @param <V> the value's type
     * @param <T> the instance's type
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    public static <V, T> V getFieldValue(Class<? super T> clazz, T instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            // Avoid forcing user to cast
            return (V) field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not get field '" + fieldName + "' from " + instance, e);
        }
    }

    /**
     * Sets the field of the given class.
     *
     * @param clazz the class on which the field is declared
     * @param fieldName the field name
     * @param instance the instance to set the field on (null for static fields)
     * @param value the value to set
     * @param <T> the instance's type
     */
    public static <T> void setField(Class<? super T> clazz, String fieldName, T instance, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not set field '" + fieldName + "' on " + instance, e);
        }
    }

    /**
     * Check that a class only has a hidden, zero-argument constructor, preventing the
     * instantiation of such classes (utility classes). Invokes the hidden constructor
     * as to register the code coverage.
     *
     * @param clazz The class to validate
     */
    public static void validateHasOnlyPrivateEmptyConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length > 1) {
            throw new IllegalStateException("Class " + clazz.getSimpleName() + " has more than one constructor");
        } else if (constructors[0].getParameterTypes().length != 0) {
            throw new IllegalStateException("Constructor of " + clazz + " does not have empty parameter list");
        } else if (!Modifier.isPrivate(constructors[0].getModifiers())) {
            throw new IllegalStateException("Constructor of " + clazz + " is not private");
        }

        // Ugly hack to get coverage on the private constructors
        // http://stackoverflow.com/questions/14077842/how-to-test-a-private-constructor-in-java-application
        try {
            constructors[0].setAccessible(true);
            constructors[0].newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}

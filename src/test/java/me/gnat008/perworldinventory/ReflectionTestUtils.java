package me.gnat008.perworldinventory;

import java.lang.reflect.Field;

import static java.lang.String.format;

/**
 *
 */
public final class ReflectionTestUtils {

    /**
     * Set the field of a given object to a new value with reflection.
     *
     * @param clazz The class of the object.
     * @param instance The instance to modify (null for static fields).
     * @param fieldName The name of the field to modify.
     * @param value The value to give the field.
     */
    public static <T> void setField(Class<? super T> clazz, T instance, String fieldName, Object value) {
        try {
            Field field = getField(clazz, instance, fieldName);
            field.set(instance, value);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException(format("Could not set field '%s' for instance '%s' of class '%s'.",
                    fieldName, instance, clazz.getName()), ex);
        }
    }

    private static <T> Field getField(Class<T> clazz, T instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            throw new UnsupportedOperationException(format("Could not get field '%s' for instance '%s' of class '%s'.",
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
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new UnsupportedOperationException(format("Could not get field '%s' for instance '%s' of class '%s'.",
                    fieldName, instance, clazz.getName()), ex);
        }
    }
}

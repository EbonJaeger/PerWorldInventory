package me.gnat008.perworldinventory;

import java.lang.reflect.Field;

import static java.lang.String.format;

/**
 * Offers reflection functionality to set up tests. Use only when absolutely necessary.
 */
public final class ReflectionTestUtils {

    private ReflectionTestUtils() {}

    @SuppressWarnings("unchecked")
    public static <T, V> V getFieldValue(Class<?> clazz, T instance, String fieldName) {
        Field field = getField(clazz, instance, fieldName);
        try {
            return (V) field.get(instance);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Could not get value of field '" + fieldName + "'", ex);
        }
    }

    private static <T> Field getField(Class<?> clazz, T instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            throw new UnsupportedOperationException(format("Could not get field '%s' for instance '%s' of class '%s'",
                    fieldName, instance, clazz.getName()), ex);
        }
    }
}

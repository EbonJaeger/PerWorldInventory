package me.gnat008.perworldinventory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * Test utilities.
 */
public final class TestHelper {

    public static final String SOURCES_FOLDER = "src/main/java/";
    public static final String PROJECT_ROOT = "/me/gnat008/perworldinventory/";

    private TestHelper() {
    }

    /**
     * Return a {@link File} to a file in the JAR's resources (main or test).
     *
     * @param path The absolute path to the file
     * @return The project file
     */
    public static File getJarFile(String path) {
        URI uri = getUriOrThrow(path);
        return new File(uri.getPath());
    }

    /**
     * Return a {@link Path} to a file in the JAR's resources (main or test).
     *
     * @param path The absolute path to the file
     * @return The Path object to the file
     */
    public static Path getJarPath(String path) {
        String sqlFilePath = getUriOrThrow(path).getPath();
        // Windows preprends the path with a '/' or '\', which Paths cannot handle
        String appropriatePath = System.getProperty("os.name").contains("indow")
                ? sqlFilePath.substring(1)
                : sqlFilePath;
        return Paths.get(appropriatePath);
    }

    private static URI getUriOrThrow(String path) {
        URL url = TestHelper.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("File '" + path + "' could not be loaded");
        }
        try {
            return new URI(url.toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("File '" + path + "' cannot be converted to a URI");
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
    public static <V, T> V getField(Class<? super T> clazz, String fieldName, T instance) {
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

    public static Logger initMockLogger() {
        Logger logger = mock(Logger.class);
        PwiLogger.setLogger(logger);
        return logger;
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

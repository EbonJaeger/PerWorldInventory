package me.gnat008.perworldinventory;

import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * Test utilities.
 */
public final class TestHelper {

    public static final String SOURCES_FOLDER = "src/main/java/";
    public static final String PROJECT_ROOT = "/me/gnat008/perworldinventory/";
    public static final UUID TESTING_UUID = UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980");

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

    public static Logger initMockLogger() {
        Logger logger = mock(Logger.class);
        PwiLogger.setLogger(logger);
        return logger;
    }

    /**
     * Get a group for testing.
     *
     * @param name The name of the group.
     * @return The created Group.
     */
    public static Group mockGroup(String name) {
        Set<String> worlds = new HashSet<>();
        worlds.add(name);

        return mockGroup(name, worlds);
    }

    public static Group mockGroup(String name, Collection<String> worlds) {
        return mockGroup(name, worlds, GameMode.SURVIVAL);
    }

    public static Group mockGroup(String name, Collection<String> worlds, GameMode gameMode) {
        Set<String> worldSet = new HashSet<>();
        worldSet.addAll(worlds);

        return new Group(name, worldSet, gameMode);
    }
}

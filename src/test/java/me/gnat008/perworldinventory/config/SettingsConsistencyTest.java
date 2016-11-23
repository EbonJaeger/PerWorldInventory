package me.gnat008.perworldinventory.config;

import com.github.authme.configme.SettingsHolder;
import com.github.authme.configme.knownproperties.ConfigurationData;
import com.github.authme.configme.properties.Property;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import me.gnat008.perworldinventory.ClassCollector;
import me.gnat008.perworldinventory.ReflectionTestUtils;
import me.gnat008.perworldinventory.TestHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.gnat008.perworldinventory.TestHelper.getJarFile;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests that the config.yml file corresponds with the settings holder classes in the code.
 */
public class SettingsConsistencyTest {

    /** Bukkit's FileConfiguration#getKeys returns all inner nodes also. We want to exclude those in tests. */
    private static final List<String> YAML_INNER_NODES = ImmutableList.of("player", "player.stats", "database");

    private final ConfigurationData configData = SettingsRetriever.buildConfigurationData();
    private final FileConfiguration ymlConfiguration = YamlConfiguration.loadConfiguration(getJarFile("/config.yml"));

    private static final String SETTINGS_FOLDER = TestHelper.PROJECT_ROOT + "config";
    private static List<Class<? extends SettingsHolder>> classes;

    @BeforeClass
    public static void scanForSettingsClasses() {
        ClassCollector collector = new ClassCollector(TestHelper.SOURCES_FOLDER, SETTINGS_FOLDER);
        classes = collector.collectClasses(SettingsHolder.class);

        if (classes.isEmpty()) {
            throw new IllegalStateException("Did not find any SettingsHolder classes. Is the folder correct?");
        }

        System.out.println("Found " + classes.size() + " SettingsHolder implementations");
    }

    @Test
    public void shouldContainAllPropertiesWithSameDefaultValue() {
        // given / when / then
        for (Property<?> property : configData.getProperties()) {
            assertThat("config.yml does not have property for " + property,
                ymlConfiguration.contains(property.getPath()), equalTo(true));
            assertThat("config.yml does not have same default value for " + property,
                property.getDefaultValue(), equalTo(ymlConfiguration.get(property.getPath())));
        }
    }

    @Test
    public void shouldNotHaveUnknownProperties() {
        // given
        Set<String> keysInYml = ymlConfiguration.getKeys(true);
        keysInYml.removeAll(YAML_INNER_NODES);
        Set<String> keysInCode = configData.getProperties().stream().map(Property::getPath).collect(Collectors.toSet());

        // when / then
        assertThat(Sets.difference(keysInYml, keysInCode), empty());
    }

    /**
     * Make sure that all {@link Property} instances we define are in public, static, final fields.
     */
    @Test
    public void shouldHavePublicStaticFinalFields() {
        for (Class<?> clazz : classes) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Property.class.isAssignableFrom(field.getType())) {
                    String fieldName = "Field " + clazz.getSimpleName() + "#" + field.getName();
                    assertThat(fieldName + " should be public, static and final",
                            isValidConstantField(field), equalTo(true));
                }
            }
        }
    }

    /**
     * Make sure that no properties use the same path.
     */
    @Test
    public void shouldHaveUniquePaths() {
        Set<String> paths = new HashSet<>();
        for (Class<?> clazz : classes) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Property.class.isAssignableFrom(field.getType())) {
                    Property<?> property = ReflectionTestUtils.getFieldValue(clazz, null, field.getName());
                    if (!paths.add(property.getPath())) {
                        fail("Path '" + property.getPath() + "' should be used by only one constant");
                    }
                }
            }
        }
    }

    @Test
    public void shouldHaveEmptyHiddenConstructorOnly() {
        for (Class<?> clazz : classes) {
            TestHelper.validateHasOnlyPrivateEmptyConstructor(clazz);
        }
    }

    private static boolean isValidConstantField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }
}
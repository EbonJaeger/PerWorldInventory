package me.gnat008.perworldinventory.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyResource;
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
    private static final String SETTINGS_FOLDER = TestHelper.PROJECT_ROOT + "config";

    private static ConfigurationData configData;
    private static Class<? extends SettingsHolder>[] classes;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void scanForSettingsClasses() {
        ClassCollector collector = new ClassCollector(TestHelper.SOURCES_FOLDER, SETTINGS_FOLDER);
        // TODO ConfigMe/#30: Create ConfigurationDataBuilder.collectData(Iterable<Class<...>>) to avoid unchecked array
        classes = collector.collectClasses(SettingsHolder.class).stream().toArray(Class[]::new);

        if (classes.length == 0) {
            throw new IllegalStateException("Did not find any SettingsHolder classes. Is the folder correct?");
        }

        System.out.println("Found " + classes.length + " SettingsHolder implementations");

        configData = ConfigurationDataBuilder.collectData(classes);
    }

    /**
     * Make sure that all properties in the config contain the same default value as the code.
     */
    @Test
    public void shouldContainAllPropertiesWithSameDefaultValue() {
        // given
        PropertyResource yamlResource = new YamlFileResource(getJarFile("/config.yml"));

        // when / then
        for (Property<?> property : configData.getProperties()) {
            assertThat("config.yml does not have property for " + property,
                yamlResource.contains(property.getPath()), equalTo(true));
            assertThat("config.yml does not have same default value for " + property,
                property.getDefaultValue(), equalTo(property.getValue(yamlResource)));
        }
    }

    /**
     * Make sure that there are no properties in the config.yml that are not defined
     * in the code.
     */
    @Test
    public void shouldNotHaveUnknownProperties() {
        // given
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(getJarFile("/config.yml"));
        Set<String> keysInYml = yamlConfig.getKeys(true);
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
                    Property<?> property = ReflectionTestUtils.getFieldValue(null, field);
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
            ReflectionTestUtils.validateHasOnlyPrivateEmptyConstructor(clazz);
        }
    }

    private static boolean isValidConstantField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }
}
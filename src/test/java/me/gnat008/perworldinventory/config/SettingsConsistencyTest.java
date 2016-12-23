package me.gnat008.perworldinventory.config;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.properties.Property;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.gnat008.perworldinventory.TestHelper.getJarFile;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that the config.yml file corresponds with the settings holder classes in the code.
 */
public class SettingsConsistencyTest {

    /** Bukkit's FileConfiguration#getKeys returns all inner nodes also. We want to exclude those in tests. */
    private static final List<String> YAML_INNER_NODES = ImmutableList.of("player", "player.stats");

    private final ConfigurationData configData = ConfigurationDataBuilder.collectData(PwiProperties.class);
    private final FileConfiguration ymlConfiguration = YamlConfiguration.loadConfiguration(getJarFile("/config.yml"));

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
}
package me.gnat008.perworldinventory.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Allows mocking of settings for unit tests.
 */
public class SettingsMocker {

    private FileConfiguration fileConfiguration;
    private Set<String> properties;

    private SettingsMocker() {
        fileConfiguration = mock(FileConfiguration.class);
        properties = new HashSet<>();
    }

    /**
     * Returns a new settings mocker instance.
     *
     * @return new instance
     */
    public static SettingsMocker create() {
        return new SettingsMocker();
    }

    public SettingsMocker set(String key, Object value) {
        properties.add(key);
        given(fileConfiguration.get(key)).willReturn(value);
        return this;
    }

    /**
     * Triggers a reload of the settings with the configured properties.
     */
    public void save() {
        given(fileConfiguration.getKeys(true)).willReturn(properties);
        Settings.reloadSettings(fileConfiguration);
    }
}

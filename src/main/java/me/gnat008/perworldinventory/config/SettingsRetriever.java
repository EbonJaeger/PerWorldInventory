package me.gnat008.perworldinventory.config;

import com.github.authme.configme.SettingsHolder;
import com.github.authme.configme.knownproperties.ConfigurationData;
import com.github.authme.configme.knownproperties.ConfigurationDataBuilder;
import com.github.authme.configme.properties.Property;

/**
 * Utility class for retrieving {@link Property} fields from
 * {@link SettingsHolder} implementations via
 * reflection.
 */
public final class SettingsRetriever {

    private SettingsRetriever() {}

    /**
     * Builds the configuration data for all property fields in AuthMe {@link SettingsHolder} classes.
     *
     * @return The configuration data.
     */
    public static ConfigurationData buildConfigurationData() {
        return ConfigurationDataBuilder.collectData(
                PwiProperties.class,
                DatabaseProperties.class
        );
    }
}

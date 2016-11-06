package me.gnat008.perworldinventory.config;

import com.github.authme.configme.SettingsManager;
import com.github.authme.configme.migration.PlainMigrationService;
import com.github.authme.configme.resource.YamlFileResource;

import java.io.File;

/**
 * Settings class for PWI properties.
 */
public class Settings extends SettingsManager {

    /**
     * Constructor.
     *
     * @param yamlFile the configuration file to load
     */
    public Settings(File yamlFile) {
        super(
            new YamlFileResource(yamlFile),
            new PlainMigrationService(),
            PwiProperties.class);
    }

}

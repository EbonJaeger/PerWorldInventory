package me.gnat008.perworldinventory.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.YamlFileResource;

import java.io.File;
import java.io.IOException;

import static me.gnat008.perworldinventory.util.FileUtils.createFileIfNotExists;

/**
 * Settings class for PWI properties.
 */
public class Settings extends SettingsManager {

    /**
     * Constructor.
     *
     * @param yamlFile the configuration file to load
     */
    public Settings(File yamlFile) throws IOException {
        super(
            new YamlFileResource(createFileIfNotExists(yamlFile)),
            new PlainMigrationService(),
            PwiProperties.class);
    }

}

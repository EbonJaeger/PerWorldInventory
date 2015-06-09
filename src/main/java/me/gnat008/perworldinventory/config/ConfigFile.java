/*
 * Copyright (C) 2014-2015  Gnat008
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.config;

import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    private ConfigType type;
    private File file;
    private FileConfiguration config;

    public ConfigFile(ConfigType type, File file) {
        this.type = type;
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public ConfigType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reload() {
        setDefaults(type);
    }

    public void save() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        saveConfig();
    }

    private void setDefaults(ConfigType type) {
        switch (type) {
            case CONFIG:
                for (ConfigValues defaults : ConfigValues.values()) {
                    if (!config.contains(defaults.getKey())) {
                        config.set(defaults.getKey(), defaults);
                    }
                }

                if (ConfigValues.FIRST_START.getBoolean()){
                    ConfigValues.FIRST_START.set(false);
                }

                try {
                    saveConfig();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case WORLDS:
                //TODO: Set world defaults
                break;
        }
    }

    private void saveConfig() throws IOException {
        config.save(file);
    }
}

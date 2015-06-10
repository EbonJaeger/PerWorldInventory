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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static ConfigManager instance = null;

    private Map<ConfigType, ConfigFile> configs = new HashMap<>();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    public void disable() {
        for (ConfigType level : configs.keySet()) {
            try {
                configs.get(level).save();
            } catch (IOException ex) {
                //TODO: Figure out new logging system to log this
                //plugin.getPrinter().printToConsole("Error saving " + level.toString().toLowerCase() + ".yml': " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        }
        instance = null;
    }

    public boolean getShouldSerialize(ConfigValues var) {
        return var.getBoolean();
    }

    public ConfigFile getConfig(ConfigType type) {
        return configs.get(type);
    }

    public void addConfig(ConfigType type, File file) {
        configs.put(type, new ConfigFile(type, file));
        reloadConfig(type);
    }

    public void reloadConfigs() {
        for (ConfigType type : configs.keySet()) {
            reloadConfig(type);
        }
    }

    public void reloadConfig(ConfigType type) {
        configs.get(type).reload();
    }
}

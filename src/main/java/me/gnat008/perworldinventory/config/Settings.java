/*
 * Copyright (C) 2014-2016  EbonJaguar
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

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class Settings {

    private static Map<String, Object> settings = new HashMap<>();

    private Settings() {
    }

    public static void reloadSettings(FileConfiguration config) {
        settings.clear();

        for (String key : config.getKeys(true))
            settings.put(key, config.get(key));
    }

    /**
     * Get a boolean value from the config.
     * Returns false if the key is not found.
     *
     * @param key The key to get
     * @return The value of the key
     */
    public static boolean getBoolean(String key) {
        if (settings.containsKey(key))
            return (boolean) settings.get(key);
        else
            return false;
    }

    /**
     * Get an int value from the config.
     * Returns -1 if the key is not found.
     *
     * @param key The key to get
     * @return The value of the key
     */
    public static int getInt(String key) {
        if (settings.containsKey(key))
            return (int) settings.get(key);
        else
            return -1;
    }

    /**
     * Get a String value from the config.
     * Returns null if the key is not found.
     *
     * @param key The key to get
     * @return The value of the key
     */
    public static String getString(String key) {
        if (settings.containsKey(key))
            return (String) settings.get(key);
        else
            return null;
    }
}

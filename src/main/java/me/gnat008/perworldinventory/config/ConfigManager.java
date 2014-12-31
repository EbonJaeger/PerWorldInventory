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

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.util.Printer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private PerWorldInventory plugin;
    private YamlConfiguration config;

    private static ConfigManager manager;

    private ConfigManager(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static ConfigManager getManager(PerWorldInventory plugin) {
        if (manager == null) {
            manager = new ConfigManager(plugin);
        }

        return manager;
    }

    public static void disable() {
        manager = null;
    }

    public File getConfigFile() {
        return new File(plugin.getDataFolder() + File.separator + "config.yml");
    }

    public boolean getShouldSerialize(String path) {
        return getConfig().getBoolean(path);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(getConfigFile());
        setDefaults();
        save();
        Printer.getInstance(plugin).printToConsole("Config reloaded successfully!", false);
    }

    public void save() {
        try {
            config.save(getConfigFile());
        } catch (IOException ex) {
            Printer.getInstance(plugin).printToConsole("Error saving 'config.yml'!", true);
            ex.printStackTrace();
        }
    }

    private void setDefaults() {
        addDefault("player.ender-chest", true);
        addDefault("player.inventory", true);
        addDefault("player.stats", true);
        addDefault("player-stats.can-fly", true);
        addDefault("player-stats.display-name", true);
        addDefault("player-stats.exhaustion", true);
        addDefault("player-stats.exp", true);
        addDefault("player-stats.food", true);
        addDefault("player-stats.flying", true);
        addDefault("player-stats.health", true);
        addDefault("player-stats.level", true);
        addDefault("player-stats.potion-effects", true);
        addDefault("player-stats.saturation", true);

        // Add sections for world sharing configurations
        List<String> defaults = new ArrayList<>();
        defaults.add("world");
        defaults.add("world_nether");
        defaults.add("world_the_end");
        addDefault("groups.default.worlds", defaults);
    }

    private void addDefault(String path, Object value) {
        if (!getConfig().contains(path)) {
            getConfig().set(path, value);
        }
    }

    private YamlConfiguration getConfig() {
        if (config == null) {
            reload();
        }

        return config;
    }
}

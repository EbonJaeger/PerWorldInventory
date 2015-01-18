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
    private YamlConfiguration worlds;

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

    public File getWorldsFile() {
        return new File(plugin.getDataFolder() + File.separator + "worlds.yml");
    }

    public boolean getShouldSerialize(String path) {
        return getConfig().getBoolean(path);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(getConfigFile());
        setConfigDefaults();
        saveConfig();
    }

    public void reloadWorlds() {
        worlds = YamlConfiguration.loadConfiguration(getWorldsFile());

        if (getConfig().getBoolean("first-start")) {
            setWorldsDefaults();
        }

        saveWorlds();
    }

    public void saveConfig() {
        try {
            config.save(getConfigFile());
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("Error saving 'config.yml'!", true);
            ex.printStackTrace();
        }
    }

    public void saveWorlds() {
        try {
            worlds.save(getWorldsFile());
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("Error saving 'worlds.yml'!", true);
            ex.printStackTrace();
        }
    }

    private void setConfigDefaults() {
        YamlConfiguration config = getConfig();

        addDefault(config, "first-start", true);
        addDefault(config, "player.ender-chest", true);
        addDefault(config, "player.inventory", true);
        addDefault(config, "player.stats", true);
        addDefault(config, "player-stats.can-fly", true);
        addDefault(config, "player-stats.display-name", false);
        addDefault(config, "player-stats.exhaustion", true);
        addDefault(config, "player-stats.exp", true);
        addDefault(config, "player-stats.food", true);
        addDefault(config, "player-stats.flying", true);
        addDefault(config, "player-stats.health", true);
        addDefault(config, "player-stats.level", true);
        addDefault(config, "player-stats.potion-effects", true);
        addDefault(config, "player-stats.saturation", true);
    }

    private void setWorldsDefaults() {
        YamlConfiguration worlds = getWorlds();

        List<String> defaults = new ArrayList<>();
        defaults.add("world");
        defaults.add("world_nether");
        defaults.add("world_the_end");
        addDefault(worlds, "default", defaults);
    }

    private void addDefault(YamlConfiguration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public YamlConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }

        return config;
    }

    public YamlConfiguration getWorlds() {
        if (worlds == null) {
            reloadWorlds();
        }

        return worlds;
    }
}

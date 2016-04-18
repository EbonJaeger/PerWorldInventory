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

package me.gnat008.perworldinventory.groups;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.ConfigFile;
import me.gnat008.perworldinventory.config.ConfigManager;
import me.gnat008.perworldinventory.config.ConfigType;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {

    private static GroupManager instance = null;

    private Map<String, Group> groups = new HashMap<>();

    private PerWorldInventory plugin;

    private GroupManager(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static GroupManager getInstance(PerWorldInventory plugin) {
        if (instance == null) {
            instance = new GroupManager(plugin);
        }

        return instance;
    }

    public void disable() {
        groups.clear();
        instance = null;
    }

    public void addGroup(String name, List<String> worlds) {
        addGroup(name, worlds, GameMode.SURVIVAL);
    }

    public void addGroup(String name, List<String> worlds, GameMode gamemode) {
        groups.put(name.toLowerCase(), new Group(name, worlds, gamemode));
    }

    public Group getGroup(String group) {
        return this.groups.containsKey(group.toLowerCase()) ? this.groups.get(group.toLowerCase()) : null;
    }

    public Group getGroupFromWorld(String world) {
        Group result = null;
        for (Group group : this.groups.values()) {
            if (group.containsWorld(world)) {
                result = group;
            }
        }

        return result;
    }

    public void loadGroupsToMemory() {
        groups.clear();

        FileConfiguration groupsConfig = ConfigManager.getInstance().getConfig(ConfigType.WORLDS).getConfig();
        for (String key : groupsConfig.getConfigurationSection("groups.").getKeys(false)) {
            List<String> worlds;
            if (groupsConfig.contains("groups." + key + ".worlds")) {
                worlds = groupsConfig.getStringList("groups." + key + ".worlds");
            } else {
                worlds = groupsConfig.getStringList("groups." + key);
                groupsConfig.set("groups." + key, null);
                groupsConfig.set("groups." + key + ".worlds", worlds);
                if (ConfigValues.MANAGE_GAMEMODES.getBoolean()) {
                    groupsConfig.set("groups." + key + ".default-gamemode", "SURVIVAL");
                }
            }

            if (ConfigValues.MANAGE_GAMEMODES.getBoolean()) {
                GameMode gameMode = GameMode.valueOf(groupsConfig.getString("groups." + key + ".default-gamemode").toUpperCase());
                addGroup(key, worlds, gameMode);
            } else {
                addGroup(key, worlds);
            }

            setDefaultsFile(key);
        }
    }

    public void saveGroupsToDisk() {
        ConfigFile groupsConfig = ConfigManager.getInstance().getConfig(ConfigType.WORLDS);
        FileConfiguration groupsConfigFile = groupsConfig.getConfig();
        groupsConfigFile.set("groups", null);

        for (Group group : groups.values()) {
            String groupKey = "groups." + group.getName();
            groupsConfigFile.set(groupKey, null);
            groupsConfigFile.set(groupKey + ".worlds", group.getWorlds());
            // Saving gamemode regardless of management; might be saving after convert
            groupsConfigFile.set(groupKey + ".default-gamemode", group.getGameMode().name());
        }

        try {
            groupsConfig.save();
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("Could not save the groups config to disk!", true);
            ex.printStackTrace();
        }
    }

    private void setDefaultsFile(String group) {
        File fileTo = new File(plugin.getDefaultFilesDirectory() + File.separator + group + ".json");
        if (!fileTo.exists()) {
            File fileFrom = new File(plugin.getDefaultFilesDirectory() + File.separator + "__default.json");
            plugin.copyFile(fileFrom, fileTo);
        }
    }
}

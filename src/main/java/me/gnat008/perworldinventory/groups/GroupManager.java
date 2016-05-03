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
import me.gnat008.perworldinventory.config.Settings;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {

    private Map<String, Group> groups = new HashMap<>();

    private PerWorldInventory plugin;

    public GroupManager(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public void disable() {
        groups.clear();
    }

    public void addGroup(String name, List<String> worlds) {
        addGroup(name, worlds, GameMode.SURVIVAL);
    }

    public void addGroup(String name, List<String> worlds, GameMode gamemode) {
        groups.put(name.toLowerCase(), new Group(name, worlds, gamemode));
    }

    public Group getGroup(String group) {
        return this.groups.get(group);
    }

    public Group getGroupFromWorld(String world) {
        Group result = null;
        for (Group group : this.groups.values()) {
            if (group.containsWorld(world)) {
                result = group;
            }
        }

        if (result == null) { // If true, world is unconfigured
            if (getGroup("__unconfigured__") != null) {
                result = getGroup("__unconfigured__");
                result.addWorld(world);
            } else {
                List<String> worlds = new ArrayList<>();
                worlds.add(world);
                result = new Group("__unconfigured__", worlds, GameMode.SURVIVAL);
            }
        }

        return result;
    }

    public void loadGroupsToMemory(FileConfiguration config) {
        groups.clear();

        for (String key : config.getConfigurationSection("groups.").getKeys(false)) {
            List<String> worlds;
            if (config.contains("groups." + key + ".worlds")) {
                worlds = config.getStringList("groups." + key + ".worlds");
            } else {
                worlds = config.getStringList("groups." + key);
                config.set("groups." + key, null);
                config.set("groups." + key + ".worlds", worlds);
                if (Settings.getBoolean("separate-gamemode-inventories")) {
                    config.set("groups." + key + ".default-gamemode", "SURVIVAL");
                }
            }

            if (Settings.getBoolean("manage-gamemodes")) {
                GameMode gameMode = GameMode.valueOf(config.getString("groups." + key + ".default-gamemode").toUpperCase());
                addGroup(key, worlds, gameMode);
            } else {
                addGroup(key, worlds);
            }

            setDefaultsFile(key);
        }
    }

    public void saveGroupsToDisk() {
        FileConfiguration groupsConfigFile = plugin.getWorldsConfig();
        groupsConfigFile.set("groups", null);

        for (Group group : groups.values()) {
            String groupKey = "groups." + group.getName();
            groupsConfigFile.set(groupKey, null);
            groupsConfigFile.set(groupKey + ".worlds", group.getWorlds());
            // Saving gamemode regardless of management; might be saving after convert
            groupsConfigFile.set(groupKey + ".default-gamemode", group.getGameMode().name());
        }

        try {
            groupsConfigFile.save(plugin.getDataFolder() + "worlds.yml");
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save the groups config to disk!");
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

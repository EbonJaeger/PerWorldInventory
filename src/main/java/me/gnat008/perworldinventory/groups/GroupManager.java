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
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.utils.Utils;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {

    private Map<String, Group> groups = new HashMap<>();

    @Inject
    private PerWorldInventory plugin;

    @Inject
    private Settings settings;

    GroupManager() {}

    public void disable() {
        groups.clear();
    }

    /**
     * Add a Group to memory with a default GameMode of survival.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     */
    public void addGroup(String name, List<String> worlds) {
        addGroup(name, worlds, GameMode.SURVIVAL);
    }

    /**
     * Add a Group to memory.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gamemode The default GameMode for this group.
     */
    public void addGroup(String name, List<String> worlds, GameMode gamemode) {
        PwiLogger.debug("Adding group to memory. Group: " + name + " Worlds: " + worlds.toString() + " Gamemode: " + gamemode.name());

        groups.put(name.toLowerCase(), new Group(name, worlds, gamemode, true));
    }

    /**
     * Get a group by name. This will return null if no group with the given name
     * exists.
     *
     * @param group The name of the Group.
     * @return The Group, or null.
     */
    public Group getGroup(String group) {
        return groups.get(group.toLowerCase());
    }

    /**
     * Get a group by the name of a world. This method iterates through the groups
     * and checks if each one contains the name of the given world. If no groups contain
     * the world, a new group will be created and returned.
     *
     * @param world The name of the world in the group.
     * @return The group that contains the given world.
     */
    public Group getGroupFromWorld(String world) {
        Group result = null;
        for (Group group : this.groups.values()) {
            if (group.containsWorld(world)) {
                result = group;
            }
        }

        if (result == null) { // If true, world was not defined in worlds.yml
            List<String> worlds = new ArrayList<>();
            worlds.add(world);
            worlds.add(world + "_nether");
            worlds.add(world + "_the_end");
            result = new Group(world, worlds, GameMode.SURVIVAL, false);

            groups.put(world.toLowerCase(), result);
        }

        return result;
    }

    /**
     * Loads the groups defined in a 'worlds.yml' file into memory.
     *
     * @param config The contents of the configuration file.
     */
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
                if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)) {
                    config.set("groups." + key + ".default-gamemode", "SURVIVAL");
                }
            }

            if (settings.getProperty(PwiProperties.MANAGE_GAMEMODES)) {
                GameMode gameMode = GameMode.SURVIVAL;
                if (config.getString("groups." + key + ".default-gamemode") != null) {
                    gameMode = GameMode.valueOf(config.getString("groups." + key + ".default-gamemode").toUpperCase());
                }

                addGroup(key, worlds, gameMode);
            } else {
                addGroup(key, worlds);
            }

            setDefaultsFile(key);
        }
    }

    /**
     * Clears the worlds.yml configuration file, then writes all of the groups currently in memory
     * to it.
     */
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
            groupsConfigFile.save(plugin.getDataFolder() + "/worlds.yml");
        } catch (IOException ex) {
            PwiLogger.warning("Could not save the groups config to disk:", ex);
        }
    }

    private void setDefaultsFile(String group) {
        File fileTo = new File(plugin.getDefaultFilesDirectory() + File.separator + group + ".json");
        if (!fileTo.exists()) {
            File fileFrom = new File(plugin.getDefaultFilesDirectory() + File.separator + "__default.json");
            try {
                Utils.copyFile(fileFrom, fileTo);
            } catch (IOException ex) {
                PwiLogger.severe("An error occurred copying file '" + fileFrom.getName() + "' to '" + fileTo.getName() + "':", ex);
            }
        }
    }
}

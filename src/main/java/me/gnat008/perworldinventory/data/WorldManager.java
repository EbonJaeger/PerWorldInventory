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

package me.gnat008.perworldinventory.data;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorldManager {

    private Map<String, List<String>> groups = new HashMap<>();
    private PerWorldInventory plugin;

    private static WorldManager manager = null;

    private WorldManager(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static WorldManager getInstance(PerWorldInventory plugin) {
        if (manager == null) {
            manager = new WorldManager(plugin);
        }

        return manager;
    }

    public static void disable() {
        manager = null;
    }

    public List<String> getGroup(String group) {
        if (groups.containsKey(group)) {
            return groups.get(group);
        } else {
            return null;
        }
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    public String getGroupFromWorld(String world) {
        for (String group : groups.keySet()) {
            if (groups.get(group).contains(world)) {
                return group;
            }
        }

        throw new IllegalArgumentException("World '" + world + "' not found! Could not get group!");
    }

    public void addGroup(String group, List<String> worlds) {
        groups.put(group, worlds);
    }

    public void removeGroup(String group) {
        groups.remove(group);
    }

    public boolean isInGroup(String group, String world) {
        if (groups.containsKey(group)) {
            return groups.get(group).contains(world);
        } else {
            throw new IllegalArgumentException("Group '" + group + "' does not exist!");
        }
    }

    public void loadGroups() {
        groups.clear();

        YamlConfiguration config = plugin.getConfigManager().getWorlds();
        Set<String> keys = config.getConfigurationSection("groups").getKeys(false);
        for (String key : keys) {
            if (config.get("groups." + key) instanceof List) {
                List<String> worlds = config.getStringList("groups." + key);
                addGroup(key, worlds);
            }
        }
    }
}

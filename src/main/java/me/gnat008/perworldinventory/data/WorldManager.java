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
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

public class WorldManager {

    private Map<String, List<String>> groups = new HashMap<>();
    private Map<String, GameMode> gameModes = new HashMap<>();
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

    public GameMode getGameMode(String group) {
        return gameModes.containsKey(group) ? gameModes.get(group) : null;
    }

    public List<String> getGroup(String group) {
        return groups.containsKey(group) ? groups.get(group) : null;
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

    public void addGroup(String group, List<String> worlds, GameMode defGameMode) {
        groups.put(group, worlds);
        if (defGameMode != null)
            gameModes.put(group, defGameMode);
    }

    public void removeGroup(String group) {
        groups.remove(group);
        gameModes.remove(group);
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
        gameModes.clear();

        YamlConfiguration config = plugin.getConfigManager().getConfig("worlds");
        for (String key : config.getConfigurationSection("groups").getKeys(false)) {
            List<String> worlds;
            GameMode gameMode;
            if (config.contains("groups." + key + ".worlds")) {
                worlds = config.getStringList("groups." + key + ".worlds");
                if (plugin.getConfigManager().getConfig("config").getBoolean("manage-gamemodes")) {
                    gameMode = GameMode.valueOf(config.getString("groups." + key + ".default-gamemode").toUpperCase());
                } else {
                    gameMode = null;
                }
            } else {
                worlds = config.getStringList("groups." + key);

                config.set("groups." + key, null);
                config.set("groups." + key + ".worlds", worlds);

                if (plugin.getConfigManager().getConfig("config").getBoolean("manage-gamemodes")) {
                    gameMode = GameMode.SURVIVAL;
                    config.set("groups." + key + ".default-gamemode", "SURVIVAL");
                } else {
                    gameMode = null;
                }
            }
            addGroup(key, worlds, gameMode);
        }

        for (String group : groups.keySet()) {
            File fileTo = new File(plugin.getDefaultFilesDirectory() + File.separator + group + ".json");
            if (!fileTo.exists()) {
                File fileFrom = new File(plugin.getDefaultFilesDirectory() + File.separator + "__default.json");
                copyFile(fileFrom, fileTo);
            }
        }
    }

    private void copyFile(File from, File to) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage(), true);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}

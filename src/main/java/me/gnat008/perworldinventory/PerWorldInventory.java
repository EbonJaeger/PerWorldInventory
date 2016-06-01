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

package me.gnat008.perworldinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.FileSerializer;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.gnat008.perworldinventory.commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.data.DataConverter;
import me.gnat008.perworldinventory.data.DataSerializer;
import me.gnat008.perworldinventory.groups.GroupManager;
import net.milkbowl.vault.economy.Economy;

public class PerWorldInventory extends JavaPlugin {

    private Economy economy;
    private DataSerializer serializer;
    private GroupManager groupManager;
    private PWIPlayerManager playerManager;

    private static Logger logger;
    private static PerWorldInventory instance = null;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // Make the data folders
        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").mkdirs();
        }

        // Copy over the server default loadout file
        if ((!(new File(getDataFolder() + File.separator + "__default.json").exists()))) {
            saveResource("default.json", false);
            File dFile = new File(getDataFolder() + File.separator + "default.json");
            dFile.renameTo(new File(getDefaultFilesDirectory() + File.separator + "__default.json"));
        }

        // Save the default config files if they do not exist
        saveDefaultConfig();
        if (!(new File(getDataFolder() + File.separator + "worlds.yml").exists()))
            saveResource("worlds.yml", false);
        Settings.reloadSettings(getConfig());
        if (Settings.getInt("config-version") < 1) {
            getLogger().warning("Your PerWorldInventory config is out of date! Some options may be missing.");
            getLogger().warning("Copy the new options from here: https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }

        // Load world groups
        groupManager = new GroupManager(this);
        FileConfiguration worldsConfig = getWorldsConfig();
        groupManager.loadGroupsToMemory(worldsConfig);

        playerManager = new PWIPlayerManager(this);

        // Register commands
        getLogger().info("Registering commands...");
        getCommand("pwi").setExecutor(new PerWorldInventoryCommand(this));

        // Register listeners
        getLogger().info("Commands registered! Registering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Check the server version to see if PlayerSpawnLocationEvent exists (at least 1.9.2)
        String cbVersionRaw = Bukkit.getVersion();
        String cbVersion = cbVersionRaw.substring(cbVersionRaw.length() - 6, cbVersionRaw.length() - 1);
        String[] parts = cbVersion.split("\\.");
        if ((Integer.parseInt(parts[0]) >= 1) && (Integer.parseInt(parts[1]) >= 9) && (Integer.parseInt(parts[2]) >= 2))
            getServer().getPluginManager().registerEvents(new PlayerSpawnLocationListener(this), this);

        if (Settings.getBoolean("separate-gamemode-inventories")) {
            getServer().getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            getLogger().info("Registered PlayerGameModeChangeListener.");
        }
        getLogger().info("Listeners enabled!");

        // Register Vault if present
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getLogger().info("Vault found! Hooking into it...");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                getLogger().info("Hooked into Vault!");
            } else {
                getLogger().warning("Unable to hook into Vault!");
            }
        }

        serializer = new FileSerializer(this);

        if (Settings.getBoolean("debug-mode"))
            printDebug("PerWorldInventory is enabled and debug-mode is active!");
    }

    @Override
    public void onDisable() {
        playerManager.onDisable();
        try {
            DataConverter.disable();
        }
        catch (NoClassDefFoundError ex) {
            // To be expected if multiverse isn't loaded
        }
        getGroupManager().disable();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    public static PerWorldInventory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cannot get instance before onEnable() has been called!");
        }

        return instance;
    }

    public static void printDebug(String message) {
        logger.info("[DEBUG] " + message);
    }

    public DataConverter getDataConverter() {
        return DataConverter.getInstance(this);
    }

    public DataSerializer getSerializer() {
        return serializer;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public File getDefaultFilesDirectory() {
        return new File(getDataFolder() + File.separator + "data" + File.separator + "defaults");
    }

    public FileConfiguration getWorldsConfig() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "worlds.yml"));
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    public PWIPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public void copyFile(File from, File to) {
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
            getLogger().severe("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage());
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

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

import me.gnat008.perworldinventory.commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.config.ConfigManager;
import me.gnat008.perworldinventory.config.ConfigType;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.DataConverter;
import me.gnat008.perworldinventory.data.DataSerializer;
import me.gnat008.perworldinventory.data.mysql.MySQLManager;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.listeners.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.listeners.PlayerQuitListener;
import me.gnat008.perworldinventory.util.Printer;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

public class PerWorldInventory extends JavaPlugin {

    private Connection conn = null;
    private Economy economy;

    private static PerWorldInventory instance = null;

    @Override
    public void onEnable() {
        instance = this;

        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").mkdirs();
        }

        if (!(new File(getDataFolder() + File.separator + "__default.json").exists())) {
            saveResource("default.json", false);
            File dFile = new File(getDataFolder() + File.separator + "default.json");
            dFile.renameTo(new File(getDefaultFilesDirectory() + File.separator + "__default.json"));
        }

        getConfigManager().addConfig(ConfigType.CONFIG, new File(getDataFolder() + File.separator + "config.yml"));
        getConfigManager().addConfig(ConfigType.WORLDS, new File(getDataFolder() + File.separator + "worlds.yml"));

        getGroupManager().loadGroupsToMemory();

        if (ConfigValues.ENABLE_METRICS.getBoolean()) {
            getLogger().info("Starting metrics...");
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                getLogger().info("Failed to start metrics!");
            }
        }
        
        getLogger().info("Registering commands...");
        getCommand("pwi").setExecutor(new PerWorldInventoryCommand(this));
        getLogger().info("Commands registered! Registering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        getLogger().info("Registered PlayerChangedWorldListener.");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getLogger().info("Registered PlayerQuitListener.");

        if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
            getServer().getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            getLogger().info("Registered PlayerGameModeChangeListener.");
        }
        getLogger().info("Listeners enabled!");

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

        if (ConfigValues.USE_MYSQL.getBoolean()) {
            getLogger().info("Configured to use MySQL! Attempting to connect to database...");
            try {
                MySQLManager.getInstance().startConnection();
            } catch (SQLException ex) {
                getLogger().warning("Could not connect to database: " + ex.getMessage());
                getLogger().warning("Setting 'use-mysql' to 'false' in the config, and switching to flatfiles!");
                //ConfigValues.USE_MYSQL.set(false);
                //getConfigManager().reloadConfig(ConfigType.CONFIG);
            }
        }
    }

    @Override
    public void onDisable() {
        Printer.disable();
        DataSerializer.disable();
        DataConverter.disable();
        getConfigManager().disable();
        getGroupManager().disable();
        getMySQLManager().disable();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    public static PerWorldInventory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cannot get instance before onEnable() has been called!");
        }

        return instance;
    }

    public ConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }

    public DataConverter getDataConverter() {
        return DataConverter.getInstance(this);
    }

    public DataSerializer getSerializer() {
        return DataSerializer.getInstance(this);
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public File getDefaultFilesDirectory() {
        return new File(getDataFolder() + File.separator + "data" + File.separator + "defaults");
    }

    public GroupManager getGroupManager() {
        return GroupManager.getInstance(this);
    }

    public MySQLManager getMySQLManager() {
        return MySQLManager.getInstance();
    }

    public Printer getPrinter() {
        return Printer.getInstance(this);
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
            getPrinter().printToConsole("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage(), true);
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

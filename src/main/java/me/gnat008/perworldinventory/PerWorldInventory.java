/**
 * PerWorldInventory is a multi-world inventory plugin.
 * Copyright (C) 2014 - 2015 Gnat008
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory;

import me.gnat008.perworldinventory.Commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.Config.ConfigManager;
import me.gnat008.perworldinventory.Config.ConfigType;
import me.gnat008.perworldinventory.Config.defaults.ConfigValues;
import me.gnat008.perworldinventory.Data.DataConverter;
import me.gnat008.perworldinventory.Data.DataSerializer;
import me.gnat008.perworldinventory.Groups.GroupManager;
import me.gnat008.perworldinventory.Listeners.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.Listeners.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.Listeners.PlayerQuitListener;
import me.gnat008.perworldinventory.Logger.PWILogger;
import me.gnat008.perworldinventory.Metrics.Metrics;
import me.gnat008.perworldinventory.Updater.SpigotUpdater;
import me.gnat008.perworldinventory.Util.ChatColor;
import me.gnat008.perworldinventory.Util.PlayerMessenger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PerWorldInventory extends JavaPlugin {

    // Initialize Economy
    private Economy economy;
    // Initialize updater
    private SpigotUpdater updater;
    // Initialize logger (auto implements enable/disable messages to console)
    public static PWILogger log;

    private static PerWorldInventory instance = null;

    @Override
    public void onEnable() {
        // Initialize Logger
        log = new PWILogger();
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

        log.info("Registering commands...");
        getCommand("pwi").setExecutor(new PerWorldInventoryCommand(this));
        log.info("Commands registered! Registering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        log.info("Registered PlayerChangedWorldListener.");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        log.info("Registered PlayerQuitListener.");

        if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
            getServer().getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            log.info("Registered PlayerGameModeChangeListener.");
        }
        log.info("Listeners enabled!");

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            log.info("Vault found! Hooking into it...");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                log.info("Hooked into Vault!");
            } else {
                log.warning("Unable to hook into Vault!");
            }
        }

    {

    }
    if (getConfig().getBoolean("CHECK_UPDATES")) {

        log.info("Initializing updater...");
        this.updater = new SpigotUpdater(this);
        getUpdater().checkUpdates();
        if (SpigotUpdater.updateAvailable()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "---------------------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "           PerWorldInventory Updater");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "An update for PerWorldInventory has been found!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "PerWorldInventory " + SpigotUpdater.getHighest());
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You are running " + getDescription().getVersion());
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Download at:");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "SpigotMC: https://goo.gl/W7b4yK");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "---------------------------------");
        }
    }

    log.info("Initializing Metrics..");
    setupMetrics();
    log.info("Enabled!");
}

    @Override
    public void onDisable() {
        PlayerMessenger.disable();
        DataSerializer.disable();
        DataConverter.disable();
        getConfigManager().disable();
        getGroupManager().disable();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            log.warning("Couldn't submit metrics stats: " + e.getMessage());
        }
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

    public PlayerMessenger getPlayerMessenger() {
        return PlayerMessenger.getInstance(this);
    }

    public SpigotUpdater getUpdater()
    {
        return this.updater;
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
            log.warning("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage());
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

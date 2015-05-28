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
import me.gnat008.perworldinventory.data.DataConverter;
import me.gnat008.perworldinventory.data.DataSerializer;
import me.gnat008.perworldinventory.data.WorldManager;
import me.gnat008.perworldinventory.listeners.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.listeners.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.listeners.PlayerQuitListener;
import me.gnat008.perworldinventory.util.Printer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PerWorldInventory extends JavaPlugin {

    private Economy economy;

    @Override
    public void onEnable() {
        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").mkdirs();
        }

        if (!(new File(getDataFolder() + File.separator + "__default.json").exists())) {
            saveResource("default.json", false);
            File dFile = new File(getDataFolder() + File.separator + "default.json");
            dFile.renameTo(new File(getDefaultFilesDirectory() + File.separator + "__default.json"));
        }

        getConfigManager().addConfigFile("config", new File(getDataFolder() + File.separator + "config.yml"), true);
        getConfigManager().addConfigFile("worlds", new File(getDataFolder() + File.separator + "worlds.yml"), true);

        getWorldManager().loadGroups();

        getLogger().info("Registering commands...");
        getCommand("pwi").setExecutor(new PerWorldInventoryCommand(this));
        getLogger().info("Commands registered! Registering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        getLogger().info("Registered PlayerChangedWorldListener.");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getLogger().info("Registered PlayerQuitListener.");

        if (getConfigManager().getConfig("config").getBoolean("separate-gamemode-inventories")) {
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
    }

    @Override
    public void onDisable() {
        Printer.disable();
        DataSerializer.disable();
        DataConverter.disable();
        getConfigManager().disable();
        WorldManager.disable();
        getServer().getScheduler().cancelTasks(this);
    }

    public ConfigManager getConfigManager() {
        return ConfigManager.getManager(this);
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

    public Printer getPrinter() {
        return Printer.getInstance(this);
    }

    public WorldManager getWorldManager() {
        return WorldManager.getInstance(this);
    }
}

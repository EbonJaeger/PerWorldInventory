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

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import me.gnat008.perworldinventory.api.PerWorldInventoryAPI;
import me.gnat008.perworldinventory.commands.*;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataSource;
import me.gnat008.perworldinventory.data.DataSourceProvider;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.entity.EntityPortalEventListener;
import me.gnat008.perworldinventory.listeners.player.*;
import me.gnat008.perworldinventory.listeners.server.InventoryLoadingListener;
import me.gnat008.perworldinventory.listeners.server.PluginListener;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.util.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PerWorldInventory extends JavaPlugin {

    private PerWorldInventoryAPI api;
    private Economy economy;
    private GroupManager groupManager;
    private PWIPlayerManager playerManager;
    private Settings settings;
    private PermissionManager permissionManager;

    private final Map<String, ExecutableCommand> commands = new HashMap<>();

    /**
     * Constructor.
     */
    public PerWorldInventory() {
    }

    /*
     * Constructor for testing purposes.
     */
    protected PerWorldInventory(final JavaPluginLoader loader, final PluginDescriptionFile description,
                                final File dataFolder, final File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        ConsoleLogger.setLogger(getLogger());

        // Make the data folders
        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").mkdirs();
        }

        // Copy over the server default loadout file
        if (!(new File(getDefaultFilesDirectory(), "__default.json").exists())) {
            saveResource("__default.json", false);
            File dFile = new File(getDataFolder(), "__default.json");
            Path source = dFile.toPath();
            Path destination = getDefaultFilesDirectory().toPath();
            try {
                Files.move(source, destination.resolve(source.getFileName()));
            } catch (IOException ex) {
                if (!(ex instanceof FileAlreadyExistsException)) {
                    ConsoleLogger.warning("Unable to move defaults.json to the defaults folder:", ex);
                }
            }
        }

        if (!(new File(getDataFolder() + File.separator + "worlds.yml").exists()))
            saveResource("worlds.yml", false);

        /* Initialization */
        Injector injector = new InjectorBuilder().addDefaultHandlers("me.gnat008.perworldinventory").create();
        injector.register(PerWorldInventory.class, this);
        injector.register(Server.class, getServer());
        injector.register(PluginManager.class, getServer().getPluginManager());
        injector.provide(DataFolder.class, getDataFolder());
        injector.registerProvider(DataSource.class, DataSourceProvider.class);
        settings = initSettings();
        injector.register(Settings.class, settings);
        injectServices(injector);
        ConsoleLogger.setUseDebug(settings.getProperty(PwiProperties.DEBUG_MODE));
        registerEventListeners(injector);

        // Load world groups
        FileConfiguration worldsConfig = getWorldsConfig();
        groupManager.loadGroupsToMemory(worldsConfig);

        // Register commands
        getLogger().info("Registering commands...");
        registerCommands(injector);

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

        // Start bStats metrics
        if (settings.getProperty(PwiProperties.ENABLE_METRICS)) {
            Metrics bStats = new Metrics(this);

            if (settings.getProperty(PwiProperties.SEND_NUM_GROUPS)) {
                // Get the total number of configured Groups
                bStats.addCustomChart(new Metrics.SimplePie("num_groups") {
                    @Override
                    public String getValue() {
                        int numGroups = groupManager.countGroups();

                        return String.valueOf(numGroups);
                    }
                });
            }

            if (settings.getProperty(PwiProperties.SEND_NUM_WORLDS)) {
                // Get the total number of worlds (configured or not)
                bStats.addCustomChart(new Metrics.SimplePie("num_worlds") {
                    @Override
                    public String getValue() {
                        int numWorlds = Bukkit.getWorlds().size();

                        if (numWorlds <= 5) {
                            return "1-5";
                        } else if (numWorlds <= 10) {
                            return "6-10";
                        } else if (numWorlds <= 15) {
                            return "11-15";
                        } else if (numWorlds <= 20) {
                            return "16-20";
                        } else if (numWorlds <= 25) {
                            return "21-25";
                        } else if (numWorlds <= 30) {
                            return "26-30";
                        } else {
                            return String.valueOf(numWorlds);
                        }
                    }
                });
            }
        }

        ConsoleLogger.debug("PerWorldInventory is enabled and debug-mode is active!");
    }

    @Override
    public void onDisable() {
        playerManager.onDisable();
        groupManager.clearGroups();
        getServer().getScheduler().cancelTasks(this);
    }

    protected void injectServices(Injector injector) {
        settings = injector.getSingleton(Settings.class);
        groupManager = injector.getSingleton(GroupManager.class);
        playerManager = injector.getSingleton(PWIPlayerManager.class);
        permissionManager = injector.getSingleton(PermissionManager.class);
        api = injector.getSingleton(PerWorldInventoryAPI.class);
    }

    protected void registerEventListeners(Injector injector) {
        getLogger().info("Registering listeners...");

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(injector.getSingleton(PluginListener.class), this);

        pluginManager.registerEvents(injector.getSingleton(PlayerTeleportListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(PlayerChangedWorldListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(PlayerDeathListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(PlayerGameModeChangeListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(PlayerQuitListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(EntityPortalEventListener.class), this);
        pluginManager.registerEvents(injector.getSingleton(InventoryLoadingListener.class), this);

        // The PlayerSpawnLocationEvent is only fired in Spigot
        // As of version 1.9.2
        if (Bukkit.getVersion().contains("Spigot") && Utils.checkServerVersion(Bukkit.getVersion(), 1, 9, 2)) {
            pluginManager.registerEvents(injector.getSingleton(PlayerSpawnLocationListener.class), this);
        }
        getLogger().info("Listeners registered!");
    }

    protected void registerCommands(Injector injector) {
        commands.put("pwi", injector.getSingleton(PerWorldInventoryCommand.class));
        commands.put("convert", injector.getSingleton(ConvertCommand.class));
        commands.put("help", injector.getSingleton(HelpCommand.class));
        commands.put("reload", injector.getSingleton(ReloadCommand.class));
        commands.put("setworlddefault", injector.getSingleton(SetWorldDefaultCommand.class));
        commands.put("version", injector.getSingleton(VersionCommand.class));
        getLogger().info("Commands registered!");
    }

    /**
     * Get a class for other plugins to more easily integrate with
     * PerWorldInventory.
     *
     * @return The API class.
     */
    public PerWorldInventoryAPI getAPI() {
        return api;
    }

    public void reload() {
        ConsoleLogger.setUseDebug(settings != null && settings.getProperty(PwiProperties.DEBUG_MODE));
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

    /**
     * Get whether the economy function of PWI is enabled.
     * Vault has to be hooked and the setting has to be enabled in the
     * config for this method to return true.
     *
     * @return If the plugin's economy feature is enabled.
     */
    public boolean isEconEnabled() {
        return economy != null && settings.getProperty(PwiProperties.USE_ECONOMY);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String... args) {
        if (command.getName().equalsIgnoreCase("pwi")) {
            if (args.length == 0) {
                commands.get("pwi").executeCommand(sender, Collections.emptyList());
                return true;
            }

            ExecutableCommand mappedCommand = commands.get(args[0].toLowerCase());
            if (mappedCommand != null) {
                if (!permissionManager.hasPermission(sender, mappedCommand.getRequiredPermission())) {
                    sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
                    return true;
                }

                // Add all args excluding the first one
                List<String> argsList = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    argsList.add(args[i]);
                }

                // Execute the command
                mappedCommand.executeCommand(sender, argsList);
                return true;
            } else {
                commands.get("pwi").executeCommand(sender, Collections.emptyList());
                return true;
            }
        }

        return false;
    }

    private Settings initSettings() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        return new Settings(configFile);
    }
}

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

package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.FileSerializer;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PerWorldInventoryCommand implements CommandExecutor {

    private PerWorldInventory plugin;
    private PermissionManager permissionManager;

    private final String NO_PERMISSION = "You do not have permission to do that.";

    public PerWorldInventoryCommand(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.permissionManager = plugin.getPermissionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isPlayer = false;
        Player player = null;
        if (sender instanceof Player) {
            isPlayer = true;
            player = (Player) sender;
        }

        PWICommand command;
        try {
            command = PWICommand.valueOf(args[0].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            if (isPlayer) {
                /*new FancyMessage("» ")
                        .color(ChatColor.BLUE)
                        .then("Not a valid command, ")
                        .color(ChatColor.GRAY)
                        .then("click here ")
                        .color(ChatColor.BLUE)
                        .suggest("/perworldinventory help")
                        .then("for help.")
                        .color(ChatColor.GRAY)
                        .send(player);*/
                player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Not a valid command. Type " + ChatColor.BLUE + "/perworldinventory help " + ChatColor.GRAY + "for help.");
            } else {
                displayConsoleHelp();
            }

            return true;
        }

        switch (command) {
            case CONVERT:
                if (isPlayer) {
                    if (permissionManager.hasPermission(player, AdminPermission.CONVERT)) {
                        if (args.length == 2) {
                            switch (args[1].toUpperCase()) {
                                case "MULTIVERSE":
                                    Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
                                    if (p == null) {
                                        sender.sendMessage(ChatColor.RED + "I'm sorry, Multiverse-Inventories isn't loaded... Import aborted.");
                                    } else {
                                        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Starting data conversions. Check console for details.");
                                        mvConvert();
                                        /*new FancyMessage("» ")
                                                .color(ChatColor.BLUE)
                                                .then("Starting data conversion, messages have been set to your terminal...")
                                                .color(ChatColor.GRAY)
                                                .send(player);*/
                                    }
                                    break;
                                case "MULTIINV":
                                    p = plugin.getServer().getPluginManager().getPlugin("MultiInv");
                                    if (p == null) {
                                        sender.sendMessage(ChatColor.RED + "I'm sorry, MultiInv isn't loaded... Import aborted.");
                                    } else {
                                        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Starting data conversion. Check console for details.");
                                        mvConvert();
                                        /*new FancyMessage("» ")
                                                .color(ChatColor.BLUE)
                                                .then("Starting data conversion, messages have been set to your terminal...")
                                                .color(ChatColor.GRAY)
                                                .send(player);*/
                                    }
                                    break;
                                default:
                                    player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Valid arguments are: MULTIVERSE | MULTIINV");
                                    break;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "You must specify the server to convert from: MULTIVERSE | MULTIINV");
                        }
                    } else {
                        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + NO_PERMISSION);
                    }
                } else {
                    if (args.length == 2) {
                        switch (args[1].toUpperCase()) {
                            case "MULTIVERSE":
                                plugin.getLogger().info("Converting from MultiVerse-Inventories!");
                                mvConvert();
                                break;
                            case "MULTIINV":
                                plugin.getLogger().info("Converting from MultiInv!");
                                miConvert();
                                break;
                            default:
                                plugin.getLogger().info("Valid arguments are: MULTIVERSE | MULTIINV");
                                break;
                        }
                    } else {
                        plugin.getLogger().info("You must specify the server to convert from: MULTIVERSE | MULTIINV");
                    }
                }

                return true;

            case HELP:
                if (isPlayer) {
                    displayPlayerHelp(player);
                } else {
                    displayConsoleHelp();
                }

                return true;

            case VERSION: {
                if (isPlayer) {
                    playerVersion(player);
                } else {
                    consoleVersion();
                }
                return true;
            }

            case RELOAD:
                if (isPlayer) {
                    if (permissionManager.hasPermission(player, AdminPermission.RELOAD)) {
                        reload(player);
                    } else {
                        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + NO_PERMISSION);
                    }
                } else {
                    reload();
                }

                return true;

            case SETWORLDDEFAULT:
                if (isPlayer) {
                    if (permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)) {
                        Group group;

                        if (args.length == 2) {
                            group = args[1].equalsIgnoreCase("default") ? new Group("__default", null, null) : plugin.getGroupManager().getGroup(args[1]);
                            setWorldDefault(player, group);
                        } else {
                            try {
                                group = plugin.getGroupManager().getGroupFromWorld(player.getWorld().getName());
                                setWorldDefault(player, group);
                            } catch (IllegalArgumentException ex) {
                                player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "You are not standing in a valid world!");
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + NO_PERMISSION);
                    }
                } else {
                    plugin.getLogger().warning("This command can only be run from ingame.");
                }

                return true;
        }

        return false;
    }

    private void mvConvert() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getDataConverter().convertMultiVerseData();
            }
        });
    }

    private void miConvert() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getDataConverter().convertMultiInvData();
            }
        });
    }

    private void playerVersion(Player player) {
        String version = plugin.getDescription().getVersion();
        String authors = plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("Version: ")
                .color(ChatColor.GRAY)
                .then(version)
                .color(ChatColor.BLUE)
                .send(player);
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("Author: ")
                .color(ChatColor.GRAY)
                .then(String.valueOf(authors))
                .color(ChatColor.BLUE)
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Version: " + ChatColor.BLUE + version);
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Author: " + ChatColor.BLUE + authors);
    }

    private void consoleVersion() {
        String version = plugin.getDescription().getVersion();
        List<String> authors = plugin.getDescription().getAuthors();
        Bukkit.getConsoleSender().sendMessage("Version: " + version);
        Bukkit.getConsoleSender().sendMessage("Author: " + authors);
    }

    private void displayConsoleHelp() {
        plugin.getLogger().info("Available commands:");
        plugin.getLogger().info("/perworldinventory convert - Convert MultiVerse-Inventories data");
        plugin.getLogger().info("/perworldinventory help - Displays this help");
        plugin.getLogger().info("/perworldinventory version - Shows the version of the server");
        plugin.getLogger().info("/perworldinventory reload - Reload config and world files");
    }

    private void displayPlayerHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
        /*new FancyMessage
                ("                ")
                .then("[")
                .color(ChatColor.DARK_GRAY)
                .then("PerWorldInventory Commands")
                .color(ChatColor.BLUE)
                .tooltip(ChatColor.YELLOW + "To use a command without typing," + '\n' + ChatColor.YELLOW + "click on the Hover context...")
                .then("]")
                .color(ChatColor.DARK_GRAY)
                .send(player);*/
        player.sendMessage(ChatColor.DARK_GRAY + "                [ " + ChatColor.BLUE + "PerWorldInventory Commands" + ChatColor.DARK_GRAY + " ]");
        player.sendMessage("");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory convert multiverse")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory convert multiverse")
                .tooltip(ChatColor.YELLOW + "Convert data from Multiverse-Inventories")
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory convert multiverse" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Convert data from Multiverse-Inventories");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory help")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory help")
                .tooltip(ChatColor.YELLOW + "Shows this help page")
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory help" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Shows this help page");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory reload")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory reload")
                .tooltip(ChatColor.YELLOW + "Reloads all configuration files")
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory reload" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Reloads all configuration files");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory version")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory version")
                .tooltip(ChatColor.YELLOW + "Shows the version of the server, and authors")
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory version" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Shows the version and authors of the server");
        /*new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory setworlddefault [group]")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory setworlddefault [group]")
                .tooltip(ChatColor.YELLOW + "Set the default inventory loadout for a world, or the server default." + '\n' + ChatColor.YELLOW + "The group you are standing in will be used if no group is specified.")
                .send(player);*/
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory setworlddefault [group]" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Set the default inventory loadout for a world, or the server default." + '\n' + ChatColor.YELLOW + "The group you are standing in will be used if no group is specified.");
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
    }

    private void reload() {
        reloadConfigFiles();

        plugin.getLogger().info("Configuration files reloaded.");
    }

    private void reload(Player player) {
        reloadConfigFiles();

        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Configuration files reloaded.");
    }

    private void reloadConfigFiles() {
    	plugin.reloadConfig();
        Settings.reloadSettings(plugin.getConfig());
        if (Settings.getInt("config-version") < PerWorldInventory.CONFIG_VERSION) {
            plugin.getLogger().warning("Your PerWorldInventory config is out of date! Some options may be missing.");
            plugin.getLogger().warning("Copy the new options from here: https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }
        plugin.getGroupManager().loadGroupsToMemory(plugin.getWorldsConfig());
    }

    private void setWorldDefault(Player player, Group group) {
        FileSerializer fs = new FileSerializer(plugin);
        File file = new File(plugin.getDefaultFilesDirectory() + File.separator + group.getName() + ".json");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Default file for this group not found!");
            return;
        }

        File tmp = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + player.getUniqueId() + File.separator + "tmp.json");
        try {
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
        } catch (IOException ex) {
            player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY +  "Could not create temporary file! Aborting!");
            return;
        }
        Group tempGroup = new Group("tmp", null, null);
        fs.writeData(tmp, PlayerSerializer.serialize(plugin, new PWIPlayer(player, tempGroup)));

        player.setFoodLevel(20);
        player.setHealth(20);
        player.setSaturation(20);
        player.setTotalExperience(0);

        fs.writeData(file, PlayerSerializer.serialize(plugin, new PWIPlayer(player, group)));

        fs.getFromDatabase(tempGroup, GameMode.SURVIVAL, player);
        tmp.delete();
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY +  "Defaults for '" + group.getName() + "' set!");
    }
}

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

import com.google.gson.Gson;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.FileSerializer;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PerWorldInventoryCommand implements CommandExecutor {

    private PerWorldInventory plugin;

    private final String NO_PERMISSION = "You do not have permission to do that.";
    private final String PERMISSION_NODE = "perworldinventory.";

    public PerWorldInventoryCommand(PerWorldInventory plugin) {
        this.plugin = plugin;
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
                new FancyMessage("» ")
                        .color(ChatColor.BLUE)
                        .then("Not a valid command, ")
                        .color(ChatColor.GRAY)
                        .then("click here ")
                        .color(ChatColor.BLUE)
                        .suggest("/perworldinventory help")
                        .then("for help.")
                        .color(ChatColor.GRAY)
                        .send(player);
            } else {
                displayConsoleHelp();
            }

            return true;
        }

        switch (command) {
            case CONVERT:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "convert")) {
                        if (args.length == 2) {
                            switch (args[1].toUpperCase()) {
                                case "MULTIVERSE":
                                    plugin.getPrinter().printToPlayer(player, "Converting from MultiVerse-Inventories! All messages are sent to console!", false);
                                    mvConvert();
                                    break;
                                case "MULTIINV":
                                    plugin.getPrinter().printToPlayer(player, "Converting from MultiInv! All messages are sent to console!", false);
                                    miConvert();
                                    break;
                                default:
                                    plugin.getPrinter().printToPlayer(player, "Valid arguments are: MULTIVERSE | MULTIINV", true);
                                    break;
                            }
                        } else {
                            plugin.getPrinter().printToPlayer(player, "You must specify the plugin to convert from: MULTIVERSE | MULTIINV", true);
                        }
                    } else {
                        plugin.getPrinter().printToPlayer(player, NO_PERMISSION, true);
                    }
                } else {
                    if (args.length == 2) {
                        switch (args[1].toUpperCase()) {
                            case "MULTIVERSE":
                                plugin.getPrinter().printToConsole("Converting from MultiVerse-Inventories!", false);
                                mvConvert();
                                break;
                            case "MULTIINV":
                                plugin.getPrinter().printToConsole("Converting from MultiInv!", false);
                                miConvert();
                                break;
                            default:
                                plugin.getPrinter().printToConsole("Valid arguments are: MULTIVERSE | MULTIINV", true);
                                break;
                        }
                    } else {
                        plugin.getPrinter().printToConsole("You must specify the plugin to convert from: MULTIVERSE | MULTIINV", true);
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
                    if (player.hasPermission(PERMISSION_NODE + "reload")) {
                        reload(player);
                    } else {
                        plugin.getPrinter().printToPlayer(player, NO_PERMISSION, true);
                    }
                } else {
                    reload();
                }

                return true;

            case SETWORLDDEFAULT:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "setdefaults")) {
                        Group group;

                        if (args.length == 2) {
                            group = args[1].equalsIgnoreCase("default") ? new Group("__default", null, null) : plugin.getGroupManager().getGroup(args[1]);
                            setWorldDefault(player, group);
                        } else {
                            try {
                                group = plugin.getGroupManager().getGroupFromWorld(player.getWorld().getName());
                                setWorldDefault(player, group);
                            } catch (IllegalArgumentException ex) {
                                plugin.getPrinter().printToPlayer(player, "You are not standing in a valid world!", true);
                            }
                        }
                    } else {
                        plugin.getPrinter().printToPlayer(player, NO_PERMISSION, true);
                    }
                } else {
                    plugin.getPrinter().printToConsole("This command can only be run from ingame.", true);
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
        List<String> authors = plugin.getDescription().getAuthors();
        new FancyMessage("» ")
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
                .send(player);
    }

    private void consoleVersion() {
        String version = plugin.getDescription().getVersion();
        List<String> authors = plugin.getDescription().getAuthors();
        Bukkit.getConsoleSender().sendMessage("Version: " + version);
        Bukkit.getConsoleSender().sendMessage("Author: " + authors);
    }

    private void displayConsoleHelp() {
        plugin.getPrinter().printToConsole("Available commands:", false);
        plugin.getPrinter().printToConsole("/perworldinventory convert - Convert MultiVerse-Inventories data", false);
        plugin.getPrinter().printToConsole("/perworldinventory help - Displays this help", false);
        plugin.getPrinter().printToConsole("/perworldinventory version - Shows the version of the plugin", false);
        plugin.getPrinter().printToConsole("/perworldinventory reload - Reload config and world files", false);
    }

    private void displayPlayerHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
        new FancyMessage
                ("             ")
                .then("[")
                .color(ChatColor.DARK_GRAY)
                .then("PerWorldInventory Commands")
                .color(ChatColor.BLUE)
                .tooltip(ChatColor.YELLOW + "To use a command without typing," + '\n' + ChatColor.YELLOW + "click on the Hover context...")
                .then("]")
                .color(ChatColor.DARK_GRAY)
                .send(player);
        player.sendMessage("");
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory convert multiverse")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory convert multiverse")
                .tooltip(ChatColor.YELLOW + "Convert data from Multiverse-Inventories")
                .send(player);
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory help")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory help")
                .tooltip(ChatColor.YELLOW + "Shows this help page")
                .send(player);
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory reload")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory reload")
                .tooltip(ChatColor.YELLOW + "Reloads all configuration files")
                .send(player);
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory version")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory version")
                .tooltip(ChatColor.YELLOW + "Shows the version of the plugin, and authors")
                .send(player);
        new FancyMessage("» ")
                .color(ChatColor.BLUE)
                .then("/perworldinventory setworlddefault [group]")
                .color(ChatColor.GRAY)
                .then(" - Hover")
                .color(ChatColor.RED)
                .suggest("/perworldinventory setworlddefault [group]")
                .tooltip(ChatColor.YELLOW + "Set the default inventory loadout for a world, or the server default." + '\n' + ChatColor.YELLOW + "The group you are standing in will be used if no group is specified.")
                .send(player);
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
    }

    private void reload() {
        reloadConfigFiles();

        plugin.getPrinter().printToConsole("Configuration files reloaded.", false);
    }

    private void reload(Player player) {
        reloadConfigFiles();

        plugin.getPrinter().printToPlayer(player, "Configuration files reloaded.", false);
    }

    private void reloadConfigFiles() {
        Settings.reloadSettings(plugin.getConfig());
        if (Settings.getInt("config-version") < 1) {
            plugin.getLogger().warning("Your PerWorldInventory config is out of date! Some options may be missing.");
            plugin.getLogger().warning("Copy the new options from here: https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }
        plugin.getGroupManager().loadGroupsToMemory(plugin.getWorldsConfig());
    }

    private void setWorldDefault(Player player, Group group) {
        FileSerializer fs = new FileSerializer(plugin);
        Gson gson = new Gson();
        File file = new File(plugin.getDefaultFilesDirectory() + File.separator + group.getName() + ".json");
        if (!file.exists()) {
            plugin.getPrinter().printToPlayer(player, "Default file for this group not found!", true);
            return;
        }

        File tmp = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + player.getUniqueId() + File.separator + "tmp.json");
        try {
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
        } catch (IOException ex) {
            plugin.getPrinter().printToPlayer(player, "Could not create temporary file! Aborting!", true);
            return;
        }
        Group tempGroup = new Group("tmp", null, null);
        String writable = gson.toJson(PlayerSerializer.serialize(plugin, new PWIPlayer(player, tempGroup)));
        fs.writeData(tmp, writable);

        player.setFoodLevel(20);
        player.setHealth(20);
        player.setSaturation(20);
        player.setTotalExperience(0);

        writable = gson.toJson(PlayerSerializer.serialize(plugin, new PWIPlayer(player, group)));
        fs.writeData(file, writable);

        fs.getFromDatabase(tempGroup, GameMode.SURVIVAL, player);
        tmp.delete();
        plugin.getPrinter().printToPlayer(player, "Defaults for '" + group.getName() + "' set!", false);
    }
}

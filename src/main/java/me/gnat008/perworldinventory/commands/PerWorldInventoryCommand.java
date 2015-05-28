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

import com.kill3rtaco.tacoserialization.PlayerSerialization;
import com.kill3rtaco.tacoserialization.Serializer;
import me.gnat008.perworldinventory.PerWorldInventory;
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

    private enum Commands {CONVERT, HELP, RELOAD, SETWORLDDEFAULT}

    private PerWorldInventory plugin;

    private final String NO_PERMISSION = "You do not have permission to do that.";
    private final String PERMISSION_NODE = "perworldinventories.";

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

        Commands command;
        try {
            command = Commands.valueOf(args[0].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            if (isPlayer) {
                plugin.getPrinter().printToPlayer((Player) sender, "Not a valid command. Please type /pwi help for help.", true);
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
                        String group = "";

                        if (args.length == 2) {
                            group = args[1].equalsIgnoreCase("default") ? "__default" : args[1];
                            setWorldDefault(player, group);
                        } else {
                            try {
                                group = plugin.getWorldManager().getGroupFromWorld(player.getWorld().getName());
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

    private void displayConsoleHelp() {
        plugin.getPrinter().printToConsole("Available commands:", false);
        plugin.getPrinter().printToConsole("/pwi convert - Convert MultiVerse-Inventories data", false);
        plugin.getPrinter().printToConsole("/pwi help - Displays this help", false);
        plugin.getPrinter().printToConsole("/pwi reload - Reload config and world files", false);
    }

    private void displayPlayerHelp(Player player) {
        String version = plugin.getDescription().getVersion();
        List<String> authors = plugin.getDescription().getAuthors();

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "     PerWorldInventory Help Page:");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.GREEN + version);
        player.sendMessage(ChatColor.GOLD + "Author: " + ChatColor.GREEN + authors);
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "/pwi convert" + ChatColor.GOLD + " - Convert data from MultiVerse-Inventories.");
        player.sendMessage(ChatColor.WHITE + "/pwi help" + ChatColor.GOLD + " - Displays this help page.");
        player.sendMessage(ChatColor.WHITE + "/pwi reload" + ChatColor.GOLD + " - Reloads all configuration files.");
        player.sendMessage(ChatColor.WHITE + "/pwi setworlddefault [group]" + ChatColor.GOLD + " - Set the default inventory " +
                "loadout for a world, or the server default. The group you are standing in will be used if no group is specified.");
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
        plugin.getConfigManager().reloadConfigs();
        plugin.getWorldManager().loadGroups();
    }

    private void setWorldDefault(Player player, String group) {
        File file = new File(plugin.getDefaultFilesDirectory() + File.separator + group + ".json");
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
        plugin.getSerializer().writePlayerDataToFile(player, PlayerSerialization.serializePlayer(player, plugin), "tmp", GameMode.SURVIVAL.toString());

        player.setFoodLevel(20);
        player.setHealth(20);
        player.setSaturation(20);
        player.setTotalExperience(0);

        plugin.getSerializer().writeData(file, Serializer.toString(PlayerSerialization.serializePlayer(player, plugin)));

        plugin.getSerializer().getPlayerDataFromFile(player, "tmp", GameMode.SURVIVAL.toString());
        tmp.delete();
        plugin.getPrinter().printToPlayer(player, "Defaults for '" + group + "' set!", false);
    }
}

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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PerWorldInventoryCommand implements CommandExecutor {

    private enum Commands {CONVERT, HELP, RELOAD}

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
                        plugin.getPrinter().printToPlayer(player, "This is currently not guaranteed to work. All messages printed to console", true);
                        mvConvert();
                    } else {
                        plugin.getPrinter().printToPlayer(player, NO_PERMISSION, true);
                    }
                } else {
                    plugin.getPrinter().printToConsole("This is currently not guaranteed to work.", true);
                    mvConvert();
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
        plugin.getConfigManager().reloadConfig();
        plugin.getConfigManager().reloadWorlds();
    }
}

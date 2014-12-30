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

package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Printer {

    private static PerWorldInventory plugin;
    private static Printer instance;

    private Printer(PerWorldInventory main) {
        plugin = main;
    }

    public static Printer getInstance(PerWorldInventory plugin) {
        if (instance == null) {
            instance = new Printer(plugin);
        }

        return instance;
    }

    public static void disable() {
        instance = null;
    }

    /**
     * Method to print a message to the server console.
     *
     * @param msg  The message to send to console.
     * @param warn The level of the message.
     */
    public static void printToConsole(String msg, boolean warn) {
        if (warn) {
            plugin.getLogger().warning("[BlockPlaceLimiter] " + msg);
        } else {
            plugin.getLogger().info("[BlockPlaceLimiter] " + msg);
        }
    }

    /**
     * Method to print a message to a specific player on the server.
     *
     * @param player The player to send the message to.
     * @param msg    The message to send.
     * @param warn   If the message should be a warning (true for red, false for green).
     */
    public static void printToPlayer(Player player, String msg, boolean warn) {
        String message = "";

        if (warn) {
            message += ChatColor.RED;
        } else {
            message += ChatColor.GREEN;
        }

        message += "[BlockPlaceLimiter] " + msg;

        player.sendMessage(message);
    }
}

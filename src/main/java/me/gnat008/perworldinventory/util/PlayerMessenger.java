/**
 * NexusInventory is a multi-world inventory plugin.
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

package me.gnat008.perworldinventory.Util;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.entity.Player;

public class PlayerMessenger {

    private static PerWorldInventory plugin;
    private static PlayerMessenger instance;

    private PlayerMessenger(PerWorldInventory main) {
        plugin = main;
    }

    public static PlayerMessenger getInstance(PerWorldInventory plugin) {
        if (instance == null) {
            instance = new PlayerMessenger(plugin);
        }

        return instance;
    }

    public static void disable() {
        instance = null;
    }

    /**
     * Method to print a message to a specific player on the server.
     *
     * @param player The player to send the message to.
     * @param msg    The message to send.
     */
    public void sendMessage(Player player, String msg) {
        String message = "";

        message += ChatColor.GRAY + "[" + ChatColor.AQUA + "PerWorldInventory" + ChatColor.GRAY + "] " + ChatColor.YELLOW + msg;

        player.sendMessage(message);
    }
}

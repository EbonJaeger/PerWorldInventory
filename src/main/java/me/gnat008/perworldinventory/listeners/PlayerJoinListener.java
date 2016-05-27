/*
 * Copyright (C) 2014-2016  EbonJaguar
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

package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private GroupManager groupManager;
    private PWIPlayerManager playerManager;
    private PerWorldInventory plugin;

    public PlayerJoinListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.groupManager = plugin.getGroupManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("perworldinventory.notify") && Settings.getInt("config-version") < 3) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "Your PerWorldInventory config is out of date! Some options may be missing.");
            event.getPlayer().sendMessage(ChatColor.BLUE + "Copy the new options from here: " + ChatColor.WHITE + "https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }
    }
}

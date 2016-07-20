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

package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;

public class PlayerQuitListener implements Listener {

    private DataWriter serializer;
    private GroupManager manager;
    private PerWorldInventory plugin;
    private PWIPlayerManager playerManager;

    @Inject
    PlayerQuitListener(DataWriter serializer, GroupManager groupManager, PerWorldInventory plugin, PWIPlayerManager playerManager) {
        this.serializer = serializer;
        this.manager = groupManager;
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String logoutWorld = player.getWorld().getName();
        Group group = manager.getGroupFromWorld(logoutWorld);

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' quit! Checking cache");

        PWIPlayer cached = playerManager.getPlayer(group, player);
        if (cached != null) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Cached data for player '" + player.getName() + "' found! Updating and setting them as saved");

            playerManager.updateCache(player, cached);
            cached.setSaved(true);
        }

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Saving logout data for player '" + player.getName() + "'");

        PWIPlayer pwiPlayer = new PWIPlayer(plugin, player, group);
        serializer.saveToDatabase(group,
                Settings.getBoolean("separate-gamemode-inventories") ? player.getGameMode() : GameMode.SURVIVAL,
                pwiPlayer,
                true);
        serializer.saveLogoutData(pwiPlayer);

        playerManager.removePlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        String logoutWorld = player.getWorld().getName();
        Group group = manager.getGroupFromWorld(logoutWorld);

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' was kicked! Checking cache");

        PWIPlayer cached = playerManager.getPlayer(group, player);
        if (cached != null) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Cached data for player '" + player.getName() + "' found! Updating and setting them as saved");

            playerManager.updateCache(player, cached);
            cached.setSaved(true);
        }

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Saving logout data for player '" + player.getName() + "'");

        PWIPlayer pwiPlayer = new PWIPlayer(plugin, player, group);
        serializer.saveToDatabase(group,
                Settings.getBoolean("separate-gamemode-inventories") ? player.getGameMode() : GameMode.SURVIVAL,
                pwiPlayer,
                true);
        serializer.saveLogoutData(pwiPlayer);

        playerManager.removePlayer(player);
    }
}

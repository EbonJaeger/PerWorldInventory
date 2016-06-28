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
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    private GroupManager groupManager;
    private PermissionManager permissionManager;
    private PWIPlayerManager playerManager;

    public PlayerChangedWorldListener(GroupManager groupManager, PermissionManager permissionManager, PWIPlayerManager playerManager) {
        this.groupManager = groupManager;
        this.permissionManager = permissionManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldFrom = event.getFrom().getName();
        String worldTo = player.getWorld().getName();
        Group groupFrom = groupManager.getGroupFromWorld(worldFrom);
        Group groupTo = groupManager.getGroupFromWorld(worldTo);

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' going from world '" + worldFrom + "' to world '" + worldTo + "'");

        playerManager.addPlayer(player, groupFrom);

        if (!Settings.getBoolean("disable-bypass") && permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Player '" + player.getName() + "' has '" + PlayerPermission.BYPASS_WORLDS.getNode() + "' permission! Returning");
            return;
        }

        if (groupFrom.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
            return;
        }

        if (!groupFrom.containsWorld(worldTo)) {

            if (groupTo.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
                return;
            }

            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Different groups!");

            if (Settings.getBoolean("separate-gamemode-inventories")) {
                if (Settings.getBoolean("debug-mode"))
                    PerWorldInventory.printDebug("Gamemodes are separated! Loading data for player '" + player.getName() + "' for group '" + groupTo.getName() + "' in gamemode '" + player.getGameMode().name() + "'");

                playerManager.getPlayerData(groupTo, player.getGameMode(), player);

                if (Settings.getBoolean("manage-gamemodes")) {
                    if (Settings.getBoolean("debug-mode"))
                        PerWorldInventory.printDebug("PWI manages gamemodes! Setting player '" + player.getName() + "' to gamemode " + groupTo.getGameMode().name());

                    player.setGameMode(groupTo.getGameMode());
                }
            } else {
                if (Settings.getBoolean("debug-mode"))
                    PerWorldInventory.printDebug("Loading data for player '" + player.getName() + "' for group '" + groupTo.getName() + "'");

                playerManager.getPlayerData(groupTo, GameMode.SURVIVAL, player);
            }
        }
    }
}

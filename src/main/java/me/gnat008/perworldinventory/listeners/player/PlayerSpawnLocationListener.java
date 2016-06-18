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

package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnLocationListener implements Listener {

    private PerWorldInventory plugin;
    private GroupManager groupManager;
    private PermissionManager permissionManager;
    private PWIPlayerManager playerManager;

    public PlayerSpawnLocationListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.groupManager = plugin.getGroupManager();
        this.permissionManager = plugin.getPermissionManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        if (!Settings.getBoolean("load-data-on-join"))
            return;

        Player player = event.getPlayer();
        String spawnWorld = event.getSpawnLocation().getWorld().getName();

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' joining! Spawning in world '" + spawnWorld + "'. Getting last logout location");

        Location lastLogout = plugin.getSerializer().getLogoutData(player);
        if (lastLogout != null) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Logout location found for player '" + player.getName() + "'!");

            if (!lastLogout.getWorld().getName().equals(spawnWorld)) {
                Group spawnGroup = groupManager.getGroupFromWorld(spawnWorld);
                Group logoutGroup = groupManager.getGroupFromWorld(lastLogout.getWorld().getName());

                if (!spawnGroup.equals(logoutGroup)) {
                    if (Settings.getBoolean("debug-mode"))
                        PerWorldInventory.printDebug("Logout world groups are different! Saving data for player '" + player.getName() + "' for group '" + logoutGroup.getName() + "'");

                    playerManager.addPlayer(player, logoutGroup);

                    if (permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)) {
                        if (Settings.getBoolean("debug-mode"))
                            PerWorldInventory.printDebug("Player '" + player.getName() + "' has permission to bypass worlds. Returning");

                        return;
                    }

                    if (spawnGroup.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
                        return;
                    }

                    if (Settings.getBoolean("separate-gamemode-inventories")) {
                        if (Settings.getBoolean("debug-mode"))
                            PerWorldInventory.printDebug("Gamemodes are separated! Loading data for player '" + player.getName() + "' for group '" + spawnGroup.getName() + "' in gamemode '" + player.getGameMode().name() + "'");

                        playerManager.getPlayerData(spawnGroup, player.getGameMode(), player);

                        if (Settings.getBoolean("manage-gamemodes")) {
                            player.setGameMode(spawnGroup.getGameMode());
                        }
                    } else {
                        if (Settings.getBoolean("debug-mode"))
                            PerWorldInventory.printDebug("Loading data for player '" + player.getName() + "' for group '" + spawnGroup.getName() + "'");

                        playerManager.getPlayerData(spawnGroup, GameMode.SURVIVAL, player);
                    }
                }
            }
        }
    }
}

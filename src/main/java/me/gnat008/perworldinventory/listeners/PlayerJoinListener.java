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
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

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
        if (event.getPlayer().hasPermission("perworldinventory.notify") && Settings.getInt("config-version") < 1) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "Your PerWorldInventory config is out of date! Some options may be missing.");
            event.getPlayer().sendMessage(ChatColor.BLUE + "Copy the new options from here: " + ChatColor.WHITE + "https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        String spawnWorld = event.getSpawnLocation().getWorld().getName();

        Location lastLogout = plugin.getSerializer().getLogoutData(player);
        if (lastLogout != null) {
            if (!lastLogout.getWorld().getName().equals(spawnWorld)) {
                Group spawnGroup = groupManager.getGroupFromWorld(spawnWorld);
                Group logoutGroup = groupManager.getGroupFromWorld(lastLogout.getWorld().getName());

                if (!spawnGroup.equals(logoutGroup)) {
                    playerManager.addPlayer(player, logoutGroup);

                    if (event.getPlayer().hasPermission("perworldinventory.bypass")) {
                        return;
                    }

                    if (spawnGroup.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
                        return;
                    }

                    if (Settings.getBoolean("separate-gamemode-inventories")) {
                        playerManager.getPlayerData(spawnGroup, player.getGameMode(), player);

                        if (Settings.getBoolean("manage-gamemodes")) {
                            player.setGameMode(spawnGroup.getGameMode());
                        }
                    } else {
                        playerManager.getPlayerData(spawnGroup, GameMode.SURVIVAL, player);
                    }
                }
            }
        }
    }
}

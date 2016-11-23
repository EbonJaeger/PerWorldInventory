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

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataSource;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import javax.inject.Inject;

public class PlayerSpawnLocationListener implements Listener {

    private DataSource dataSource;
    private GroupManager groupManager;
    private InventoryChangeProcess process;
    private Settings settings;

    @Inject
    PlayerSpawnLocationListener(DataSource dataSource, GroupManager groupManager, InventoryChangeProcess process,
                                Settings settings) {
        this.dataSource = dataSource;
        this.groupManager = groupManager;
        this.process = process;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        if (!settings.getProperty(PwiProperties.LOAD_DATA_ON_JOIN))
            return;

        Player player = event.getPlayer();
        String spawnWorld = event.getSpawnLocation().getWorld().getName();

        PwiLogger.debug("Player '" + player.getName() + "' joining! Spawning in world '" + spawnWorld + "'. Getting last logout location");

        Location lastLogout = dataSource.getLogoutData(player);
        if (lastLogout != null) {
            PwiLogger.debug("Logout location found for player '" + player.getName() + "'!");

            if (!lastLogout.getWorld().getName().equals(spawnWorld)) {
                Group spawnGroup = groupManager.getGroupFromWorld(spawnWorld);
                Group logoutGroup = groupManager.getGroupFromWorld(lastLogout.getWorld().getName());

                process.processWorldChangeOnSpawn(player, logoutGroup, spawnGroup);
            }
        }
    }
}

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

package me.gnat008.perworldinventory.listeners;

import com.kill3rtaco.tacoserialization.PlayerSerialization;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    private WorldManager manager;
    private PerWorldInventory plugin;

    public PlayerChangedWorldListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.manager = plugin.getWorldManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldFrom = event.getFrom().getName();
        String worldTo = player.getWorld().getName();
        String groupFrom = manager.getGroupFromWorld(worldFrom);

        plugin.getSerializer().writePlayerDataToFile(player,
                PlayerSerialization.serializePlayer(player, plugin),
                groupFrom);

        if (!shouldKeepInventory(worldFrom, worldTo)) {
            plugin.getSerializer().getPlayerDataFromFile(player, manager.getGroupFromWorld(worldTo));
        }
    }

    private boolean shouldKeepInventory(String worldFrom, String worldTo) {
        return manager.getGroupFromWorld(worldFrom).contains(worldTo);
    }
}

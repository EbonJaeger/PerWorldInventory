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
import org.bukkit.GameMode;
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

        if (plugin.getConfigManager().getConfig("config").getBoolean("separate-gamemode-inventories")) {
            plugin.getSerializer().writePlayerDataToFile(player,
                    PlayerSerialization.serializePlayer(player, plugin),
                    groupFrom,
                    player.getGameMode().toString());
        } else {
            plugin.getSerializer().writePlayerDataToFile(player,
                    PlayerSerialization.serializePlayer(player, plugin),
                    groupFrom,
                    GameMode.SURVIVAL.toString());
        }

        if (!shouldKeepInventory(worldFrom, worldTo)) {
            if (plugin.getConfigManager().getConfig("config").getBoolean("separate-gamemode-inventories")) {
                if (plugin.getConfigManager().getConfig("config").getBoolean("manage-gamemodes")) {
                    plugin.getSerializer().getPlayerDataFromFile(player, manager.getGroupFromWorld(worldTo),
                            manager.getGameMode(manager.getGroupFromWorld(worldTo)).toString());
                    player.setGameMode(manager.getGameMode(manager.getGroupFromWorld(worldTo)));
                } else {
                    plugin.getSerializer().getPlayerDataFromFile(player, manager.getGroupFromWorld(worldTo), player.getGameMode().toString());
                }
            } else {
                plugin.getSerializer().getPlayerDataFromFile(player, manager.getGroupFromWorld(worldTo), GameMode.SURVIVAL.toString());
            }
        }
    }

    private boolean shouldKeepInventory(String worldFrom, String worldTo) {
        try {
            return manager.getGroup(manager.getGroupFromWorld(worldFrom)).contains(worldTo);
        } catch (NullPointerException ex) {
            return false;
        }
    }
}

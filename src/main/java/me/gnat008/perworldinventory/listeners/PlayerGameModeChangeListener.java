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

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerGameModeChangeListener implements Listener {

    private GroupManager manager;
    private PerWorldInventory plugin;

    public PlayerGameModeChangeListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGroupManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        Group group = manager.getGroupFromWorld(player.getWorld().getName());
        if (group == null) {
            if (manager.getGroup("__unconfigured__") != null) {
                group = manager.getGroup("__unconfigured__");
            } else {
                List<String> worlds = new ArrayList<>();
                worlds.add(player.getWorld().getName());
                group = new Group("__unconfigured__", worlds, GameMode.SURVIVAL);
            }
        }

        plugin.getPlayerManager().addPlayer(player, group);

        if (!player.hasPermission("perworldinventory.bypass.gamemode"))
            plugin.getPlayerManager().getPlayerData(group, newGameMode, player);
    }
}

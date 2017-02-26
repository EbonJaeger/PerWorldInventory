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

import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.process.GameModeChangeProcess;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import javax.inject.Inject;

public class PlayerGameModeChangeListener implements Listener {

    private BukkitService bukkitService;
    private GameModeChangeProcess process;
    private GroupManager groupManager;
    private PWIPlayerManager playerManager;

    @Inject
    PlayerGameModeChangeListener(BukkitService bukkitService, GameModeChangeProcess process,
                                 GroupManager groupManager, PWIPlayerManager playerManager) {
        this.bukkitService = bukkitService;
        this.process = process;
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Group group = groupManager.getGroupFromWorld(player.getWorld().getName());

        PwiLogger.debug("[GM PROCESS] Player '" + player.getName() + "' changed to GameMode '" +
                event.getNewGameMode().name() + "' in group '" + group.getName() + "'");

        playerManager.addPlayer(player, group);
        bukkitService.runTaskLater(() -> process.processGameModeChange(player, event.getNewGameMode(), group), 1L);
    }
}

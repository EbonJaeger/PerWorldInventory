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
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class PlayerGameModeChangeListener implements Listener {

    private GroupManager manager;
    private PermissionManager permissionManager;
    private PWIPlayerManager playerManager;

    public PlayerGameModeChangeListener(GroupManager manager, PermissionManager permissionManager, PWIPlayerManager playerManager) {
        this.manager = manager;
        this.permissionManager = permissionManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (!Settings.getBoolean("separate-gamemode-inventories"))
            return;

        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        Group group = manager.getGroupFromWorld(player.getWorld().getName());

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' changed to gamemode '" + newGameMode.name() + "' in group '" + group.getName() + "'");

        playerManager.addPlayer(player, group);

        if (!permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Player '" + player.getName() + "' does not have gamemode bypass permission! Loading data");

            playerManager.getPlayerData(group, newGameMode, player);
        }
    }
}

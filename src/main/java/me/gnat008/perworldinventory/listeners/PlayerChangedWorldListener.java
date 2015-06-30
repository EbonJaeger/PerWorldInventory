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
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.sql.SQLException;
import java.util.ArrayList;

public class PlayerChangedWorldListener implements Listener {

    private GroupManager manager;
    private PerWorldInventory plugin;

    public PlayerChangedWorldListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGroupManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldFrom = event.getFrom().getName();
        String worldTo = player.getWorld().getName();
        Group groupFrom = manager.getGroupFromWorld(worldFrom);
        Group groupTo = manager.getGroupFromWorld(worldTo);

        if (groupFrom == null) {
            groupFrom = new Group(worldFrom, new ArrayList<String>(), null);
        }

        if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
            if (ConfigValues.USE_MYSQL.getBoolean()) {
                try {
                    plugin.getMySQLManager().updateDatabase(
                            player.getUniqueId().toString(),
                            groupFrom.getName(),
                            player.getGameMode().toString().toLowerCase(),
                            plugin.getSerializer().serializeForMySQL(PlayerSerialization.serializePlayer(player, plugin)));
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Error trying to save player data to database: " + ex.getMessage());
                    plugin.getLogger().severe("Saving to flatfile instead!");
                    plugin.getSerializer().writePlayerDataToFile(player,
                            PlayerSerialization.serializePlayer(player, plugin),
                            groupFrom,
                            player.getGameMode());
                }
            } else {
                plugin.getSerializer().writePlayerDataToFile(player,
                        PlayerSerialization.serializePlayer(player, plugin),
                        groupFrom,
                        player.getGameMode());
            }
        } else {
            if (ConfigValues.USE_MYSQL.getBoolean()) {
                try {
                    plugin.getMySQLManager().updateDatabase(
                            player.getUniqueId().toString(),
                            groupFrom.getName(),
                            GameMode.SURVIVAL.toString().toLowerCase(),
                            plugin.getSerializer().serializeForMySQL(PlayerSerialization.serializePlayer(player, plugin)));
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Error trying to save player data to database: " + ex.getMessage());
                    plugin.getLogger().severe("Saving to flatfile instead!");
                    plugin.getSerializer().writePlayerDataToFile(player,
                            PlayerSerialization.serializePlayer(player, plugin),
                            groupFrom,
                            GameMode.SURVIVAL);
                }
            } else {
                plugin.getSerializer().writePlayerDataToFile(player,
                        PlayerSerialization.serializePlayer(player, plugin),
                        groupFrom,
                        GameMode.SURVIVAL);
            }
        }

        if (!groupFrom.containsWorld(worldTo)) {
            if (groupTo == null) {
                groupTo = new Group(worldTo, null, GameMode.SURVIVAL);
            }

            if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
                if (ConfigValues.MANAGE_GAMEMODES.getBoolean()) {
                    plugin.getSerializer().getPlayerDataFromFile(player, groupTo,
                            groupTo.getGameMode());
                    player.setGameMode(groupTo.getGameMode());
                } else {
                    plugin.getSerializer().getPlayerDataFromFile(player, groupTo, player.getGameMode());
                }
            } else {
                plugin.getSerializer().getPlayerDataFromFile(player, groupTo, GameMode.SURVIVAL);
            }
        }
    }
}

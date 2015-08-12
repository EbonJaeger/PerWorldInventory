/*
 * Copyright (C) 2014-2015  Erufael
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

package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is used to manage cached players.
 * Players are meant to be added when data needs to be saved, and removed
 * when the data has been saved to the database, whether it be MySQL or
 * flat files.
 */
public class PWIPlayerManager {

    private static PWIPlayerManager instance = null;

    private PerWorldInventory plugin;
    private Map<UUID, PWIPlayer> playerCache = new HashMap<>();

    private PWIPlayerManager() {
        this.plugin = PerWorldInventory.getInstance();
    }

    /**
     * Get the Singleton PWIPlayerManager instance. If one has not been
     * initialized, then it will be initialized.
     *
     * @return PWIPlayerManager
     */
    public static PWIPlayerManager getInstance() {
        if (instance == null) {
            instance = new PWIPlayerManager();
        }

        return instance;
    }

    /**
     * Sets the Singleton instance to null. Called during onDisable().
     */
    public void disable() {
        instance = null;
    }

    /**
     * Add a new player to the cache.
     *
     * @param player The Player to add
     */
    public void addPlayer(Player player) {
        playerCache.put(player.getUniqueId(), new PWIPlayer(player));
    }

    /**
     * Removes a player from the cache. This should be called when the data has been
     * written to the database so we don't use up unnecessary amounts of memory.
     *
     * @param uuid The UUID of the player to remove
     */
    public void removePlayer(UUID uuid) {
        playerCache.remove(uuid);
    }

    /**
     * Get a PWI player from a UUID.
     * This method will return null if no player is found.
     *
     * @param uuid The UUID of the player to get
     * @return The PWIPlayer
     */
    public PWIPlayer getPlayer(UUID uuid) {
        return playerCache.get(uuid) != null ? playerCache.get(uuid) : null;
    }
}

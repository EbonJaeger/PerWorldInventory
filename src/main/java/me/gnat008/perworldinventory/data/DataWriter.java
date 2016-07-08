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

package me.gnat008.perworldinventory.data;

import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface DataWriter {

    /**
     * Save the location of a player when they log out or are kicked from the server.
     *
     * @param player The player who logged out
     */
    void saveLogoutData(PWIPlayer player);

    /**
     * Saves a player's data to the database.
     * <p>
     * The database used will be different depending on the config,
     * either flatfile (.yml), or MySQL.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} the player was in
     * @param gamemode The {@link org.bukkit.GameMode} the player was in
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} to save
     * @param async Save data asynchronously
     */
    void saveToDatabase(final Group group, final GameMode gamemode, final PWIPlayer player, boolean async);

    void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player);

    /**
     * Retrieves a player's data from the database.
     * <p>
     * The database used will be different depending on the config,
     * either flatfile (.yml), or MySQL.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} the player was in
     * @param gamemode The {@link org.bukkit.GameMode} the player was in
     * @param player The {@link org.bukkit.entity.Player} to set the data to
     */
    void getFromDatabase(Group group, GameMode gamemode, Player player);

    /**
     * Get the name of the world that a player logged out in.
     * If this is their first time logging in, this method will return null instead of a location.
     *
     * @param player The player to get the last logout for
     * @return The location of the player when they last logged out or null
     */
    Location getLogoutData(Player player);
}

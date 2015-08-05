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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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
}

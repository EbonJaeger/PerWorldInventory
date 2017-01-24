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

package me.gnat008.perworldinventory.groups;

import org.bukkit.GameMode;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A group of worlds, typically defined in the worlds.yml file.
 * Each Group has a name, and should have a list of worlds in that group, as well as
 * a default GameMode.
 */
public class Group {

    private String name;
    private Set<String> worlds;
    private GameMode gameMode;
    private boolean configured;

    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     */
    public Group(String name, Set<String> worlds, GameMode gameMode) {
        this.name = name;
        this.worlds = worlds;
        this.gameMode = gameMode;
        this.configured = false;
    }

    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     * @param configured If the group is defined in the worlds.yml file.
     */
    public Group(String name, Set<String> worlds, GameMode gameMode, boolean configured) {
        this.name = name;
        this.worlds = worlds;
        this.gameMode = gameMode;
        this.configured = configured;
    }

    /**
     * Get the default {@link GameMode} of this group.
     *
     * @return The default GameMode.
     */
    public GameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * Get a list of the names of all the worlds in this group.
     *
     * @return A List of world names.
     */
    public Set<String> getWorlds() {
        return this.worlds;
    }

    /**
     * Get the name of this group.
     *
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get whether this group contains a world with a given name.
     *
     * @param world The name of the world to check for.
     *
     * @return True if the world is in this group.
     */
    public boolean containsWorld(String world) {
        return this.worlds.contains(world);
    }

    /**
     * Add a list of worlds to this group.
     *
     * @param worlds A list of the worlds to add.
     */
    public void addWorlds(Collection<String> worlds) {
        for (String world : worlds)
            if (!this.worlds.contains(world))
                this.worlds.add(world);
    }

    /**
     * Add a world to this group.
     *
     * @param world The name of the world to add.
     */
    public void addWorld(String world) {
        this.worlds.add(world);
    }

    /**
     * Get whether this group is defined in the worlds.yml file.
     *
     * @return True if it is defined in the file.
     */
    public boolean isConfigured() {
        return this.configured;
    }

    /**
     * Set whether this group is defined in the worlds.yml file.
     *
     * @param configured If this group is defined.
     */
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }
}

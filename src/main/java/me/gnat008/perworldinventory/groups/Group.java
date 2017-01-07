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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * A group of worlds, typically defined in the worlds.yml file.
 * Each Group has a name, and should have a list of worlds in that group, as well as
 * a default GameMode.
 */
public class Group {

    private String name;
    private List<String> worlds;
    private GameMode gameMode;
    private Set<String> useLastWorld;
    private Set<String> useLastPosInGroup;
    private Set<String> useLastPosInWorld;
    private String defaultWorld;
    private boolean configured;

    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     */
    public Group(String name, List<String> worlds, GameMode gameMode) {
        this(name,worlds,null,null,null,null,gameMode,false);
    }

    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     * @param configured If the group is defined in the worlds.yml file.
     */
    public Group(String name, List<String> worlds, GameMode gameMode, boolean configured) {
        this(name,worlds,null,null,null,null,gameMode,configured);
    }


    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     * @param useLastWorld A {@link List} of teleport causes that should map to the last world in group
     * @param usePosInWorld A {@link List} of teleport causes that should map to the last position in world
     */
    public Group(String name, List<String> worlds, List<String> useLastWorld, List<String> useLastPosInGroup, List<String> useLastPosInWorld, String defaultWorld, GameMode gameMode) {
        this(name,worlds,useLastWorld,useLastPosInGroup,useLastPosInWorld,defaultWorld,gameMode,false);
    }

    /**
     * Constructor.
     *
     * @param name The name of the group.
     * @param worlds A list of world names in this group.
     * @param gameMode The default {@link GameMode} for this group.
     * @param useLastWorld A {@link List} of teleport causes that should map to the last world in group
     * @param usePosInWorld A {@link List} of teleport causes that should map to the last position in world
     * @param configured If the group is defined in the worlds.yml file.
     */
    public Group(String name, List<String> worlds, List<String> useLastWorld, List<String> useLastPosInGroup, List<String> useLastPosInWorld, String defaultWorld, GameMode gameMode, boolean configured) {
        this.name = name;
        this.worlds = worlds;
        this.gameMode = gameMode;
        this.useLastWorld = useLastWorld!=null?new HashSet<String> (useLastWorld):new HashSet<String> ();
        this.useLastPosInGroup = useLastPosInGroup!=null?new HashSet<String> (useLastPosInGroup):new HashSet<String> ();
        this.useLastPosInWorld = useLastPosInWorld!=null?new HashSet<String> (useLastPosInWorld):new HashSet<String> ();
        this.defaultWorld = defaultWorld;
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
     * Get if a cause should enforce moving to the last known of world the player was in this group.
     *
     * @param cause the string representation of the cause that we check.
     *
     * @return True if enforce last world, false if not.
     */
    public boolean shouldUseLastWorld(String cause) {
        return this.useLastWorld.contains(cause);
    }

    /**
     * Get if a cause should enforce moving to the last known of world the player was in this group.
     *
     * @return A copy of the internal configuration map
     */
    public List<String> shouldUseLastWorld() {
        return new ArrayList<String>(this.useLastWorld);
    }

    /**
     * Get if moving to last known position should be enforced when changing
     * world into this group from another.
     *
     * @param cause the string representation of the cause that we check.
     *
     * @return True if enforce last position, false if not.
     */
    public boolean shouldUseLastPosInGroup(String cause) {
        return this.useLastPosInGroup.contains(cause);
    }

    /**
     * Get if moving to last known position should be enforced when changing
     * world into this group from another.
     *
     * @return A copy of the internal configuration map
     */
    public List<String> shouldUseLastPosInGroup() {
        return new ArrayList<String>(this.useLastPosInGroup);
    }

    /**
     * Get if moving to last known position should be enforced when changing
     * worlds within this group.
     *
     * @param cause the string representation of the cause that we check.
     *
     * @return True if enforce last position, false if not.
     */
    public boolean shouldUseLastPosInWorld(String cause) {
        return this.useLastPosInWorld.contains(cause);
    }

    /**
     * Get if moving to last known position should be enforced when changing
     * worlds within this group.
     *
     * @return A copy of the internal configuration map
     */
    public List<String> shouldUseLastPosInWorld() {
        return new ArrayList<String>(this.useLastPosInWorld);
    }

    /**
     * Set if moving to last known world should be enforced in this group.
     * For specific cause. Atomic update
     *
     * @param cause the string representation of the cause that we check.
     * @param value should we enforce world?
     */
    public void setUseLastWorld(String cause, boolean value) {
        if(value)
            this.useLastWorld.add(cause);
        else
            this.useLastWorld.remove(cause);
    }


    /**
     * Set if moving to last known position should be enforced when changing
     * worlds within this group from.
     * Will overwrtite all previous causes.
     *
     * @param list A map of all permissions to use. This will overwri
     */
    public void setUseLastWorld (List<String> list) {
        this.useLastWorld = new HashSet<String>(list);
    }

    /**
     * Set if moving to last known position should be enforced when changing
     * world into this group from another.
     * For specific cause. Atomic update
     *
     * @param cause the string representation of the cause that we check.
     * @param value should we enforce position?
     */
    public void setUseLastPosInGroup(String cause, boolean value) {
        if(value)
            this.useLastPosInGroup.add(cause);
        else
            this.useLastPosInGroup.remove(cause);
    }

    /**
     * Set if moving to last known position should be enforced when changing
     * world into this group from another.
     * Will overwrtite all previous causes.
     *
     * @param cause the string representation of the caus that we check.
     * @param list should we enforce position?
     */
    public void setUseLastPosInGroup (List<String> list) {
        this.useLastPosInGroup = new HashSet<String>(list);
    }

    /**
     * Set if moving to last known position should be enforced when changing
     * world into this group from another.
     * For specific cause. Atomic update
     *
     * @param cause the string representation of the cause that we check.
     * @param value should we enforce position?
     */
    public void setUseLastPosInWorld(String cause, boolean value) {
        if(value)
            this.useLastPosInWorld.add(cause);
        else
            this.useLastPosInWorld.remove(cause);
    }

    /**
     * Set if moving to last known position should be enforced when changing
     * worlds within this group.
     * Will overwrtite all previous causes.
     *
     * @param cause the string representation of the caus that we check.
     * @param list should we enforce position?
     */
    public void setUseLastPosInWorld (List<String> list) {
        this.useLastPosInWorld = new HashSet<String>(list);
    }


    /**
     * Get a list of the names of all the worlds in this group.
     *
     * @return A List of world names.
     */
    public List<String> getWorlds() {
        return this.worlds;
    }

    /**
     * Get the defoult world for this group.
     *
     * @return A List of world names.
     */
    public String getDefaultWorld() {
        return this.defaultWorld;
    }

    /**
     * Get the defoult world for this group.
     *
     * @return A List of world names.
     */
    public void setDefaultWorld(String defaultWorld) {
        this.defaultWorld = defaultWorld;
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
    public void addWorlds(List<String> worlds) {
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

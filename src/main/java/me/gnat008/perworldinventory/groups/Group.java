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

import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * A group of worlds, typically defined in the worlds.yml file.
 * Each Group has a name, and should have a list of worlds in that group, as well as
 * a default GameMode.
 */
public class Group {

    private String name;
    private List<String> worlds;
    private GameMode gameMode;
    private Set<TeleportCause> enableLastWorldCauses;
    private Set<TeleportCause> enableLastPosToGroupCauses;
    private Set<TeleportCause> enableLastPosWithinGroupCauses;
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
        this(name, worlds, null, null, null, null, gameMode,false);
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
        this(name,worlds,null,null, null, null, gameMode, configured);
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
    public Group(String name, List<String> worlds, String defaultWorld, Set<TeleportCause> enableLastWorldCauses, Set<TeleportCause> enableLastPosToGroupCauses, Set<TeleportCause> enableLastPosWithinGroupCauses, GameMode gameMode) {
        this(name, worlds, defaultWorld, enableLastWorldCauses, enableLastPosToGroupCauses, enableLastPosWithinGroupCauses, gameMode, false);
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
    public Group(String name, List<String> worlds, String defaultWorld, Set<TeleportCause> enableLastWorldCauses, Set<TeleportCause> enableLastPosToGroupCauses, Set<TeleportCause> enableLastPosWithinGroupCauses, GameMode gameMode, boolean configured) {
        this.name = name;
        this.worlds = worlds;
        this.gameMode = gameMode;

        this.enableLastWorldCauses = enableLastWorldCauses != null
          ? new HashSet<>(enableLastWorldCauses) : new HashSet<>();

        this.enableLastPosToGroupCauses = enableLastPosToGroupCauses != null
          ? new HashSet<>(enableLastPosToGroupCauses) : new HashSet<>();

        this.enableLastPosWithinGroupCauses = enableLastPosWithinGroupCauses != null
          ? new HashSet<>(enableLastPosWithinGroupCauses) : new HashSet<>();

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
     * Get a list of the names of all the worlds in this group.
     *
     * @return A List of world names.
     */
    public List<String> getWorlds() {
        return this.worlds;
    }

    /**
     * Get the default world for this group.
     *
     * @return A List of world names.
     */
    public String getDefaultWorld() {
        return this.defaultWorld;
    }

    /**
     * Set the default world for this group.
     *
     * @param defaultWorld The default world.
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

    /**
     * Check if a cause should redirect player to last known world when changing
     * world <b>into</b> group.
     *
     * @param  cause The {@link TeleportCause} to check.
     * @return whether to use the last known world when switching to this group.
     */
    public boolean shouldUseLastWorld(TeleportCause cause) {
        return this.enableLastWorldCauses.contains(cause);
    }

    /**
     * Get a set of all causes that will cause the player to be redirected to
     * last known world when changing <b>into</b> this group.
     *
     * @return A set of causes that cause the player to be redirected.
     */
    public Set<TeleportCause> shouldUseLastWorldCauses() {
        return new HashSet<>(this.enableLastWorldCauses);
    }

    /**
     * Check if a cause should redirect player to last known position in target
     * world when changing world <b>into</b> this group.
     *
     * @param  cause The {@link TeleportCause} to check.
     * @return whether to use the last position in world when switching to this group.
     */
    public boolean shouldUseLastPosToGroup(TeleportCause cause) {
        return this.enableLastPosToGroupCauses.contains(cause);
    }

    /**
     * Get a set of all causes that will cause the player to be redirected to
     * last known position in target world when changing <b>into</b> this group.
     *
     * @return A set of causes that cause the player to be redirected.
     */
    public Set<TeleportCause> shouldUseLastPosToGroupCauses() {
        return new HashSet<TeleportCause>(this.enableLastPosToGroupCauses);
    }

    /**
     * Check if a cause should redirect player to last known position in target
     * world when changing world <b>within</b> this group.
     *
     * @param  cause The {@link TeleportCause} to check.
     * @return whether to use the last position in world when switching within this group.
     */
    public boolean shouldUseLastPosWithinGroup(TeleportCause cause) {
        return this.enableLastPosWithinGroupCauses.contains(cause);
    }

    /**
     * Get a set of all causes that will cause the player to be redirected to
     * last known position in target world when changing <b>within</b> this group.
     *
     * @return A set of causes that cause the player to be redirected.
     */
    public Set<TeleportCause> shouldUseLastPosWithinGroupCauses() {
        return new HashSet<>(this.enableLastPosWithinGroupCauses);
    }

    /**
     * Set wether a player teleporting into this group should be redirected
     * to last known world for a specific cause.
     *
     * @param cause The {@link TeleportCause} to set.
     * @param enabled Wether to enable or disable the redirect
     */
    public void setLastWorldEnabled(TeleportCause cause, boolean enabled) {
        if (enabled)
            this.enableLastWorldCauses.add(cause);
        else
            this.enableLastWorldCauses.remove(cause);
    }

    /**
     * Replace all causes that should redirect the player to the last known
     * word when changing world <b>into</b> this group.
     *
     * @param causes a {@link Set] of new {@link TeleportCause}s to use.
     */
    public void replaceLastWorldCauses(Set<TeleportCause> causes) {
        this.enableLastWorldCauses = new HashSet<>(causes);
    }

    /**
     * Set wether a player teleporting <b>into</b> this group should be redirected
     * to last known position in target world for a specific cause.
     *
     * @param cause The {@link TeleportCause} to set.
     * @param enabled Wether to enable or disable the redirect
     */
    public void setLastPosToGroupEnabled(TeleportCause cause, boolean enabled) {
        if (enabled)
            this.enableLastPosToGroupCauses.add(cause);
        else
            this.enableLastPosToGroupCauses.remove(cause);
    }

    /**
     * Replace all causes that should redirect the player to the last known
     * position in target world when changing world <b>into</b> this group.
     *
     * @param causes a {@link Set] of new {@link TeleportCause}s to use.
     */
    public void replaceLastPosToGroupCauses(Set<TeleportCause> causes) {
        this.enableLastPosToGroupCauses = new HashSet<>(causes);
    }

    /**
     * Set wether a player teleporting <b>within</b> this group should be redirected
     * to last known position in target world for a specific cause.
     *
     * @param cause The {@link TeleportCause} to set.
     * @param enabled Wether to enable or disable the redirect
     */
    public void setLastPosWithinGroupEnabled(TeleportCause cause, boolean enabled) {
        if (enabled)
            this.enableLastPosWithinGroupCauses.add(cause);
        else
            this.enableLastPosWithinGroupCauses.remove(cause);
    }

    /**
     * Replace all causes that should redirect the player to the last known
     * position in target world when changing world <b>within</b> this group.
     *
     * @param causes a {@link Set] of new {@link TeleportCause}s to use.
     */
    public void replaceLastPosWithinGroupCauses(Set<TeleportCause> causes) {
        this.enableLastPosWithinGroupCauses = new HashSet<>(causes);
    }
}

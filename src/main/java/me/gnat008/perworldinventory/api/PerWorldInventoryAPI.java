package me.gnat008.perworldinventory.api;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * This class is for other plugin developers to access parts of PWI.
 */
public class PerWorldInventoryAPI {

    @Inject
    private GroupManager groupManager;
    @Inject
    private PermissionManager permissionManager;
    @Inject
    private PerWorldInventory plugin;
    @Inject
    private PWIPlayerManager playerManager;
    @Inject
    private Settings settings;

    /**
     * Constructor
     */
    PerWorldInventoryAPI() {
    }

    /**
     * Check if two worlds are a part of the same group, and thus
     * can share the same inventory. If one of the groups is not configured
     * in the worlds.yml, this method will return true if the option for sharing
     * inventories between non-configured worlds is true in the config.yml file.
     *
     * @param first The name of first world.
     * @param second The name of the other world to check.
     *
     * @return True if both worlds are in the same group, or one group is not configured and the option
     * to share is set to true.
     */
    public boolean canWorldsShare(String first, String second) {
        Group firstGroup = groupManager.getGroupFromWorld(first);
        Group otherGroup = groupManager.getGroupFromWorld(second);

        if (!firstGroup.isConfigured() || !otherGroup.isConfigured()) {
            return firstGroup.containsWorld(second) || settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED);
        } else {
            return firstGroup.containsWorld(second);
        }
    }

    /**
     * Get whether a player is currently cached as a {@link PWIPlayer} for a given Group.
     *
     * @param group The group to check if the player is cached for.
     * @param gameMode The gamemode the player was in.
     * @param player The player to check for.
     *
     * @return True if the player exists in the cache with the given parameters.
     */
    public boolean isPlayerCached(Group group, GameMode gameMode, Player player) {
        return playerManager.isPlayerCached(group, gameMode, player);
    }

    /**
     * Get a {@link Group} by name. If no group with that name exists, this
     * method will return null.
     *
     * @param name The name of the group.
     *
     * @return The group if it exists, null otherwise.
     */
    public Group getGroup(String name) {
        return groupManager.getGroup(name);
    }

    /**
     * Get the {@link Group} that a given world is in. This method will always
     * return a Group, even if the world is not in any groups in the worlds.yml file.
     * In that case, a new group will be created that contains this world, and possible Nether and End
     * worlds with the same name, and that group will be returned.
     *
     * @param worldName The name of the world to get the Group from.
     *
     * @return The Group that the world is in.
     */
    public Group getGroupFromWorld(String worldName) {
        return groupManager.getGroupFromWorld(worldName);
    }

    /**
     * Get a {@link PWIPlayer} from the cache for a given group, if one exists. This method
     * will use the current GameMode of the given player when searching for a cached version.
     * If no cached player for this group and GameMode exists, this method will return null.
     *
     * @param group The Group to look for a cached player in.
     * @param player The player to find a cached version for.
     *
     * @return The cached PWIPlayer if found, null otherwise.
     */
    public PWIPlayer getCachedPlayer(Group group, Player player) {
        return getCachedPlayer(group, player.getGameMode(), player);
    }

    /**
     * Get a {@link PWIPlayer} for a given group, if one exists. If no cached player exists,
     * this method will return null.
     *
     * @param group The Group to look for a cached player in.
     * @param gameMode The GameMode that player was in.
     * @param player The player to find a cached version for.
     *
     * @return The cached PWIPlayer if found, null otherwise.
     * @since 1.10.0
     */
    public PWIPlayer getCachedPlayer(Group group, GameMode gameMode, Player player) {
        return playerManager.getPlayer(group, gameMode, player);
    }

    public String createCachedPlayer(Player player, Group group) {
        return playerManager.addPlayer(player, group);
    }
}

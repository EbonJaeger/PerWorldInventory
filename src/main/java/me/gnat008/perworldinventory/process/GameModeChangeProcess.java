package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import javax.inject.Inject;

public class GameModeChangeProcess {

    @Inject
    private GroupManager groupManager;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    private PWIPlayerManager playerManager;

    @Inject
    private Settings settings;

    GameModeChangeProcess() {
    }

    /**
     * Process a player's GameMode changing.
     *
     * @param event The {@link PlayerGameModeChangeEvent} that is happening.
     */
    public void processGameModeChange(PlayerGameModeChangeEvent event) {
        if (!settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)) {
            return;
        }

        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        Group group = groupManager.getGroupFromWorld(player.getWorld().getName());

        PerWorldInventory.printDebug("Player '" + player.getName() + "' changed to gamemode '" + newGameMode.name() + "' in group '" + group.getName() + "'");

        playerManager.addPlayer(player, group);

        if (settings.getProperty(PwiProperties.DISABLE_BYPASS)) {
            PerWorldInventory.printDebug("Bypass system is disabled in the config, loading data");

            playerManager.getPlayerData(group, newGameMode, player);
        } else {
            if (!permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)) {
                PerWorldInventory.printDebug("Player '" + player.getName() + "' does not have gamemode bypass permission! Loading data");

                playerManager.getPlayerData(group, newGameMode, player);
            } else {
                PerWorldInventory.printDebug("Player '" + player.getName() + "' has gamemode bypass permission!");
            }
        }
    }
}

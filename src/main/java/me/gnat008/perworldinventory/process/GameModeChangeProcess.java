package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.ConsoleLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import me.gnat008.perworldinventory.events.InventoryLoadEvent;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class GameModeChangeProcess {

    @Inject
    private BukkitService bukkitService;
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
     * @param player The player who's GameMode is changing.
     * @param newGameMode The player's new GameMode.
     * @param group The {@link Group} the player is in.
     */
    public void processGameModeChange(Player player, GameMode newGameMode, Group group) {
        if (!settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)) {
            return;
        }

        InventoryLoadEvent event = new InventoryLoadEvent(player, DeserializeCause.GAMEMODE_CHANGE, newGameMode, group);

        if (settings.getProperty(PwiProperties.DISABLE_BYPASS)) {
            ConsoleLogger.debug("[GM PROCESS] Bypass system is disabled in the config, loading data");

            bukkitService.callEvent(event);
        } else {
            if (!permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)) {
                ConsoleLogger.debug("[GM PROCESS] Player '" + player.getName() + "' does not have GameMode bypass permission! Loading data");

                bukkitService.callEvent(event);
            } else {
                ConsoleLogger.debug("[GM PROCESS] Player '" + player.getName() + "' has GameMode bypass permission!");
            }
        }
    }
}

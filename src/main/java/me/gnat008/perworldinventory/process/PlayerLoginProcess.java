package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.metadata.PWIPlayerLastLocInWorldData;
import me.gnat008.perworldinventory.data.metadata.PWIPlayerLastWorldInGroupdData;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * Handle a player joiving the server.
 */
public class PlayerLoginProcess {

    PlayerLoginProcess() {
    }

    /**
     * Preapare the players metadata upon joining the server.
     *
     * @param player The player leaving.
     */
    public void processPlayerLogin(Player player) {
        player.setMetadata("lastLocInWorld", new PWIPlayerLastLocInWorldData(player));
        player.setMetadata("lastWorldInGroup", new PWIPlayerLastWorldInGroupData(player));
    }
}

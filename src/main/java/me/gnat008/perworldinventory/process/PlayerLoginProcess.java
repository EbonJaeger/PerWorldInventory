package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.metadata.PWIPlayerLastLocationInWorldData;
import me.gnat008.perworldinventory.data.metadata.PWIPlayerLastWorldInGroupData;
import me.gnat008.perworldinventory.data.metadata.PWIMetaDataManager;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * Handle a player joiving the server.
 */
public class PlayerLoginProcess {

    public PlayerLoginProcess() {
    }

    @Inject PWIMetaDataManager metaDataManager;
    /**
     * Preapare the players metadata upon joining the server.
     *
     * @param player The player leaving.
     */
    public void processPlayerLogin(Player player) {
        player.setMetadata("lastLocationInWorld", metaDataManager.<PWIPlayerLastLocationInWorldData>createMetadataValue(PWIPlayerLastLocationInWorldData.class,player));
        player.setMetadata("lastWorldInGroup", metaDataManager.<PWIPlayerLastWorldInGroupData>createMetadataValue(PWIPlayerLastWorldInGroupData.class,player));
    }
}

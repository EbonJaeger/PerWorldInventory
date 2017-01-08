package me.gnat008.perworldinventory.data.metadata;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.DataWriter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PWIPlayerLastLocationInWorldData extends PWIMetaDataValueAbstract<Map<String,Location>> {

    // I don't store the entire player object. I've read somewhere that it may
    // cause memleaks if you're not careful with dereferencing the player.
    private UUID playerUUID;

    private Map<String, Location> cache;

    private DataWriter dataWriter;

    public PWIPlayerLastLocationInWorldData(PerWorldInventory plugin, DataWriter dataWriter, Player player) {
        super(plugin);
        this.dataWriter = dataWriter;
        this.playerUUID = player.getUniqueId();
    }

    @Override
    public Map<String, Location> value() {
        if (this.cache == null)
            this.cache = dataWriter.getLastLocationInWorld(playerUUID);
        if (this.cache == null)
            this.cache = new HashMap<String, Location>();
        return this.cache;
    }

    @Override
    public void invalidate() {
        PwiLogger.debug("Invalidating lastlocationinworlddata for player...");
        dataWriter.saveLastLocationInWorld(playerUUID, value());
        this.cache = null;
    }
}

package me.gnat008.perworldinventory.data.metadata;

import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.PerWorldInventory;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.lang.IllegalArgumentException;

import javax.inject.Inject;

public class PWIPlayerLastLocationInWorldData extends PWIMetaDataValueAbstract<Map<String,Location>> {

    // I don't store the entire player object. I've read somewhere that it may
    // cause memleaks if you're not careful with dereferencing the player.
    private UUID playerUUID;

    private Map<String,Location> cache;

    private DataWriter dataWriter;

    @Inject
    public PWIPlayerLastLocationInWorldData(PerWorldInventory plugin, DataWriter dataWriter) {
        super(plugin);
        this.dataWriter = dataWriter;
    }

    public void init(Object... args) {
        if(args.length != 1) {
            throw new IllegalArgumentException("Illegal number of arguments to init. Expected 1, got "+args.length);
        }
        if(!(args[0] instanceof Player)) {
            throw new IllegalArgumentException("Illegal arguments to init. Expected Player, got "+args[0].getClass());
        }
        this.playerUUID = ((Player)args[0]).getUniqueId();
    }

    @Override
    public Map<String,Location> value(){
        if(this.cache == null)
            this.cache = dataWriter.getLastLocationInWorld(playerUUID);
        if(this.cache == null)
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

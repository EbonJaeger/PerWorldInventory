package me.gnat008.perworldinventory.data.metadata;

import java.util.HashMap;
import java.util.Map;
import me.gnat008.perworldinventory.data.DataWriter;
import javax.inject.Inject;

public abstract class PWIPlayerLastWorldInGroupData extends PWIMetaDataValueAbstract<Map<String,Location>> {

    // I don't store the entire player object. I've read somewhere that it may
    // cause memleaks if you're not careful with dereferencing the player.
    private String playerUUID;

    private Map<String,Location> cache;

    @Inject
    private DataWriter dataWriter;

    public PWIPlayerLastWorldInGroupData(Player player) {
        super(plugin);
        this.playerUUID = player.getUniqueId();
    }

    public PWIPlayerLastWorldInGroupData(String playerUUID) {
        super(plugin);
        this.playerUUID = playerUUID;
    }

    @Override
    public Map<String,Location> value(){
        if(this.cache == null)
            this.cache = dataWriter.getLastLocInWorld(playerUUID);
        if(this.cache == null)
            this.cache = new HashMap<String, Location>();
        return this.cache;
    }

    @Override
    public void invalidate() {
        if(this.cache == null) {
            return;
        }
        dataWriter.saveLastLocInWorld(playerUUID, cache);
        this.cache = null;
    }
}

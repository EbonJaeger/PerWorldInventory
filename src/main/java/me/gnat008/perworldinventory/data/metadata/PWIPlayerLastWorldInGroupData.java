package me.gnat008.perworldinventory.data.metadata;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.DataWriter;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PWIPlayerLastWorldInGroupData extends PWIMetaDataValueAbstract<Map<String,String>> {
    // I don't store the entire player object. I've read somewhere that it may
    // cause memleaks if you're not careful with dereferencing the player.
    private UUID playerUUID;

    private Map<String, String> cache;

    private DataWriter dataWriter;

    @Inject
    public PWIPlayerLastWorldInGroupData(PerWorldInventory plugin, DataWriter dataWriter) {
        super(plugin);
        this.dataWriter = dataWriter;
    }

    public void init(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Illegal number of arguments to init. Expected 1, got " + args.length);
        }
        if (!(args[0] instanceof Player)) {
            throw new IllegalArgumentException("Illegal arguments to init. Expected Player, got " + args[0].getClass());
        }
        this.playerUUID = ((Player) args[0]).getUniqueId();
    }

    @Override
    public Map<String, String> value() {
        if (this.cache == null)
            this.cache = dataWriter.getLastWorldInGroup(playerUUID);
        if (this.cache == null)
            this.cache = new HashMap<String, String>();
        return this.cache;
    }

    @Override
    public void invalidate() {
        dataWriter.saveLastWorldInGroup(playerUUID, value());
        this.cache = null;
    }
}

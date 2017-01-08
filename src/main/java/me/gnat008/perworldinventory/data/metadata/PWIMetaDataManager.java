package me.gnat008.perworldinventory.data.metadata;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.DataWriter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class PWIMetaDataManager {

    @Inject
    private PerWorldInventory plugin;

    @Inject
    private DataWriter dataWriter;

    PWIMetaDataManager() {
    }

    public Map<String, Location> getLastLocationInWorldMap(Player player) {
        final String _KEY_ = "lastLocationInWorld";

        List<MetadataValue> dataList = player.getMetadata(_KEY_);
        PWIPlayerLastLocationInWorldData metadata = null;
        for (MetadataValue metadataSearch : dataList) {
            if(plugin.equals(metadataSearch.getOwningPlugin())) {
                metadata = (PWIPlayerLastLocationInWorldData) metadataSearch;
                break;
            }
        }
        // Create the metadata if it doesn't exists...
        if(metadata == null) {
            metadata = new PWIPlayerLastLocationInWorldData(plugin, dataWriter, player);
            player.setMetadata(_KEY_,metadata);
        }
        return metadata.value();
    }

    public Map<String, String> getLastWorldInGroupMap(Player player) {
        final String _KEY_ = "lastWorldInGroup";

        List<MetadataValue> dataList = player.getMetadata(_KEY_);
        PWIPlayerLastWorldInGroupData metadata = null;
        for (MetadataValue metadataSearch : dataList) {
            if(plugin.equals(metadataSearch.getOwningPlugin())) {
                metadata = (PWIPlayerLastWorldInGroupData) metadataSearch;
                break;
            }
        }
        // Create the metadata if it doesn't exists...
        if(metadata == null) {
            metadata = new PWIPlayerLastWorldInGroupData(plugin, dataWriter, player);
            player.setMetadata(_KEY_,metadata);
        }
        return metadata.value();
    }
}

package me.gnat008.perworldinventory.data.metadata;

import ch.jalu.injector.Injector;
import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import javax.inject.Inject;
import java.util.List;

public class PWIMetaDataManager {

    @Inject
    private PerWorldInventory plugin;
    @Inject
    private Injector injector;

    PWIMetaDataManager() {
    }

    public <T extends PWIMetaDataValueAbstract> T createMetadataValue(Class<T> clazz, Object... args) {
        T instance = injector.newInstance(clazz);
        instance.init(args);
        return instance;
    }

    public <T> T getFromPlayer(Player player, String key) {
        List<MetadataValue> dataList = player.getMetadata(key);
        MetadataValue data = null;
        for (MetadataValue searchData : dataList) {
            if (searchData.getOwningPlugin().equals(plugin)) {
                data = searchData;
                break;
            }
        }
        return data != null
            ? (T) data.value()
            : null;
    }
}

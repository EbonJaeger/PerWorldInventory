package me.gnat008.perworldinventory.data.metadata;

import ch.jalu.injector.Injector;

import me.gnat008.perworldinventory.PerWorldInventory;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Player;

import java.util.List;

import javax.inject.Inject;

public class PWIMetaDataManager {

    @Inject PerWorldInventory plugin;
    @Inject Injector injector;

    PWIMetaDataManager() {}
    public <T extends PWIMetaDataValueAbstract> T createMetadataValue(Class<T> clazz, Object... args) {
        T instance = injector.<T>newInstance(clazz);
        instance.init(args);
        return instance;
    }

    public <T> T getFromPlayer(Player player, String key) {
        List<MetadataValue> dataList = player.getMetadata(key);
        MetadataValue data = null;
        for(MetadataValue searchData : dataList) {
            if(searchData.getOwningPlugin().equals(plugin)) {
                data = searchData;
                break;
            }
        }
        return data != null?(T)data.value():null;
    }
}

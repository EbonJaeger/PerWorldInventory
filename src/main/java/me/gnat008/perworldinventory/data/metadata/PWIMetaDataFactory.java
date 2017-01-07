package me.gnat008.perworldinventory.data.metadata;

import ch.jalu.injector.Injector;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.NumberConversions;

import javax.inject.Inject;

public class PWIMetaDataFactory {

    @Inject PerWorldInventory plugin;
    @Inject Injector injector;

    PWIMetaDataFactory() {}
    public <T extends PWIMetaDataValueAbstract> T createMetadataValue(Class<T> clazz, Object... args) {
        T instance = injector.<T>newInstance(clazz);
        instance.init(args);
        return instance;
    }
}

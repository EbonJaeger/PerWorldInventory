package me.gnat008.perworldinventory.data.metadata;

import org.bukkit.metadata.MetadataValueAdapter;
import javax.inject.Inject;

public abstract class PWIMetaDataValueAbstract<T> extends MetadataValueAdapter {
    @Inject
    private PerWorldInventory plugin;

    PWIMetaDataValueAbstract() {
        super(plugin);
    }

    @Override
    public T value();
}

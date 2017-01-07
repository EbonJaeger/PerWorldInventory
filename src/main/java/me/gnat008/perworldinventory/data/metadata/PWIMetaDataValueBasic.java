package me.gnat008.perworldinventory.data.metadata;
import me.gnat008.perworldinventory.PerWorldInventory;

import javax.inject.Inject;

public class PWIMetaDataValueBasic<T> extends PWIMetaDataValueAbstract<T> {

    @Inject
    PWIMetaDataValueBasic(PerWorldInventory plugin) {
        super(plugin);
    }

    private T value = null;
    public void init(Object... args) {
        if(args.length != 1) {
            throw new IllegalArgumentException("Illegal number of arguments to init. Expected 1, got "+args.length);
        }
        value =((T)args[0]);
    }
    @Override
    public T value() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void invalidate() {
        // Basic values exists and live in cache with no way to invalidate.
    }
}

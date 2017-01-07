package me.gnat008.perworldinventory.data.metadata;

public class PWIMetaDataValueBasic<T> extends PWIMetaDataValueAbstract<T> {
    private T value;

    PWIMetaDataValueBasic() {
        super(plugin);
        this.value = null;
    }

    PWIMetaDataValueBasic(T value) {
        super(plugin);
        this.value = value;
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

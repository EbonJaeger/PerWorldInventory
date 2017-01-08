package me.gnat008.perworldinventory.data.metadata;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.NumberConversions;

public abstract class PWIMetaDataValueAbstract<T> implements MetadataValue {

    private final PerWorldInventory owningPlugin;

    PWIMetaDataValueAbstract(PerWorldInventory plugin) {
        this.owningPlugin = plugin;
    }

    public PerWorldInventory getOwningPlugin() {
        return this.owningPlugin;
    }

    public int asInt() {
        return NumberConversions.toInt(value());
    }

    public float asFloat() {
        return NumberConversions.toFloat(value());
    }

    public double asDouble() {
        return NumberConversions.toDouble(value());
    }

    public long asLong() {
        return NumberConversions.toLong(value());
    }

    public short asShort() {
        return NumberConversions.toShort(value());
    }

    public byte asByte() {
        return NumberConversions.toByte(value());
    }

    public boolean asBoolean() {
        Object value = value();
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }

        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return value != null;
    }

    public String asString() {
        Object value = value();

        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public abstract T value();
}

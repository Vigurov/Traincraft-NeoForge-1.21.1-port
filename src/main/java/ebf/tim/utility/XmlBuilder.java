package ebf.tim.utility;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

/**
 * TiM kept per-transport data (owner, skin, ...) in an XML string; 1.21 keeps the same calls on an NBT compound,
 * which saves and syncs without the string round trip.
 */
public class XmlBuilder {
    public CompoundTag tag;

    public XmlBuilder() {
        this(new CompoundTag());
    }

    public XmlBuilder(CompoundTag tag) {
        this.tag = tag;
    }

    public void putString(String k, String v) { tag.putString(k, v); }
    public String getString(String k) { return tag.getString(k); }
    public boolean containsString(String k) { return tag.contains(k, Tag.TAG_STRING); }

    public void putFloat(String k, float v) { tag.putFloat(k, v); }
    public float getFloat(String k) { return tag.getFloat(k); }
    public boolean hasFloat(String k) { return tag.contains(k, Tag.TAG_FLOAT); }

    public void putDouble(String k, double v) { tag.putDouble(k, v); }
    public double getDouble(String k) { return tag.getDouble(k); }
    public boolean containsDouble(String k) { return tag.contains(k, Tag.TAG_DOUBLE); }

    public void putInt(String k, int v) { tag.putInt(k, v); }
    public int getInt(String k) { return tag.getInt(k); }
    public boolean containsInt(String k) { return tag.contains(k, Tag.TAG_INT); }

    public void putUUID(String k, UUID v) { if (v != null) tag.putUUID(k, v); }
    public UUID getUUID(String k) { return tag.hasUUID(k) ? tag.getUUID(k) : null; }
}

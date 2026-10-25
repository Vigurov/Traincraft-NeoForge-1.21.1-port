package ebf.tim.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

/**
 * TiM kept per-transport and per-rail data in an XML string; 1.21 keeps the same calls on an NBT compound,
 * which saves and syncs without the string round trip. Item stacks are kept as item id and count (rail
 * materials, ties, ballast).
 */
public class XmlBuilder {
    public CompoundTag tag;

    public XmlBuilder() {
        this(new CompoundTag());
    }

    public XmlBuilder(CompoundTag tag) {
        this.tag = tag;
    }

    public XmlBuilder putString(String k, String v) { tag.putString(k, v); return this; }
    public String getString(String k) { return tag.getString(k); }
    public boolean containsString(String k) { return tag.contains(k, Tag.TAG_STRING); }

    public XmlBuilder putFloat(String k, float v) { tag.putFloat(k, v); return this; }
    public Float getFloat(String k) { return tag.contains(k, Tag.TAG_FLOAT) ? tag.getFloat(k) : null; }
    public boolean hasFloat(String k) { return tag.contains(k, Tag.TAG_FLOAT); }
    public boolean containsFloat(String k) { return hasFloat(k); }

    public XmlBuilder putDouble(String k, double v) { tag.putDouble(k, v); return this; }
    public double getDouble(String k) { return tag.getDouble(k); }
    public boolean containsDouble(String k) { return tag.contains(k, Tag.TAG_DOUBLE); }

    public XmlBuilder putInt(String k, int v) { tag.putInt(k, v); return this; }
    public Integer getInt(String k) { return tag.contains(k, Tag.TAG_INT) ? tag.getInt(k) : null; }
    public boolean containsInt(String k) { return tag.contains(k, Tag.TAG_INT); }

    public XmlBuilder putIntArray(String k, int[] v) { if (v != null) tag.putIntArray(k, v); return this; }
    public int[] getIntArray(String k) { return tag.contains(k, Tag.TAG_INT_ARRAY) ? tag.getIntArray(k) : null; }
    public boolean containsIntArray(String k) { return tag.contains(k, Tag.TAG_INT_ARRAY); }

    /**float arrays are kept as the floats' bits in an int array*/
    public XmlBuilder putFloatArray(String k, float[] v) {
        if (v == null) return this;
        int[] bits = new int[v.length];
        for (int i = 0; i < v.length; i++) bits[i] = Float.floatToIntBits(v[i]);
        tag.putIntArray("f:" + k, bits);
        return this;
    }
    public float[] getfloatArray(String k) {
        if (!containsFloatArray(k)) return null;
        int[] bits = tag.getIntArray("f:" + k);
        float[] v = new float[bits.length];
        for (int i = 0; i < v.length; i++) v[i] = Float.intBitsToFloat(bits[i]);
        return v;
    }
    public boolean containsFloatArray(String k) { return tag.contains("f:" + k, Tag.TAG_INT_ARRAY); }

    public XmlBuilder putUUID(String k, UUID v) { if (v != null) tag.putUUID(k, v); return this; }
    public UUID getUUID(String k) { return tag.hasUUID(k) ? tag.getUUID(k) : null; }
    public boolean containsUUID(String k) { return tag.hasUUID(k); }

    public XmlBuilder putItemStack(String k, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            tag.remove("i:" + k);
            return this;
        }
        CompoundTag t = new CompoundTag();
        t.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        t.putInt("count", stack.getCount());
        tag.put("i:" + k, t);
        return this;
    }
    public ItemStack getItemStack(String k) {
        if (!containsItemStack(k)) return null;
        CompoundTag t = tag.getCompound("i:" + k);
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(t.getString("id")));
        if (item == Items.AIR) return null;
        return new ItemStack(item, Math.max(1, t.getInt("count")));
    }
    public boolean containsItemStack(String k) { return tag.contains("i:" + k, Tag.TAG_COMPOUND); }

    public XmlBuilder putXml(String k, XmlBuilder v) { tag.put("x:" + k, v.tag); return this; }
    public XmlBuilder getXml(String k) { return containsXml(k) ? new XmlBuilder(tag.getCompound("x:" + k)) : null; }
    public boolean containsXml(String k) { return tag.contains("x:" + k, Tag.TAG_COMPOUND); }

    public boolean isEmpty() { return tag.isEmpty(); }

    public XmlBuilder copy() { return new XmlBuilder(tag.copy()); }

    @Override
    public boolean equals(Object o) {
        return o instanceof XmlBuilder && ((XmlBuilder) o).tag.equals(tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}

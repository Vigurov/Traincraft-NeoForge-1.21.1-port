package train.blocks.catenary;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import train.blocks.TCBlocks;

/** a player hit a wire: it breaks there, and its wire falls where it was hit */
public record PacketCutWire(int kind, BlockPos a, BlockPos b, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<PacketCutWire> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("traincraft", "cut_wire"));
    public static final StreamCodec<ByteBuf, PacketCutWire> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketCutWire::kind,
            BlockPos.STREAM_CODEC, PacketCutWire::a,
            BlockPos.STREAM_CODEC, PacketCutWire::b,
            ByteBufCodecs.DOUBLE, PacketCutWire::x,
            ByteBufCodecs.DOUBLE, PacketCutWire::y,
            ByteBufCodecs.DOUBLE, PacketCutWire::z,
            PacketCutWire::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCutWire msg, IPayloadContext context) {
        var player = context.player();
        Level level = player.level();
        Vec3 at = new Vec3(msg.x(), msg.y(), msg.z());
        if (msg.kind() < 0 || msg.kind() >= Wires.Kind.values().length || player.position().distanceTo(at) > 8) return;
        Wires.Kind kind = Wires.Kind.values()[msg.kind()];
        //the wire is there, and the point on it
        boolean there = false;
        for (Wires.Wire w : Wires.near(level, at, 2)) {
            if (w.kind() != kind || !w.a().equals(msg.a()) || !w.b().equals(msg.b())) continue;
            for (Vec3 p : w.points()) if (p.distanceTo(at) < 1.5) there = true;
        }
        if (!there || !Wires.cut(level, kind, msg.a(), msg.b())) return;
        if (!player.getAbilities().instabuild) {
            ItemStack drop = new ItemStack(kind == Wires.Kind.SPAN ? TCBlocks.CATENARY_WIRE.get() : TCBlocks.FEEDER_CABLE.get());
            Containers.dropItemStack(level, at.x, at.y, at.z, drop);
        }
        level.playSound(null, at.x, at.y, at.z, net.minecraft.sounds.SoundEvents.CHAIN_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1, 1.2f);
    }
}

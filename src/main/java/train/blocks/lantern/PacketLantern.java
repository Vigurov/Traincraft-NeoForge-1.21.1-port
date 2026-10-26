package train.blocks.lantern;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** the colour picked on the lantern screen */
public record PacketLantern(int color, BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketLantern> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("traincraft", "lantern"));
    public static final StreamCodec<ByteBuf, PacketLantern> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketLantern::color,
            BlockPos.STREAM_CODEC, PacketLantern::pos,
            PacketLantern::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketLantern msg, IPayloadContext context) {
        var player = context.player();
        if (player.distanceToSqr(msg.pos().getCenter()) < 64 && player.level().getBlockEntity(msg.pos()) instanceof LanternTile t) {
            t.setColor(msg.color());
        }
    }
}

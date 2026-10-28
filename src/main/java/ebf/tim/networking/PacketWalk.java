package ebf.tim.networking;

import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** a player standing in a coach walked: where to now (along and across the coach), from their client */
public record PacketWalk(int entityId, float along, float across) implements CustomPacketPayload {
    public static final Type<PacketWalk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TrainsInMotion.MODID, "walk"));
    public static final StreamCodec<ByteBuf, PacketWalk> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketWalk::entityId,
            ByteBufCodecs.FLOAT, PacketWalk::along,
            ByteBufCodecs.FLOAT, PacketWalk::across,
            PacketWalk::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketWalk msg, IPayloadContext context) {
        var p = context.player();
        if (p.level().getEntity(msg.entityId()) instanceof GenericRailTransport t && p.getVehicle() == t) {
            t.walkTo(p, msg.along(), msg.across());
        }
    }
}

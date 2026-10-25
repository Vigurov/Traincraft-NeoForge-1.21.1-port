package ebf.tim.networking;

import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * a key or button for a transport, sent from the client: the ids are the 1.12 ones, see GenericRailTransport#interact
 */
public record PacketInteract(int key, int entityId) implements CustomPacketPayload {
    public static final Type<PacketInteract> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TrainsInMotion.MODID, "interact"));
    public static final StreamCodec<ByteBuf, PacketInteract> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketInteract::key,
            ByteBufCodecs.VAR_INT, PacketInteract::entityId,
            PacketInteract::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketInteract msg, IPayloadContext context) {
        Entity e = context.player().level().getEntity(msg.entityId());
        //only players nearby (inside or next to it) may drive it
        if (e instanceof GenericRailTransport t && context.player().distanceToSqr(t) < 64 * 64) {
            t.interact(context.player(), false, false, msg.key());
        }
    }
}

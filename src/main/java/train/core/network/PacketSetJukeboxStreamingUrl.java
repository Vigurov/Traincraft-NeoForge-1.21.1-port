package train.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import train.entity.rollingStock.EntityJukeBoxCart;

/** jukebox GUI to server: the stream link and whether it should play (1.12 PacketSetJukeboxStreamingUrl) */
public record PacketSetJukeboxStreamingUrl(int entityId, String url, boolean setPlaying) implements CustomPacketPayload {
	public static final Type<PacketSetJukeboxStreamingUrl> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("traincraft", "jukebox"));
	public static final StreamCodec<ByteBuf, PacketSetJukeboxStreamingUrl> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PacketSetJukeboxStreamingUrl::entityId,
			ByteBufCodecs.stringUtf8(1000), PacketSetJukeboxStreamingUrl::url,
			ByteBufCodecs.BOOL, PacketSetJukeboxStreamingUrl::setPlaying,
			PacketSetJukeboxStreamingUrl::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PacketSetJukeboxStreamingUrl msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player p = ctx.player();
			if (p.level().getEntity(msg.entityId) instanceof EntityJukeBoxCart j && p.distanceToSqr(j) < 64 * 64
					&& (!j.getBoolean(ebf.tim.entities.GenericRailTransport.boolValues.LOCKED) || j.getPermissions(p, false, false))) {
				j.recievePacket(msg.url, msg.setPlaying);
			}
		});
	}
}

package train.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import train.blocks.catenary.PacketCutWire;
import train.blocks.catenary.Wires;

import java.util.List;

/** wires on this client: the one looked at is outlined, and an attack on it breaks it there */
public final class WireClient {
    private WireClient() {}

    /** how near the look has to pass a wire to be on it (blocks) */
    private static final double WIDTH = 0.12;
    private static Wires.Hit hovered;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(WireClient::tick);
        NeoForge.EVENT_BUS.addListener(WireClient::attack);
        NeoForge.EVENT_BUS.addListener(WireClient::render);
    }

    /** the wire looked at, if it is nearer than the block looked at */
    private static Wires.Hit look() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        double reach = mc.player.blockInteractionRange();
        Vec3 eye = mc.player.getEyePosition(), dir = mc.player.getViewVector(1);
        Wires.Hit hit = Wires.pick(mc.level, eye, dir, reach, WIDTH);
        if (hit == null) return null;
        HitResult h = mc.hitResult;
        if (h != null && h.getType() != HitResult.Type.MISS && h.getLocation().distanceTo(eye) < hit.distance()) return null;
        return hit;
    }

    private static void tick(ClientTickEvent.Post e) {
        hovered = look();
    }

    private static void attack(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack()) return;
        Wires.Hit hit = look();
        if (hit == null) return;
        e.setCanceled(true);
        e.setSwingHand(true);
        PacketDistributor.sendToServer(new PacketCutWire(hit.wire().kind().ordinal(), hit.wire().a(), hit.wire().b(), hit.at().x, hit.at().y, hit.at().z));
    }

    /** the wire looked at, outlined in white (black was lost on the dark wire) */
    private static void render(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || hovered == null) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = e.getCamera().getPosition();
        PoseStack pose = e.getPoseStack();
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());
        List<Vec3> pts = hovered.wire().points();
        for (int i = 0; i + 1 < pts.size(); i++) {
            Vec3 a = pts.get(i).subtract(cam), b = pts.get(i + 1).subtract(cam), n = b.subtract(a).normalize();
            vc.addVertex(pose.last(), (float) a.x, (float) a.y, (float) a.z).setColor(1f, 1f, 1f, 0.9f).setNormal(pose.last(), (float) n.x, (float) n.y, (float) n.z);
            vc.addVertex(pose.last(), (float) b.x, (float) b.y, (float) b.z).setColor(1f, 1f, 1f, 0.9f).setNormal(pose.last(), (float) n.x, (float) n.y, (float) n.z);
        }
        buffers.endBatch(RenderType.lines());
    }
}

package train.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import train.core.util.MP3Player;
import train.entity.rollingStock.EntityJukeBoxCart;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The jukebox carts on this client: each plays its stream while the server says so, at a loudness that falls
 * off with the distance (1.12 EntityJukeBoxCart.onUpdate), times the game's music-disc volume. All stop when
 * the player leaves the world (1.12: the proxy's player list).
 */
public final class JukeboxClient {
    private JukeboxClient() {}

    /** what a jukebox is playing here */
    private static final class State {
        MP3Player player;
        String url;
        int retryAt;
    }

    private static final Set<EntityJukeBoxCart> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());

    public static void init() {
        javazoom.jl.decoder.JavaLayerUtils.setHook(new train.core.helpers.JLayerHook());
        EntityJukeBoxCart.clientTick = JukeboxClient::tick;
        EntityJukeBoxCart.clientRemoved = JukeboxClient::stop;
        EntityJukeBoxCart.openScreen = j -> Minecraft.getInstance().setScreen(new train.client.gui.GuiJukebox(j));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) -> stopAll());
    }

    /** for the autotest log */
    public static String status(EntityJukeBoxCart j) {
        if (!(j.player instanceof State s) || s.player == null) return "no player";
        return "thread=" + s.player.isPlaying() + " frames=" + s.player.frames() + " volume=" + s.player.getVolume() + " error=" + s.player.error;
    }

    public static String error(EntityJukeBoxCart j) {
        return j.player instanceof State s && s.player != null ? s.player.error : null;
    }

    private static void tick(EntityJukeBoxCart j) {
        State s = j.player instanceof State st ? st : null;
        String url = j.getStreamURL();
        boolean want = j.isPlaying() && !url.isBlank();
        if (s != null && s.player != null && (!want || !url.equals(s.url))) {
            s.player.stop();
            s.player = null;
        }
        if (want) {
            if (s == null) {
                s = new State();
                j.player = s;
            }
            //a stream that ended or failed is tried again after 10 seconds
            if ((s.player == null || !s.player.isPlaying()) && j.tickCount >= s.retryAt) {
                s.player = new MP3Player(url);
                s.url = url;
                s.retryAt = j.tickCount + 200;
                ACTIVE.add(j);
            }
        }
        if (s == null || s.player == null) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        float dist = (float) j.distanceToSqr(cam);
        float v;
        if (dist >= j.volume * 1000.0F) {
            v = 0;
        } else {
            float v2 = 10000.0F / Math.max(dist, 0.01F) / 100.0F;
            v = v2 > 1.0F ? j.volume : Math.max(0, v2 - (1.0F - j.volume));
        }
        v *= mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.RECORDS);
        s.player.setVolume(mc.isPaused() ? 0 : v);

        if (j.getRandom().nextInt(5) == 0 && s.player.isPlaying() && s.player.error == null) {
            j.level().addParticle(ParticleTypes.NOTE, j.getX(), j.getY() + 1.2D, j.getZ(), (j.getRandom().nextInt(24) + 1) / 24.0D, 0.0D, 0.0D);
        }
    }

    private static void stop(EntityJukeBoxCart j) {
        if (j.player instanceof State s && s.player != null) {
            s.player.stop();
            s.player = null;
        }
        ACTIVE.remove(j);
    }

    private static void stopAll() {
        for (EntityJukeBoxCart j : ACTIVE.toArray(new EntityJukeBoxCart[0])) stop(j);
    }
}

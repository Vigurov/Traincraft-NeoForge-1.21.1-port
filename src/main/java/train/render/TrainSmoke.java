package train.render;

import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import train.entity.trains.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Locomotive smoke and steam, the TC 4.5 way (RenderRollingStock.renderSmokeFX / renderExplosionFX with the
 * RenderEnum data, which 1.12 kept but commented out): while the engine runs, vanilla smoke from the chimneys
 * and puffs of steam beside the cylinders. TC4 rolled the dice every rendered frame; this rolls three times a
 * tick, about the same at 60 frames a second.
 * <p>
 * The positions were made for the TC4 renderer, which placed the same models with other offsets and scales
 * (RenderEnum trans/rotate/scale); each one goes back to the model's own coordinates and then through the TiM
 * renderer's transform, so the smoke comes out of the chimney of the model as TiM draws it.
 */
public final class TrainSmoke {
    private TrainSmoke() {}

    /** tc4: trans, rotate, scale; positions: TC4 x forwards, y up, z to the side */
    private record FX(float[][] tc4, ParticleOptions smoke, int smokeIterations, double[][] smokeFX,
                      ParticleOptions explosion, int explosionIterations, double[][] explosionFX) {}

    /** the positions in the TiM model frame (before the rail height), per class */
    private record Placed(Vector3f[] smoke, Vector3f[] explosion) {}

    private static final Map<Class<?>, FX> BY_CLASS = new HashMap<>();
    private static final Map<Class<?>, Placed> PLACED = new HashMap<>();
    /**
     * per class and chimney, what moves the TC4 position onto the middle of the chimney top as TiM draws the model
     * (model frame: x backwards along, y down, z across). Measured with the autotest's smokecheck: the TC4 numbers
     * left the smoke starting 0.15-0.4 blocks over most chimneys, some beside them
     */
    private static final Map<Class<?>, float[][]> ADJUST = new HashMap<>();

    private static void adjust(Class<?> c, float[][] d) {
        ADJUST.put(c, d);
    }
    private static final RandomSource random = RandomSource.create();

    private static ParticleOptions type(String name) {
        return switch (name) {
            case "smoke" -> ParticleTypes.SMOKE;
            case "largesmoke" -> ParticleTypes.LARGE_SMOKE;
            case "explode" -> ParticleTypes.POOF;
            case "cloud" -> ParticleTypes.CLOUD;
            default -> null;
        };
    }

    private static void put(Class<?> c, float[][] tc4, String smoke, int smokeIterations, double[][] smokeFX, String explosion, int explosionIterations, double[][] explosionFX) {
        BY_CLASS.put(c, new FX(tc4, type(smoke), smokeIterations, smokeFX, type(explosion), explosionIterations, explosionFX));
    }

    static {
        //class, TC4 trans/rotate/scale, smoke type, iterations, chimney positions, steam type, iterations, cylinder positions (1.12 RenderEnum)
        put(EntityLocoDieselKof_DB.class, new float[][]{{-1.0f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 2, new double[][]{{0.9, 1.1, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselCD742.class, new float[][]{{-0.8f, 0.75f, 0.0f}, {180.0f, 180.0f, 0.0f}, {0.8f, 0.8f, 0.8f}}, "smoke", 5, new double[][]{{1.25, 1.15, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselChME3.class, new float[][]{{-0.5f, -0.47f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 4, new double[][]{{0.6, 1.55, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselGP7Red.class, new float[][]{{-0.8f, -0.47f, 0.0f}, {0.0f, 180.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 4, new double[][]{{-0.2, 1.3, 0.0}, {0.45, 1.3, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDiesel44TonSwitcher.class, new float[][]{{-2.75f, -0.425f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 4, new double[][]{{0.6, 1.55, 0.0}, {2.0, 1.55, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselSD40.class, new float[][]{{-1.2f, -0.47f, 0.0f}, {180.0f, 90.0f, 0.0f}, {-1.0f, -1.0f, 1.0f}}, "smoke", 5, new double[][]{{1.45, 1.45, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselSD70.class, new float[][]{{-1.2f, -0.44f, 0.0f}, {0.0f, 90.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 5, new double[][]{{0.4, 1.3, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselShunter.class, new float[][]{{-1.2f, -0.42f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 5, new double[][]{{2.4, 1.5, -0.1}, {2.4, 1.5, 0.1}}, "", 0, new double[][]{});
        put(EntityLocoDieselWLs40.class, new float[][]{{0.0f, 0.1f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 5, new double[][]{{3.0, 1.4, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselV60_DB.class, new float[][]{{-0.75f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 4, new double[][]{{0.75, 1.7, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselMILW_H1044.class, new float[][]{{0.0f, 0.06f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "largesmoke", 4, new double[][]{{3.7, 1.7, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselEMDF7.class, new float[][]{{-2.2f, 0.1f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 4, new double[][]{{-0.15, 1.6, 0.0}, {0.5, 1.6, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselEMDF3.class, new float[][]{{-2.2f, 0.1f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 4, new double[][]{{-0.15, 1.6, 0.0}, {0.55, 1.6, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselDD35A.class, new float[][]{{-5.0f, 0.18f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 4, new double[][]{{3.9, 1.6, 0.0}, {-0.4, 1.6, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselBamboo.class, new float[][]{{-1.0f, 0.0f, 0.0f}, {180.0f, 180.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 2, new double[][]{{-0.05, -0.05, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselDeltic.class, new float[][]{{-2.25f, 0.8f, 0.0f}, {0.0f, 90.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 1, new double[][]{{2.3, 1.6, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselClass66.class, new float[][]{{-3.0f, 0.65f, 0.0f}, {0.0f, 90.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 2, new double[][]{{0.9, 1.4, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoDieselFOLM1.class, new float[][]{{-2.8f, 0.05f, 0.9f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 3, new double[][]{{1.3, 1.45, 0.0}}, "", 0, new double[][]{});
        put(EntityLocoSteamCherepanov.class, new float[][]{{-0.875f, -0.47f, 0.0f}, {0.0f, 180.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{1.5, 2.1, 0.0}}, "explode", 2, new double[][]{{0.875, 0.2, 0.2}});
        put(EntityLocoSteamPannier.class, new float[][]{{0.15f, 0.2f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 0.9f, 0.9f}}, "largesmoke", 3, new double[][]{{3.4, 1.8, 0.0}}, "explode", 2, new double[][]{{2.575, 0.05, 0.65}});
        put(EntityLocoSteamC41.class, new float[][]{{-3.5f, -0.5f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.55, 1.65, 0.0}}, "explode", 2, new double[][]{{2.9, 0.05, 0.65}});
        put(EntityLocoSteamC41_080.class, new float[][]{{-3.5f, -0.5f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.55, 1.65, 0.0}}, "explode", 2, new double[][]{{2.9, 0.05, 0.65}});
        put(EntityLocoSteamAlcoSC4.class, new float[][]{{-3.5f, -0.5f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.55, 1.65, 0.0}}, "explode", 2, new double[][]{{2.9, 0.05, 0.65}});
        put(EntityLocoSteamC41T.class, new float[][]{{-3.5f, -0.5f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.55, 1.65, 0.0}}, "explode", 2, new double[][]{{2.9, 0.05, 0.65}});
        put(EntityLocoSteamSouthern1102.class, new float[][]{{-3.5f, -0.5f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.55, 1.65, 0.0}}, "explode", 2, new double[][]{{2.9, 0.05, 0.65}});
        put(EntityLocoSteamUSATCUS.class, new float[][]{{-2.0f, -0.25f, 0.8375f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{2.5, 1.2, 0.0}}, "explode", 2, new double[][]{{2.5, -0.5, 0.65}});
        put(EntityLocoSteamUSATCUK.class, new float[][]{{-2.0f, -0.25f, 0.8375f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{2.5, 1.2, 0.0}}, "explode", 2, new double[][]{{2.5, -0.5, 0.65}});
        put(EntityLocoSteamCoranationClass.class, new float[][]{{-3.8f, 0.2f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{5.45, 1.9, 0.0}}, "explode", 2, new double[][]{{4.8, 0.15, 0.5}});
        put(EntityLocoSteamBR80_DB.class, new float[][]{{-0.75f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{1.8, 1.75, 0.0}}, "explode", 4, new double[][]{{1.6, -0.4, 0.8}});
        put(EntityLocoSteam4_4_0.class, new float[][]{{-0.66f, -0.44f, 0.0f}, {0.0f, 90.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{1.6, 2.05, 0.0}}, "explode", 4, new double[][]{{1.56, -0.2, 0.8}});
        put(EntityLocoSteamSmall.class, new float[][]{{-0.99f, -0.5f, 0.0f}, {0.0f, -90.0f, 0.0f}, {0.8f, 0.8f, 0.8f}}, "smoke", 20, new double[][]{{1.69, 1.1, 0.0}}, "explode", 2, new double[][]{{1.6, -0.4, 0.8}});
        put(EntityLocoSteamLSSP7.class, new float[][]{{-1.0f, -0.2f, 0.8f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 20, new double[][]{{0.1, 1.15, 0.0}}, "explode", 2, new double[][]{{0.7, -0.4, 0.5}, {0.7, -0.4, -0.5}});
        put(EntityLocoSteamHeavy.class, new float[][]{{0.0f, -0.42f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.1, 2.0, 0.0}}, "explode", 2, new double[][]{{3.0, -0.2, 0.8}});
        put(EntityLocoSteamC62Class.class, new float[][]{{0.0f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{5.0, 1.75, 0.0}}, "explode", 2, new double[][]{{5.2, -0.2, 0.8}});
        put(EntityLocoSteamD51.class, new float[][]{{0.0f, -0.5f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{4.85, 1.85, 0.0}}, "explode", 2, new double[][]{{4.6, -0.2, 0.8}});
        put(EntityLocoSteamD51Long.class, new float[][]{{0.0f, -0.5f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{4.85, 1.85, 0.0}}, "explode", 2, new double[][]{{4.6, -0.2, 0.8}});
        put(EntityLocoSteamBR01_DB.class, new float[][]{{-1.0f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.45, 1.8, 0.0}}, "explode", 2, new double[][]{{3.4, -0.2, 0.8}});
        put(EntityLocoSteamGS4.class, new float[][]{{0.0f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.95f, 0.95f, 0.95f}}, "largesmoke", 5, new double[][]{{5.8, 1.7, 0.0}}, "explode", 2, new double[][]{{5.2, -0.2, 0.8}});
        put(EntityLocoSteamEr_Ussr.class, new float[][]{{-0.75f, -0.44f, 0.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{3.45, 1.9, 0.0}}, "explode", 2, new double[][]{{3.4, -0.2, 0.8}});
        put(EntityLocoSteamForneyRed.class, new float[][]{{-1.3f, -0.44f, 0.0f}, {0.0f, 90.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{1.4, 1.9, 0.0}}, "explode", 2, new double[][]{{1.4, -0.2, 0.8}});
        put(EntityLocoSteamMogulBlue.class, new float[][]{{-0.6f, -0.44f, 0.0f}, {0.0f, 90.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 3, new double[][]{{1.55, 1.95, 0.0}}, "explode", 2, new double[][]{{1.45, -0.2, 0.8}});
        put(EntityLocoSteamShay.class, new float[][]{{-0.4f, -0.45f, -0.0f}, {0.0f, 180.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{1.8, 1.7, 0.2}}, "explode", 2, new double[][]{{1.6, -0.4, 0.8}});
        put(EntityLocoSteamVBShay.class, new float[][]{{0.0f, 0.2f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "largesmoke", 20, new double[][]{{0.55, 2.2, 0.0}}, "explode", 2, new double[][]{{1.6, -0.4, 0.8}});
        put(EntityLocoSteamClimax.class, new float[][]{{0.0f, 0.18f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "largesmoke", 20, new double[][]{{2.05, 1.9, 0.0}}, "explode", 2, new double[][]{{1.6, -0.4, 0.8}});
        put(EntityLocoSteamAdler.class, new float[][]{{-0.8f, 1.05f, 0.0f}, {180.0f, -90.0f, 0.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 20, new double[][]{{1.6, 1.3, 0.0}}, "explode", 2, new double[][]{{1.5, -0.4, 0.8}});
        put(EntityLocoSteamAlice0_4_0.class, new float[][]{{0.0f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 20, new double[][]{{2.2, 1.6, 0.0}}, "explode", 2, new double[][]{{1.8, -0.4, 0.8}});
        put(EntityLocoSteam040VB.class, new float[][]{{0.0f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 20, new double[][]{{0.6, 1.75, 0.0}}, "explode", 2, new double[][]{{1.8, -0.4, 0.8}});
        put(EntityLocoSteamGLYN042T.class, new float[][]{{0.0f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 20, new double[][]{{2.15, 1.75, 0.0}}, "explode", 2, new double[][]{{1.8, -0.4, 0.8}});
        put(EntityLocoSteam262T.class, new float[][]{{0.0f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 1.0f, 0.9f}}, "smoke", 20, new double[][]{{2.65, 1.7, 0.0}}, "explode", 2, new double[][]{{2.8, -0.4, 0.8}});
        put(EntityLocoSteamMallardA4.class, new float[][]{{-3.0f, 0.2f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.975f, 0.975f, 0.975f}}, "largesmoke", 20, new double[][]{{4.8, 1.95, 0.0}}, "explode", 2, new double[][]{{4.9, -0.4, 0.8}});
        put(EntityLocoSteamHallClass.class, new float[][]{{-0.65f, 0.15f, -0.25f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{4.25, 1.55, 0.0}}, "explode", 2, new double[][]{{4.0, -0.4, 0.8}});
        put(EntityLocoSteamKingClass.class, new float[][]{{-1.75f, 0.15f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{4.8, 1.6, 0.0}}, "explode", 2, new double[][]{{3.6, -0.4, 0.8}});
        put(EntityLocoSteamBerk1225.class, new float[][]{{-2.0f, -0.65f, 0.0625f}, {0.0f, 0.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{4.65, 1.7, 0.0}}, "explode", 2, new double[][]{{4.2, -0.4, 0.8}});
        put(EntityLocoSteamBerk765.class, new float[][]{{-2.0f, -0.65f, 0.0625f}, {0.0f, 0.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{4.65, 1.7, 0.0}}, "explode", 2, new double[][]{{4.2, -0.4, 0.8}});
        put(EntityLocoSteamMILWClassA.class, new float[][]{{0.0f, 0.1f, 0.0f}, {0.0f, 180.0f, 180.0f}, {0.9f, 0.9f, 0.9f}}, "largesmoke", 20, new double[][]{{4.4, 1.35, 0.0}}, "explode", 2, new double[][]{{4.2, -0.4, 0.8}});
        put(EntityLocoSteamFowler.class, new float[][]{{-3.0f, -0.51585f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "largesmoke", 20, new double[][]{{3.2, 1.6, 0.0}}, "explode", 2, new double[][]{{2.0, -0.4, 0.0}});
        put(EntityLocoSteamSnowPlow.class, new float[][]{{-2.0f, 0.12f, 0.0f}, {0.0f, 180.0f, 180.0f}, {1.0f, 1.0f, 1.0f}}, "smoke", 4, new double[][]{{3.15, 1.6, 0.0}}, "", 0, new double[][]{});
        //smokecheck's corrections
        adjust(EntityLocoSteam4_4_0.class, new float[][]{{0.000f, 0.290f, 0.000f}});
        adjust(EntityLocoSteamMallardA4.class, new float[][]{{0.000f, 0.327f, 0.000f}});
        adjust(EntityLocoSteamHallClass.class, new float[][]{{0.000f, 0.263f, 0.000f}});
        adjust(EntityLocoSteam040VB.class, new float[][]{{0.000f, 0.125f, 0.000f}});
        adjust(EntityLocoSteam262T.class, new float[][]{{0.000f, 0.138f, 0.000f}});
        adjust(EntityLocoSteamAdler.class, new float[][]{{0.000f, 0.125f, 0.000f}});
        adjust(EntityLocoSteamAlcoSC4.class, new float[][]{{0.000f, 0.356f, 0.000f}});
        adjust(EntityLocoSteamAlice0_4_0.class, new float[][]{{0.000f, 0.100f, 0.000f}});
        adjust(EntityLocoSteamBerk765.class, new float[][]{{0.000f, 0.331f, 0.000f}});
        adjust(EntityLocoSteamBerk1225.class, new float[][]{{0.000f, 0.331f, 0.000f}});
        adjust(EntityLocoSteamBR01_DB.class, new float[][]{{0.000f, 0.342f, 0.000f}});
        adjust(EntityLocoSteamBR80_DB.class, new float[][]{{0.000f, 0.355f, 0.000f}});
        adjust(EntityLocoSteamC41.class, new float[][]{{0.000f, 0.312f, 0.000f}});
        adjust(EntityLocoSteamC41_080.class, new float[][]{{0.000f, 0.312f, 0.000f}});
        adjust(EntityLocoSteamC41T.class, new float[][]{{0.000f, 0.312f, 0.000f}});
        adjust(EntityLocoSteamC62Class.class, new float[][]{{0.000f, 0.340f, 0.000f}});
        adjust(EntityLocoSteamCherepanov.class, new float[][]{{0.000f, 0.320f, 0.000f}});
        adjust(EntityLocoSteamClimax.class, new float[][]{{0.000f, 0.370f, 0.000f}});
        adjust(EntityLocoSteamCoranationClass.class, new float[][]{{0.088f, 0.256f, -0.000f}});
        adjust(EntityLocoSteamD51.class, new float[][]{{0.000f, 0.300f, 0.000f}});
        adjust(EntityLocoSteamD51Long.class, new float[][]{{0.000f, 0.300f, 0.000f}});
        adjust(EntityLocoSteamEr_Ussr.class, new float[][]{{0.000f, 0.298f, 0.000f}});
        adjust(EntityLocoSteamForneyRed.class, new float[][]{{0.000f, 0.538f, 0.000f}});
        adjust(EntityLocoSteamFowler.class, new float[][]{{0.000f, 0.291f, 0.000f}});
        adjust(EntityLocoSteamGLYN042T.class, new float[][]{{0.000f, 0.125f, 0.000f}});
        adjust(EntityLocoSteamGS4.class, new float[][]{{0.000f, 0.060f, 0.000f}});
        adjust(EntityLocoSteamHeavy.class, new float[][]{{0.225f, 0.610f, -0.000f}});
        adjust(EntityLocoSteamKingClass.class, new float[][]{{0.000f, 0.319f, 0.000f}});
        adjust(EntityLocoSteamLSSP7.class, new float[][]{{-0.412f, -0.077f, 0.013f}});
        adjust(EntityLocoSteamMILWClassA.class, new float[][]{{0.000f, 0.378f, 0.000f}});
        adjust(EntityLocoSteamMogulBlue.class, new float[][]{{0.000f, 0.368f, 0.000f}});
        adjust(EntityLocoSteamPannier.class, new float[][]{{0.000f, 0.311f, 0.000f}});
        adjust(EntityLocoSteamShay.class, new float[][]{{0.000f, 0.338f, 0.000f}});
        adjust(EntityLocoSteamSmall.class, new float[][]{{0.000f, 0.095f, 0.000f}});
        adjust(EntityLocoSteamSnowPlow.class, new float[][]{{0.000f, 0.155f, 0.000f}});
        adjust(EntityLocoSteamSouthern1102.class, new float[][]{{0.000f, 0.312f, 0.000f}});
        adjust(EntityLocoSteamUSATCUK.class, new float[][]{{0.000f, 0.287f, 0.000f}});
        adjust(EntityLocoSteamUSATCUS.class, new float[][]{{0.096f, 0.278f, -0.046f}});
        adjust(EntityLocoSteamVBShay.class, new float[][]{{0.000f, 0.342f, 0.000f}});
        adjust(EntityLocoDiesel44TonSwitcher.class, new float[][]{{0.000f, 0.094f, 0.000f}, {0.000f, 0.094f, 0.000f}});
        adjust(EntityLocoDieselBamboo.class, new float[][]{{-0.100f, 0.044f, -0.031f}});
        adjust(EntityLocoDieselCD742.class, new float[][]{{0.000f, 0.164f, 0.000f}});
        adjust(EntityLocoDieselChME3.class, new float[][]{{0.000f, 0.103f, 0.000f}});
        adjust(EntityLocoDieselClass66.class, new float[][]{{0.288f, 0.113f, 0.031f}});
        adjust(EntityLocoDieselDD35A.class, new float[][]{{0.000f, 0.070f, 0.000f}, {0.000f, 0.070f, 0.000f}});
        adjust(EntityLocoDieselDeltic.class, new float[][]{{0.000f, 0.022f, 0.000f}});
        adjust(EntityLocoDieselEMDF3.class, new float[][]{{0.000f, 0.112f, 0.000f}, {0.000f, 0.112f, 0.000f}});
        adjust(EntityLocoDieselEMDF7.class, new float[][]{{0.000f, 0.112f, 0.000f}, {0.000f, 0.112f, 0.000f}});
        adjust(EntityLocoDieselFOLM1.class, new float[][]{{-0.125f, 0.163f, -0.025f}});
        adjust(EntityLocoDieselGP7Red.class, new float[][]{{0.000f, 0.202f, 0.000f}, {0.000f, 0.202f, 0.000f}});
        adjust(EntityLocoDieselKof_DB.class, new float[][]{{0.000f, 0.197f, 0.000f}});
        adjust(EntityLocoDieselSD40.class, new float[][]{{0.104f, 0.127f, -0.000f}});
        adjust(EntityLocoDieselSD70.class, new float[][]{{-0.144f, 0.010f, 0.023f}});
        adjust(EntityLocoDieselShunter.class, new float[][]{{0.000f, 0.148f, 0.000f}, {0.000f, 0.148f, 0.000f}});
        adjust(EntityLocoDieselV60_DB.class, new float[][]{{-0.281f, 0.098f, -0.000f}});
        adjust(EntityLocoDieselWLs40.class, new float[][]{{0.000f, 0.156f, 0.000f}});
    }

    private static Matrix3f rotation(float[] deg) {
        return new Matrix3f().rotateX(deg[0] * Mth.DEG_TO_RAD).rotateY(deg[1] * Mth.DEG_TO_RAD).rotateZ(deg[2] * Mth.DEG_TO_RAD);
    }

    /**
     * a TC4 position to the TiM model frame: back through TC4's render (y lowered 0.3, x backwards, then trans,
     * rotate, scale) to model coordinates, then forwards through TiM's (scale, model offset and rotation)
     */
    private static Vector3f toTiM(double[] p, float[][] tc4, GenericRailTransport loco) {
        Vector3f v = new Vector3f((float) -p[0] - tc4[0][0], (float) p[1] + 0.3F - tc4[0][1], (float) p[2] - tc4[0][2]);
        rotation(tc4[1]).transpose().transform(v);
        v.div(tc4[2][0], tc4[2][1], tc4[2][2]);
        float[][] off = loco.modelOffsets(), rot = loco.modelRotations();
        if (rot != null && rot.length > 0) rotation(rot[0]).transform(v);
        if (off != null && off.length > 0) v.add(off[0][0], off[0][1], off[0][2]);
        v.mul(((loco.getRenderScale() - 0.0625F) * 10F) + 1);
        return v;
    }

    private static Placed placed(EntityTrainCore loco, FX data) {
        return PLACED.computeIfAbsent(loco.getClass(), c -> {
            Vector3f[] s = new Vector3f[data.smokeFX.length], e = new Vector3f[data.explosionFX.length];
            float[][] fix = ADJUST.get(c);
            for (int i = 0; i < s.length; i++) {
                s[i] = toTiM(data.smokeFX[i], data.tc4, loco);
                if (fix != null && i < fix.length) s[i].add(fix[i][0], fix[i][1], fix[i][2]);
            }
            for (int i = 0; i < e.length; i++) e[i] = toTiM(data.explosionFX[i], data.tc4, loco);
            return new Placed(s, e);
        });
    }

    /** called every client tick of a locomotive */
    public static void tick(EntityTrainCore loco) {
        FX data = BY_CLASS.get(loco.getClass());
        boolean running = loco.getBoolean(GenericRailTransport.boolValues.RUNNING);
        //a steam locomotive smokes while its fire burns, already while the boiler heats up; the steam from the
        //cylinders only comes once it runs
        boolean fire = loco.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)
                && loco.entityData().get(GenericRailTransport.FUEL_CONSUMPTION) > 0;
        if (data == null || loco.frontBogie == null || loco.backBogie == null
                || !(running || fire) || Math.abs(loco.getXRot()) > 30) {
            return;
        }
        //the forward direction from the bogies (TiM's model x), the side is its z
        double fx = loco.frontBogie.posX - loco.backBogie.posX, fz = loco.frontBogie.posZ - loco.backBogie.posZ;
        double len = Math.sqrt(fx * fx + fz * fz);
        if (len < 1.0E-4) return;
        fx /= len;
        fz /= len;
        //RenderWagon: where the model's origin is, upside down
        double base = ebf.tim.render.RenderWagon.modelBaseY(loco, (loco.frontBogie.yOffset + loco.backBogie.yOffset) * 0.5f);
        Placed at = placed(loco, data);
        //TC4 getSpeed was km/h
        double speed = loco.getVelocity() * 72;
        for (int frame = 0; frame < 3; frame++) {
            if (data.smoke != null && data.smokeIterations > 0
                    && random.nextInt(10 * data.smokeIterations) < data.smokeIterations * 4 + speed * 5) {
                for (int j = 0; j < data.smokeIterations; j++) {
                    for (Vector3f p : at.smoke) spawn(loco, data.smoke, p, 1, base, fx, fz);
                }
            }
            //steam: both sides of the locomotive (TC4 mirrored z for its four fixed yaws)
            if (running && data.explosion != null && random.nextInt(300) < data.explosionIterations * 10) {
                for (int j = 0; j < data.explosionIterations; j++) {
                    for (Vector3f p : at.explosion) {
                        spawn(loco, data.explosion, p, 1, base, fx, fz);
                        spawn(loco, data.explosion, p, -1, base, fx, fz);
                    }
                }
            }
        }
    }

    /**
     * for tests: where its smoke comes out, relative to the locomotive with its bogies along +x (x along, y up from
     * its position, z across), at that rail height; empty when it has none
     */
    public static java.util.List<double[]> smokePoints(EntityTrainCore loco, float railY) {
        FX data = BY_CLASS.get(loco.getClass());
        java.util.List<double[]> out = new java.util.ArrayList<>();
        if (data == null || data.smoke == null) return out;
        double base = ebf.tim.render.RenderWagon.modelBaseY(loco, railY);
        for (Vector3f p : placed(loco, data).smoke) out.add(new double[]{-p.x, base - p.y, p.z});
        return out;
    }

    /** for tests: where its first chimney's smoke comes out in the world now, or null */
    public static net.minecraft.world.phys.Vec3 worldSmokePoint(EntityTrainCore loco) {
        FX data = BY_CLASS.get(loco.getClass());
        if (data == null || data.smoke == null || loco.frontBogie == null || loco.backBogie == null) return null;
        double fx = loco.frontBogie.posX - loco.backBogie.posX, fz = loco.frontBogie.posZ - loco.backBogie.posZ;
        double len = Math.sqrt(fx * fx + fz * fz);
        if (len < 1.0E-4) return null;
        fx /= len;
        fz /= len;
        double base = ebf.tim.render.RenderWagon.modelBaseY(loco, (loco.frontBogie.yOffset + loco.backBogie.yOffset) * 0.5f);
        Vector3f p = placed(loco, data).smoke[0];
        double x = -p.x, y = base - p.y, z = p.z;
        return new net.minecraft.world.phys.Vec3(loco.getX() + x * fx - z * fz, loco.getY() + y, loco.getZ() + x * fz + z * fx);
    }

    /** a model-frame point to the world: turned upside down at 1.5 + rail height, x along the bogies */
    private static void spawn(EntityTrainCore loco, ParticleOptions type, Vector3f p, int side, double base, double fx, double fz) {
        double x = -p.x, y = base - p.y, z = p.z * side;
        loco.level().addParticle(type, loco.getX() + x * fx - z * fz, loco.getY() + y, loco.getZ() + x * fz + z * fx, 0, 0, 0);
    }
}

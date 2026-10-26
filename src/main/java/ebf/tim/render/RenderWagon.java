package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.models.Bogie;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import fexcraft.tmt.slim.TextureManager;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * <h2>Entity Rendering</h2>
 * used for rendering all trains and rollingstock.
 * The transforms are the 1.12 ones; GL11 is the TMT shim that applies them to the pose stack.
 * todo: animations, particles, cargo blocks
 * @author Eternal Blue Flame
 */
public class RenderWagon extends EntityRenderer<GenericRailTransport> {

    private static double railOffset = 0.34f;
    private static final ResourceLocation NONE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public RenderWagon(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(GenericRailTransport entity) {
        return NONE;
    }

    //manages culling, mostly just breaks stuff, leave to return true.
    @Override
    public boolean shouldRender(GenericRailTransport entity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(GenericRailTransport entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        RenderContext.begin(pose, buffers, packedLight, null);
        int depth = RenderContext.depth;
        RenderContext.lampsOn = entity.getBoolean(GenericRailTransport.boolValues.LAMP);
        try {
            doRender(entity, yaw, pitch, entity.backBogie != null ? entity.backBogie.yOffset : 0, false, null);
        } catch (RuntimeException e) {
            //one broken model should not take the game down: report it once and skip it
            if (FAILED.add(entity.getClass())) {
                org.slf4j.LoggerFactory.getLogger("traincraft").error("could not render " + entity.getClass().getSimpleName(), e);
            }
            while (RenderContext.depth > depth) fexcraft.tmt.slim.GL11.glPopMatrix();
        } finally {
            RenderContext.lampsOn = false;
            RenderContext.end();
        }
        renderCouplers(entity, partialTick, pose, buffers, packedLight);
    }

    private static final ResourceLocation COUPLER_TEX = ResourceLocation.withDefaultNamespace("textures/block/anvil.png");
    /** the coupler's height above the rail's floor, and its thickness */
    private static final float COUPLER_Y = 0.5f, COUPLER_SIZE = 0.14f;

    /**
     * the couplers between this transport and the ones coupled to it: a dark bar from one body's end to the
     * other's at frame height (the models have none, and the cars a coupler apart looked like they did not touch).
     * Each pair is drawn once, by the one with the lower entity id.
     */
    private static void renderCouplers(GenericRailTransport entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.frontBogie == null || entity.backBogie == null) return;
        double ex = Mth.lerp(partialTick, entity.xo, entity.getX()), ey = Mth.lerp(partialTick, entity.yo, entity.getY()), ez = Mth.lerp(partialTick, entity.zo, entity.getZ());
        for (Integer id : new Integer[]{entity.frontLinkedID, entity.backLinkedID}) {
            if (id == null || id <= entity.getId() || !(entity.level().getEntity(id) instanceof GenericRailTransport other) || other.frontBogie == null) continue;
            double ox = Mth.lerp(partialTick, other.xo, other.getX()), oy = Mth.lerp(partialTick, other.yo, other.getY()), oz = Mth.lerp(partialTick, other.zo, other.getZ());
            double dx = ox - ex, dz = oz - ez, len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1 || len > 12) continue;
            double ux = dx / len, uz = dz / len;
            double a = entity.reachTowards(other) - 0.1, b = len - other.reachTowards(entity) + 0.1;
            if (b <= a) continue;
            float y0 = (float) (entity.backBogie.yOffset + COUPLER_Y), y1 = (float) (oy - ey + other.backBogie.yOffset + COUPLER_Y);
            box(buffers.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(COUPLER_TEX)), pose.last(),
                    (float) (ux * a), y0, (float) (uz * a), (float) (ux * b), y1, (float) (uz * b), (float) -uz, (float) ux, light);
        }
    }

    /** a square bar from p to q, nx/nz its horizontal side direction */
    private static void box(com.mojang.blaze3d.vertex.VertexConsumer c, PoseStack.Pose pose, float px, float py, float pz, float qx, float qy, float qz, float nx, float nz, int light) {
        float h = COUPLER_SIZE / 2;
        float[][] corner = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        float[][] p = new float[4][3], q = new float[4][3];
        for (int i = 0; i < 4; i++) {
            float sx = corner[i][0] * h, sy = corner[i][1] * h;
            p[i] = new float[]{px + nx * sx, py + sy, pz + nz * sx};
            q[i] = new float[]{qx + nx * sx, qy + sy, qz + nz * sx};
        }
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            quad(c, pose, p[i], p[j], q[j], q[i], light);
        }
        quad(c, pose, p[3], p[2], p[1], p[0], light);
        quad(c, pose, q[0], q[1], q[2], q[3], light);
    }

    private static void quad(com.mojang.blaze3d.vertex.VertexConsumer c, PoseStack.Pose pose, float[] a, float[] b, float[] d, float[] e, int light) {
        float[][] v = {a, b, d, e};
        float[][] uv = {{0, 0}, {1, 0}, {1, 0.15f}, {0, 0.15f}};
        for (int i = 0; i < 4; i++) {
            c.addVertex(pose, v[i][0], v[i][1], v[i][2]).setColor(110, 110, 110, 255).setUv(uv[i][0], uv[i][1])
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    private static final java.util.Set<Class<?>> FAILED = new java.util.HashSet<>();

    /**
     * <h3>Actually render</h3>
     * here we define position, rotation, pitch, bind the texture and render the model.
     * The pose stack is at the entity's position already (1.12 translated to x, y, z first).
     */
    public static void doRender(GenericRailTransport entity, float yaw, float pitch, float bogieOffset, boolean isPaintBucket, @Nullable TransportSkin textureURI){

        if(entity==null){return;}

        if (entity.renderData.modelList == null || entity.renderData.needsModelUpdate) {
            entity.renderData = new TransportRenderData();
            entity.renderData.modelList = entity.getModel();
            entity.renderData.bogies = entity.bogies();

            //cache animating parts
            if (entity.level()!=null && entity.renderData.modelList != null) {
                for (ModelBase part : entity.renderData.modelList) {
                    for (ModelRendererTurbo render : part.getnamedParts()) {
                        cacheAnimation(entity, render, true);
                    }
                }
                //cache the animating parts on the bogies.
                if (entity.renderData.bogies != null) {
                    for (Bogie bogie : entity.renderData.bogies) {
                        bogie.rotationYaw=entity.getYRot();
                        for (ModelRendererTurbo box : bogie.bogieModel.getnamedParts()) {
                            cacheAnimation(entity, box, false);
                        }
                        if(bogie.subBogies==null){continue;}
                        //cache the animating parts on sub-bogies
                        for(Bogie subBogie : bogie.subBogies){
                            subBogie.rotationYaw=entity.getYRot();
                            for(ModelRendererTurbo box : subBogie.bogieModel.getnamedParts()){
                                cacheAnimation(entity, box, false);
                            }
                        }
                    }
                }
            }
            entity.renderData.needsModelUpdate=false;
        }
        if (entity.renderData.modelList == null) {
            return;
        }

        //apply an additional offset based on the rails.
        //do this in render not in entity, otherwise it breaks Y position on client.
        float railTop;
        if(entity.frontBogie!=null && entity.backBogie!=null) {
            railTop = (entity.frontBogie.yOffset + entity.backBogie.yOffset) * 0.5f;
        } else if(entity.level()!=null && !isPaintBucket){
            return;
        } else {
            railTop = ebf.tim.entities.EntityBogie.TIM_RAIL_Y;
        }
        //the model's pivots stay the 1.12 ones (as for a lift of PIVOT), and the whole model is moved so its lowest
        //point, the wheels, lies on the top of the rails (measured: transport_dims.json)
        railOffset = measuring ? railTop : PIVOT;
        bogieOffset = measuring ? railTop : (float) PIVOT;

        GL11.glPushMatrix();
        if (!measuring) GL11.glTranslated(0, lift(entity, railTop), 0);

        //set the render position
        GL11.glTranslated(0, railOffset +bogieOffset+1.5, 0);

        GL11.glTranslated(0, CommonUtil.rotatePoint(new Vec3f(
                Math.abs(entity.rotationPoints()[0])+Math.abs(entity.rotationPoints()[1]),
                0,0), pitch,0,0).yCoord, 0);
        if(entity.frontBogie!=null && entity.backBogie!=null){
            GL11.glTranslated(0,entity.frontBogie.posY-entity.backBogie.posY,0);
        }
        //rotate the model.
        if(!isPaintBucket) {
            GL11.glTranslated(0, -railOffset -bogieOffset, 0);
            GL11.glRotatef(-yaw - 180f, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(pitch + 180f, 0.0f, 0.0f, 1.0f);
            GL11.glTranslated(0, railOffset +bogieOffset, 0);
        }
        GL11.glPushMatrix();

        //scale the model
        float scale = ((entity.getRenderScale()-0.0625f)*10f)+1;
        GL11.glScalef(scale, scale, scale);

        /*
         * <h3>animations</h3>
         * check if there is something to animate. if there is, then calculate the vectors and apply the animations
         */
        if (entity.level()!=null && !Minecraft.getInstance().isPaused()) {
            //keep the value small enough for float precision
            if(Math.abs(entity.renderData.wheelPitch) >= 1000000) {
                entity.renderData.wheelPitch -= Math.copySign(1000000, entity.renderData.wheelPitch);
            }
            // define the rotation angle, scale based on framerate.
            //(1.12 never set the speed it used here, so its wheels stood still; the animator turns a wheel by
            // wheelPitch/(height*3.14) degrees, so rolling one block (360/(3.14*h/16) degrees) is 5757 per block of travel)
            long now = System.currentTimeMillis();
            if (entity.renderData.lastFrameTime != 0) {
                entity.renderData.wheelPitch +=(entity.velocity[1]*Math.min(now-entity.renderData.lastFrameTime, 200)/50f*5757f);
            }

            if (entity.renderData.wheelPitch != entity.renderData.lastWheelPitch) {
                entity.renderData.lastWheelPitch =entity.renderData.wheelPitch;
                //animate the tagged parts
                for (AnimationBase partToAnimate : entity.renderData.animatedPart) {
                    if(partToAnimate==null){continue;}
                    partToAnimate.animate(entity.renderData.wheelPitch, entity.getAnimationData(partToAnimate.getID(partToAnimate.modelRefrence)), entity);
                }
            }
        }
        entity.renderData.lastFrameTime=System.currentTimeMillis();

        TransportSkin s;
        if(!isPaintBucket && entity.level()!=null) {
            s=entity.getTexture(Minecraft.getInstance().player);
        } else if (textureURI!=null){
            s=textureURI;
        } else {
            s=entity.getTextureByID(Minecraft.getInstance().player,false, entity.getDefaultSkin());
        }

        for(int i=0; i< entity.renderData.modelList.length;i++) {
            if(s!=null && s.getTexture(i)!=null) {
                TextureManager.bindTexture(s.getTexture(i), s.colorsFrom, s.colorsTo, entity.colorsFrom, entity.colorsTo);
            }
            GL11.glPushMatrix();
            if(entity.modelOffsets()!=null && entity.modelOffsets().length>i) {
                GL11.glTranslated(entity.modelOffsets()[i][0],entity.modelOffsets()[i][1],entity.modelOffsets()[i][2]);
            }
            if(entity.modelRotations()!=null && entity.modelRotations().length>i) {
                GL11.glRotatef(entity.modelRotations()[i][0],1,0,0);
                GL11.glRotatef(entity.modelRotations()[i][1],0,1,0);
                GL11.glRotatef(entity.modelRotations()[i][2],0,0,1);
            }
            entity.renderData.modelList[i].render(entity, 0,0,0,0,0, 0.0625f);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        /*
         * <h4> render bogies</h4>
         * this loops for every bogie defined in the registry for the transport, that way we can have different bogies.
         */
        if (entity.renderData.bogies != null) {
            int ii=0;
            for(Bogie b : entity.renderData.bogies) {
                //bind the texture
                if (s!=null && s.getBogieSkin(ii) != null) {
                    TextureManager.bindTexture(s.getBogieSkin(ii), s.colorsFrom, s.colorsTo, entity.colorsFrom, entity.colorsTo);
                } else if (s!=null && s.getBogieSkin(ii) == null) {
                    //bogies inherit the locomotive skin
                    TextureManager.bindTexture(s.getTexture(ii), s.colorsFrom, s.colorsTo, entity.colorsFrom, entity.colorsTo);
                }
                GL11.glPushMatrix();
                GL11.glTranslated(b.offset[0], -b.offset[1], b.offset[2]);
                GL11.glRotatef(b.rotation[0], 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(b.rotation[1], 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(b.rotation[2], 0.0f, 0.0f, 1.0f);
                if(!isPaintBucket && entity.level()!=null) {
                    GL11.glRotatef(b.rotationYaw-entity.getYRot(), 0.0f, 1.0f, 0);
                }
                //scale the model.
                GL11.glScalef(scale, scale, scale);

                b.bogieModel.render(entity, 0, 0, 0, 0, 0, 0.0625f);

                if(b.subBogies!=null) {
                    int iii=0;
                    for (Bogie sub : b.subBogies) {
                        if(s!=null && s.getSubBogieSkin(iii)!=null){
                            TextureManager.bindTexture(s.getSubBogieSkin(iii), s.colorsFrom, s.colorsTo, entity.colorsFrom, entity.colorsTo);
                        } else if (s!=null && s.getBogieSkin(ii) != null) {
                            TextureManager.bindTexture(s.getBogieSkin(ii), s.colorsFrom, s.colorsTo, entity.colorsFrom, entity.colorsTo);
                        }
                        GL11.glPushMatrix();
                        GL11.glTranslated(sub.offset[0]-b.offset[0], sub.offset[1]-b.offset[1], sub.offset[2]-b.offset[2]);
                        if(!isPaintBucket) {
                            GL11.glRotatef(sub.rotationYaw, 0.0f, 1.0f, 0);
                        }
                        sub.bogieModel.render(entity, 0, 0, 0, 0, 0, 0.0625f);
                        GL11.glPopMatrix();
                        iii++;
                    }
                }

                GL11.glPopMatrix();
                ii++;
            }
        }

        GL11.glPopMatrix();
    }

    /**1.12 tags in the part names: hidden parts, inside-out parts, glowing parts, animated parts*/
    private static void cacheAnimation(GenericRailTransport entity, ModelRendererTurbo render, boolean main){
        if (render.boxName == null) {
            return;
        }
        if(StaticModelAnimator.checkCulls(render)){
            if (main) render.boxName=render.boxName.replace("cull","").replace("Cull", "");
            render.showModel = false;
        } else if(main && StaticModelAnimator.checkNoCulls(render)){
            render.boxName=render.boxName.replace("nocull","").replace("Nocull", "").replace("NoCull", "");
            render.noCull = true;
        }
        if(render.boxName.contains(StaticModelAnimator.tagGlow)){
            render.boxName=render.boxName.replace(StaticModelAnimator.tagGlow,"");
            render.ignoresLighting=true;
        }
        if (StaticModelAnimator.checkAnimators(render)) {
            entity.renderData.animatedPart.add(StaticModelAnimator.initPart(render, entity));
            render.animated=true;
        }
    }

    /** the autotest's measure=1: draw as 1.12 did, the bogie height as the lift and no correction */
    public static boolean measuring;

    /** the 1.12 render lift the pivots are worked out for */
    public static final double PIVOT = 0.35;

    /** how far to move the model up so its lowest point is at railTop (blocks above the rail block's floor) */
    public static double lift(GenericRailTransport entity, float railTop) {
        ebf.tim.utility.TransportDims.Dims d = ebf.tim.utility.TransportDims.of(entity);
        double a = d == null ? 0.87 : d.a(), s = d == null ? -2 : d.s();
        return railTop - (a + s * PIVOT);
    }

    /** where the model's origin is above the entity, after the lift (TrainSmoke places its smoke from there) */
    public static double modelBaseY(GenericRailTransport entity, float railTop) {
        return 1.5 - 2 * PIVOT + lift(entity, railTop);
    }

    private static float bogieRenderYOffset(float input){
        //1.12 client setting railSkin, default 3
        if(input>0.4f){
            return input-0.095f;
        } else {
            return input;
        }
    }
}

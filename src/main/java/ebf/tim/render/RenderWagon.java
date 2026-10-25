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
        try {
            doRender(entity, yaw, pitch, entity.backBogie != null ? entity.backBogie.yOffset : 0, false, null);
        } finally {
            RenderContext.end();
        }
    }

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
        if(entity.frontBogie!=null && entity.backBogie!=null) {
            railOffset=bogieRenderYOffset(entity.frontBogie.yOffset);
            railOffset+=bogieRenderYOffset(entity.backBogie.yOffset);
            railOffset*=0.5f;
        } else if(entity.level()!=null && !isPaintBucket){
            return;
        }

        GL11.glPushMatrix();

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

    private static float bogieRenderYOffset(float input){
        //1.12 client setting railSkin, default 3
        if(input>0.4f){
            return input-0.095f;
        } else {
            return input;
        }
    }
}

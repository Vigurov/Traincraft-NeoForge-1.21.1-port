package fexcraft.fvtm;

import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import fexcraft.tmt.slim.Tessellator;
import fexcraft.tmt.slim.TexturedPolygon;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A compatibility class for "FVTM Scheme/Format" models.
 * @Author Eternal BlueFlame
 *
 * @OriginalAuthor Ferdinand Calo' (FEX___96)
 */

public class RollingStockModel extends ModelBase {

    public List<TurboList> groups = new ArrayList<>();
    public int textureX, textureY;
    public RollingStockModel(){}

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        if(init){
            initGroups();
        }
        //this can happen somehow
        if(groups==null){
            return;
        }
        //1.12 cached the static parts in display lists; 1.21 rebuilds the vertices every frame
        for(TurboList list :groups) {
            if(list!=null) {
                list.render(list.boxList);
            }
        }

        if(namedList ==null){return;}
        for(ModelRendererTurbo part : namedList) {
            //for animations to work the position and offsets must be done manually every frame.
            if (!part.showModel) {
                continue;
            }
            GL11.glPushMatrix();
            boolean bright = RenderContext.fullBright;
            if (part.ignoresLighting) {
                RenderContext.fullBright = true;
            }
            GL11.glTranslatef(part.rotationPointX * 0.0625F, part.rotationPointY * 0.0625F, part.rotationPointZ * 0.0625F);
            GL11.glRotatef(part.rotateAngleY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(part.rotateAngleZ, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(part.rotateAngleX, 1.0F, 0.0F, 0.0F);
            for (TexturedPolygon poly : part.faces) {
                Tessellator.getInstance().drawTexturedVertsWithNormal(poly, 0.0625F);
            }
            RenderContext.fullBright = bright;
            GL11.glPopMatrix();
        }
    }

    private void initGroups(){
        for(TurboList list :groups) {
            list.initAllParts();
        }
        //for the named list, we sort those into this class to avoid subclassing errors with the animator.
        for(TurboList list :groups) {
            namedList.addAll(list.namedList);
        }
        init=false;
    }

    @Override
    public List<ModelRendererTurbo> getnamedParts(){
        if(init){
            initGroups();
        }
        return namedList;
    }

    public TurboList get(String s){
        for(TurboList t : groups){
            if(t.boxname.equals(s)){
                return t;
            }
        }
        return null;
    }

}

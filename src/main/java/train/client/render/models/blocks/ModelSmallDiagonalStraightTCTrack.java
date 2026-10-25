package train.client.render.models.blocks;

import ebf.tim.utility.CommonUtil;
import net.minecraft.resources.ResourceLocation;
import traincompat.AdvancedModelLoader;
import traincompat.IModelCustom;
import fexcraft.tmt.slim.GL11;
import train.common.library.Info;
import train.common.tile.TileTCRail;

public class ModelSmallDiagonalStraightTCTrack {

    private IModelCustom modelSmallDiagonalStraight;


    public ModelSmallDiagonalStraightTCTrack() {
        modelSmallDiagonalStraight = AdvancedModelLoader.loadModel(fexcraft.tmt.slim.TextureManager.parse(Info.modelPrefix + "track_straight_diagonal.obj"));
    }

    public void render(String type) {
        modelSmallDiagonalStraight.renderAll();
    }

    public void render(String type, TileTCRail tcRail, double x, double y, double z) {
        render( type, CommonUtil.getBlockFacing(tcRail.getLevel(), tcRail.getBlockPos().getX(), tcRail.getBlockPos().getY(), tcRail.getBlockPos().getZ()), x, y, z, 1, 1, 1, 1 );
    }

    public void render( String type, int facing, double x, double y, double z, float r, float g, float b, float a )
    {
        // Bind the texture, so that OpenGL properly textures our block.
        if (type.contains("embedded"))
            fexcraft.tmt.slim.Tessellator.bindTexture(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, Info.modelTexPrefix + "track_embedded.png"));
        else fexcraft.tmt.slim.Tessellator.bindTexture(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, Info.modelTexPrefix + "track_normal.png"));
        // Push a blank matrix onto the stack
        GL11.glPushMatrix();

        // Move the object into the correct position on the block (because the OBJ's origin is the center of the object)
        GL11.glTranslatef((float) x + 0f, (float) y, (float) z + 0f);
        GL11.glColor4f(r, g, b, a);


        if (facing == 4 || facing == 6) {
            GL11.glTranslatef(0f,0,1f);
            GL11.glRotatef(90, 0, 1,0);
        }

        if (facing == 5 || facing == 7) {
            GL11.glTranslatef(0f,0,0f);
            GL11.glRotatef(0, 0, 1,0f);

        }




        render(type);

        // Pop this matrix from the stack.
        GL11.glPopMatrix();
    }

}

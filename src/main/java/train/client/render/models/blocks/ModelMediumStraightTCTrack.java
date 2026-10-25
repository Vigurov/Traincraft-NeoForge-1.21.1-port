package train.client.render.models.blocks;

import ebf.tim.utility.CommonUtil;
import net.minecraft.resources.ResourceLocation;
import traincompat.AdvancedModelLoader;
import traincompat.IModelCustom;
import fexcraft.tmt.slim.GL11;
import train.common.library.Info;
import train.common.tile.TileTCRail;

public class ModelMediumStraightTCTrack {
	private IModelCustom modelMediumStraight;
	private IModelCustom modelEmbeddedMediumStraight;

	public ModelMediumStraightTCTrack() {
		modelMediumStraight = AdvancedModelLoader.loadModel(fexcraft.tmt.slim.TextureManager.parse(Info.modelPrefix + "track_normal.obj"));
	}

	public void render(String type) {
		modelMediumStraight.renderAll();
	}

	public void render(String type, TileTCRail tcRail, double x, double y, double z) {
		// Bind the texture, so that OpenGL properly textures our block.
		if (type.contains("embedded"))
			fexcraft.tmt.slim.Tessellator.bindTexture(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, Info.modelTexPrefix + "track_embedded.png"));
		else fexcraft.tmt.slim.Tessellator.bindTexture(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, Info.modelTexPrefix + "track_normal.png"));
		// Push a blank matrix onto the stack
		GL11.glPushMatrix();

		// Move the object into the correct position on the block (because the OBJ's origin is the center of the object)
		GL11.glTranslatef((float) x + 0.5f, (float) y, (float) z + 0.5f);
		GL11.glColor4f(1, 1, 1, 1);
		//GL11.glScalef(0.5f, 0.5f, 0.5f);
		int facing = CommonUtil.getBlockFacing(tcRail.getLevel(), tcRail.getBlockPos().getX(), tcRail.getBlockPos().getY(), tcRail.getBlockPos().getZ());

		if (facing == 2) {
			GL11.glRotatef(0, 0, 1, 0);
		}
		if (facing == 3) {
			GL11.glRotatef(-90, 0, 1, 0);
		}
		if (facing == 1) {
			GL11.glRotatef(90, 0, 1, 0);
		}
		if(facing == 0){
			GL11.glRotatef(180, 0, 1, 0);
		}

		for (int i = 0; i < 3; i++) {
			render(type);
			GL11.glTranslatef(0.0f, 0.0f, -1.0f);
		}

		// Pop this matrix from the stack.
		GL11.glPopMatrix();
	}
}
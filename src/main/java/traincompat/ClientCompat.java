package traincompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** client helpers the classic track renderer needs */
public final class ClientCompat {
    private ClientCompat() {}

    /** the texture of a block side (1.7.10 Block.getIcon(side, meta)); meta is ignored in 1.21 */
    public static IIcon icon(Block block, int side, int meta) {
        if (block == null) return null;
        BlockState state = block.defaultBlockState();
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        var quads = model.getQuads(state, Direction.from3DDataValue(side), RandomSource.create(0));
        TextureAtlasSprite sprite = quads.isEmpty() ? model.getParticleIcon() : quads.get(0).getSprite();
        return sprite == null ? null : new SpriteIcon(sprite);
    }

    /** the tint of a block (grass and leaves colours) */
    public static int color(Block block, BlockAndTintGetter world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world == null ? block.defaultBlockState() : world.getBlockState(pos);
        if (state.getBlock() != block) state = block.defaultBlockState();
        int c = Minecraft.getInstance().getBlockColors().getColor(state, world, pos, 0);
        return c == -1 ? 0xFFFFFF : c;
    }
}

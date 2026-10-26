package train.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** the wooden bridge pillar, drawn from its OBJ model */
public class BridgePillarBlock extends BaseEntityBlock {
    public BridgePillarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BridgePillarBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Tile(pos, state);
    }

    public static class Tile extends BlockEntity {
        public Tile(BlockPos pos, BlockState state) {
            super(TCBlocks.PILLAR_TILE.get(), pos, state);
        }
    }
}

package train.blocks.machines;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import train.blocks.TCBlocks;

/** a Traincraft machine block: the distillation tower, the open hearth furnace, the diesel generator */
public class MachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public final String kind;

    public MachineBlock(Properties properties, String kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(p -> new MachineBlock(p, kind));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (kind.equals("windmill") && !context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        //the generator, wind mill and water wheel are models drawn by the renderer
        return modelDrawn() ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    private boolean modelDrawn() {
        return kind.equals("dieselgenerator") || kind.equals("windmill") || kind.equals("waterwheel");
    }

    /** energy only: no screen */
    private boolean hasScreen() {
        return !kind.equals("windmill") && !kind.equals("waterwheel");
    }

    private static final net.minecraft.world.phys.shapes.VoxelShape TALL = box(0, 0, 0, 16, 32, 16);

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return kind.equals("windmill") ? TALL : super.getShape(state, level, pos, context);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineTile(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != TCBlocks.MACHINE_TILE.get() ? null
                : (l, p, s, be) -> MachineTile.serverTick(l, p, s, (MachineTile) be);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!hasScreen()) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer sp && level.getBlockEntity(pos) instanceof MachineTile te) {
            sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new MachineMenu(id, inv, te), getName()),
                    buf -> { buf.writeBlockPos(pos); buf.writeUtf(kind); });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof MachineTile te) {
            te.powered = level.hasNeighborSignal(pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineTile te) {
            te.dropInventory();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /** smoke from a working machine */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && hasScreen()) {
            double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
            level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.4, y, z + (random.nextDouble() - 0.5) * 0.4, 0, 0.02, 0);
        }
    }

    @Override
    protected int getLightBlock(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return modelDrawn() ? 0 : super.getLightBlock(state, level, pos);
    }
}

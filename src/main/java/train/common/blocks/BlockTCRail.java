package train.common.blocks;

import ebf.tim.utility.CommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import train.common.ClassicTracks;
import train.common.library.BlockIDs;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

/**
 * a classic Traincraft 4 track piece: the block holding the piece's tile (its type, shape and switch state);
 * a piece longer than a block fills the rest with {@link BlockTCRailGag}. Drawn by the track renderer.
 */
public class BlockTCRail extends Block implements EntityBlock {
	/** 1.7.10 block metadata (track direction 0-3, switch states), kept as a plain 0-15 property */
	public static final IntegerProperty META = IntegerProperty.create("meta", 0, 15);
	/** thin enough to walk over, there so the piece can be looked at and broken */
	private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 2, 16);
	private static final int[] matrixXZ = {0,-1,-2,1,2}, matrixY = {0,-1,-2,1,2};

	public BlockTCRail(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(META, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(META);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileTCRail(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide || type != ClassicTracks.TILE_RAIL ? null
				: (l, p, s, be) -> ((TileTCRail) be).legacy_updateEntity();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileTCRail rail && rail.idDrop != null) {
			return new ItemStack(rail.idDrop);
		}
		return ItemStack.EMPTY;
	}

	/** 1.7.10 onBlockClicked: remember who is breaking it, for the drop */
	@Override
	protected void attack(BlockState state, Level world, BlockPos pos, Player player) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileTCRail rail) {
			rail.lastPlayerToInteract = player;
		}
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileTCRail rail) {
			rail.lastPlayerToInteract = player;
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			legacy_breakBlock(world, pos.getX(), pos.getY(), pos.getZ());
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	public void legacy_breakBlock(Level world, int i, int j, int k) {
		TileTCRail tileEntity = world.getBlockEntity(new BlockPos(i, j, k)) instanceof TileTCRail t ? t : null;
		if (tileEntity != null && tileEntity.isLinkedToRail) {
			world.destroyBlock(new BlockPos(tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ), false);
			world.removeBlockEntity(new BlockPos(tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ));
		}
		if (tileEntity != null && (tileEntity.idDrop != null) && !world.isClientSide) {
			Player player = tileEntity.lastPlayerToInteract;
			if (player != null && !player.isCreative()) {
				popResource(world, new BlockPos(i, j, k), new ItemStack(tileEntity.idDrop, 1));
			}
		}
		if (tileEntity != null) {
			BlockPos o = tileEntity.getBlockPos();
			for(int x : matrixXZ){
				for(int z : matrixXZ){
					for(int y : matrixY){
						BlockPos p = o.offset(x, y, z);
						if (world.getBlockEntity(p) instanceof TileTCRailGag gag && CommonUtil.getBlockAt(world, p.getX(), p.getY(), p.getZ()) instanceof BlockTCRailGag){
							if(gag.originX.size()>1){
								//1.12 passed int[] to removeAll, which never matched: kept as it was
								world.updateNeighborsAt(p.above(), Blocks.AIR);
							} else {
								world.updateNeighborsAt(p.above(), Blocks.AIR);
								CommonUtil.markBlockForUpdate(world, p.getX(), p.getY() + 1, p.getZ());
							}
						}
						if (CommonUtil.getBlockAt(world, p.getX(), p.getY(), p.getZ()) instanceof BlockTCRail){
							world.updateNeighborsAt(p.above(), Blocks.AIR);
							CommonUtil.markBlockForUpdate(world, p.getX(), p.getY() + 1, p.getZ());
						}
					}
				}
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!world.isClientSide) {
			legacy_onNeighborBlockChange(world, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	public void legacy_onNeighborBlockChange(Level world, int i, int j, int k) {
		BlockEntity tile = world.getBlockEntity(new BlockPos(i, j, k));
		if (tile instanceof TileTCRail) {
			if (((TileTCRail)tile).isLinkedToRail) {
				if (world.isEmptyBlock(new BlockPos(((TileTCRail)tile).linkedX, ((TileTCRail)tile).linkedY, ((TileTCRail)tile).linkedZ))) {
					world.destroyBlock(new BlockPos(i, j, k), false);
				}
			}
			BlockPos below = new BlockPos(i, j - 1, k);
			if (!world.getBlockState(below).isFaceSturdy(world, below, Direction.UP) && (BlockIDs.bridgePillar.block == null || CommonUtil.getBlockAt(world, i, j - 1, k) != BlockIDs.bridgePillar.block)) {
				world.destroyBlock(new BlockPos(i, j, k), false);
			}
		}
	}
}

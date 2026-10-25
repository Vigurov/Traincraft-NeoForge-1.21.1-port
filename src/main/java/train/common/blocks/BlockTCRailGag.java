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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import train.common.library.BlockIDs;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

/**
 * the filler blocks of a classic track piece longer than one block; they point at the piece's origin block.
 * On slopes they have the height of the slope there, so it can be walked up.
 */
public class BlockTCRailGag extends Block implements EntityBlock {
	/** 1.7.10 block metadata, kept as a plain 0-15 property */
	public static final IntegerProperty META = IntegerProperty.create("meta", 0, 15);
	private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 2, 16);
	private static final int[] matrixXZ = {0,-1,-2,1,2}, matrixY = {0,-1,-2,1,2};

	public BlockTCRailGag(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(META, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(META);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileTCRailGag(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileTCRailGag gag && gag.bbHeight > 0.125f) {
			return Block.box(0, 0, 0, 16, Math.min(16, gag.bbHeight * 16), 16);
		}
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileTCRailGag gag && !gag.type.equals("null") && gag.bbHeight > 0.2f) {
			return Block.box(0, 0, 0, 16, Math.min(16, gag.bbHeight * 16), 16);
		}
		return Shapes.empty();
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof TileTCRailGag gag && gag.originX.size() > 0
				&& world.getBlockEntity(new BlockPos(gag.originX.get(0), gag.originY.get(0), gag.originZ.get(0))) instanceof TileTCRail tile
				&& tile.idDrop != null) {
			return new ItemStack(tile.idDrop);
		}
		return ItemStack.EMPTY;
	}

	/** 1.7.10 onBlockClicked: remember who is breaking the piece, for the drop */
	@Override
	protected void attack(BlockState state, Level world, BlockPos pos, Player player) {
		setBreaker(world, pos, player);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		setBreaker(world, pos, player);
		return super.playerWillDestroy(world, pos, state, player);
	}

	private static void setBreaker(Level world, BlockPos pos, Player player) {
		if (world.getBlockEntity(pos) instanceof TileTCRailGag gag && gag.originX.size() > 0
				&& world.getBlockEntity(new BlockPos(gag.originX.get(0), gag.originY.get(0), gag.originZ.get(0))) instanceof TileTCRail tile) {
			tile.lastPlayerToInteract = player;
		}
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			legacy_breakBlock(world, pos.getX(), pos.getY(), pos.getZ());
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	public void legacy_breakBlock(Level world, int i, int j, int k) {
		TileTCRailGag tileEntity = world.getBlockEntity(new BlockPos(i, j, k)) instanceof TileTCRailGag t ? t : null;
		if (tileEntity != null && tileEntity.originX.size()>0) {
			BlockPos origin = new BlockPos(tileEntity.originX.get(0), tileEntity.originY.get(0), tileEntity.originZ.get(0));
			world.destroyBlock(origin, false);
			for(int x : matrixXZ){
				for(int z : matrixXZ){
					for(int y : matrixY){
						BlockPos p = tileEntity.getBlockPos().offset(x, y, z);
						Block b = CommonUtil.getBlockAt(world, p.getX(), p.getY(), p.getZ());
						if (b instanceof BlockTCRailGag || b instanceof BlockTCRail){
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
		BlockEntity tileEntity = world.getBlockEntity(new BlockPos(i, j, k));
		if (tileEntity instanceof TileTCRailGag gag) {
			if (gag.originX.size()>0 && world.isEmptyBlock(new BlockPos(gag.originX.get(0), gag.originY.get(0), gag.originZ.get(0)))) {
				world.destroyBlock(new BlockPos(i, j, k), false);
				return;
			}
			BlockPos below = new BlockPos(i, j - 1, k);
			if (!world.getBlockState(below).isFaceSturdy(world, below, Direction.UP) && (BlockIDs.bridgePillar.block == null || CommonUtil.getBlockAt(world, i, j-1, k) != BlockIDs.bridgePillar.block)) {
				world.destroyBlock(new BlockPos(i, j, k), false);
			}
		}
	}
}

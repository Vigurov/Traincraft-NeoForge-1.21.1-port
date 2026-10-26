package train.blocks.bench;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import train.blocks.TCBlocks;

/**
 * The train workbench ("block.parttable"): a 3x3 crafting grid that keeps its items when closed (TileTrainWbench
 * in 1.12 and TC 4.5). It crafts the crafting-table recipes, which include Traincraft's clothes and their dyeing.
 */
public class BlockTrainWorkbench extends HorizontalDirectionalBlock implements EntityBlock {
	public BlockTrainWorkbench(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return simpleCodec(BlockTrainWorkbench::new);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new Tile(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide && player instanceof ServerPlayer sp && level.getBlockEntity(pos) instanceof Tile te) {
			sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new TrainWorkbenchMenu(id, inv, te.items, te, ContainerLevelAccess.create(level, pos)),
					Component.translatable("container.traincraft.trainworkbench")));
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof Tile te) {
			Containers.dropContents(level, pos, te.items);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	/** the grid's items (1.12 TileTrainWbench) */
	public static class Tile extends BlockEntity {
		public final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

		public Tile(BlockPos pos, BlockState state) {
			super(TCBlocks.WORKBENCH_TILE.get(), pos, state);
		}

		@Override
		protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
			super.saveAdditional(tag, registries);
			ContainerHelper.saveAllItems(tag, items, registries);
		}

		@Override
		protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
			super.loadAdditional(tag, registries);
			items.clear();
			ContainerHelper.loadAllItems(tag, items, registries);
		}
	}
}

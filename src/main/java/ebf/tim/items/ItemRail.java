package ebf.tim.items;

import ebf.tim.blocks.rails.BlockRailCore;
import ebf.tim.registry.TiMBlocks;
import ebf.tim.utility.CommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * <h1>Rail Item</h1>
 * a TiM rail with its materials: rail (an ingot), ties, ballast and wires, kept as item ids in the stack's custom data.
 * @author Eternal Blue Flame
 */
public class ItemRail extends Item {
    public ItemRail(Properties properties){
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack held = context.getItemInHand();
        if(world.isClientSide){return InteractionResult.SUCCESS;}
        if(player==null || held.getCount()==0){return InteractionResult.FAIL;}
        BlockPos pos = context.getClickedPos();
        int x=pos.getX(),y=pos.getY(),z=pos.getZ();
        Block block = CommonUtil.getBlockAt(world, x, y, z);
        //for use from item, unlike external use, we want to give the player ability to "place ahead"
        if(block instanceof BaseRailBlock){
            if (context.getClickedFace() == Direction.UP){//side 1/up
                int rotation = CommonUtil.floorDouble(player.getYHeadRot() * 4.0F / 360.0F + 0.5D) & 3;
                int dx = rotation == 1 ? -1 : rotation == 3 ? 1 : 0, dz = rotation == 0 ? 1 : rotation == 2 ? -1 : 0;
                x+=dx; z+=dz;
                for (int i = 0; i < 3 && CommonUtil.getBlockAt(world, x,y,z) instanceof BaseRailBlock; i++) {
                    x+=dx; z+=dz;
                }
                if(CommonUtil.getBlockAt(world, x,y,z) instanceof BaseRailBlock){return InteractionResult.FAIL;}
            } else {//only allow placing on top of a block
                return InteractionResult.FAIL;
            }
        } else {//if it's not a track block, then the Y position will be too low, raise it one.
            y++;
        }
        //now be sure the player can place there, and the chunk position is loaded
        if (!player.mayUseItemAt(new BlockPos(x,y,z), context.getClickedFace(), held) || !world.hasChunk(x>>4, z>>4)) {
            return InteractionResult.FAIL;
        }
        //continue with normal placement
        placeTrack(held, world, x, y, z, player);
        return InteractionResult.CONSUME;
    }

    public Block getPlacedBlock(){
        return TiMBlocks.RAIL.get();
    }

    /**
     * normal placement
     * this is meant mainly for add-ons, and tools like the track builder
     */
    public boolean placeTrack(ItemStack stack, Level world, int x, int y, int z, @Nullable Player p){
        if(world.isClientSide){return true;}
        //a rail without material data (from /give or another mod) used to crash the server: default to iron
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        BlockPos pos = new BlockPos(x,y,z);
        if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)){
            return false;
        }
        BlockState here = world.getBlockState(pos);
        if(!here.isAir() && !here.canBeReplaced()){
            return false;
        }
        if(!here.isAir()){
            world.destroyBlock(pos, true);
        }
        if (world.setBlock(pos, getPlacedBlock().defaultBlockState(), 3)) {
            if (CommonUtil.getBlockAt(world, x, y, z) == getPlacedBlock()) {
                ((BlockRailCore) CommonUtil.getBlockAt(world, x, y, z)).updateShape(x, y, z, world,
                        //set rail
                        tag.contains("rail") ? item(tag, "rail") : new ItemStack(Items.IRON_INGOT),
                        //set ties
                        item(tag, "ties"),
                        //set ballast
                        item(tag, "ballast"),
                        //set wires
                        item(tag, "wires")
                );
                BlockRailCore.updateNearbyShapes(world, x, y, z);
            }
            var sound = getPlacedBlock().defaultBlockState().getSoundType();
            world.playSound(null, x + 0.5F, y + 0.5F, z + 0.5F, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (p == null || !p.isCreative()) {
                stack.shrink(1);
            }
        }
        return true;
    }

    @Nullable
    private static ItemStack item(CompoundTag tag, String key){
        if (!tag.contains(key)) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString(key)));
        return item == Items.AIR ? null : new ItemStack(item);
    }

    /**
     * <h2>Description text</h2>
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> stringList, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ItemStack s;
        if((s = item(tag, "rail")) != null) {
            stringList.add(Component.literal(CommonUtil.translate("menu.rails") + " " + s.getHoverName().getString()));
        } else {
            stringList.add(Component.literal("default rails"));
        }
        //todo: for some reason i ill never understand, the lang file returns ties and ballast backwards.
        stringList.add(Component.literal((s = item(tag, "ballast")) != null ? CommonUtil.translate("menu.ballast") + " " + s.getHoverName().getString() : CommonUtil.translate("menu.noballast")));
        stringList.add(Component.literal((s = item(tag, "ties")) != null ? CommonUtil.translate("menu.ties") + " " + s.getHoverName().getString() : CommonUtil.translate("menu.noties")));
        stringList.add(Component.literal((s = item(tag, "wires")) != null ? CommonUtil.translate("menu.wires") + " " + s.getHoverName().getString() : CommonUtil.translate("menu.nowires")));
    }

    public static ItemStack setStackData(ItemStack stack, ItemStack ingot, ItemStack ballast, ItemStack ties, ItemStack wires){
        //init stack NBT
        CompoundTag stackTag = new CompoundTag();
        if(ingot!=null && !ingot.isEmpty()) {
            stackTag.putString("rail", BuiltInRegistries.ITEM.getKey(ingot.getItem()).toString());
        }
        //rinse and repeat
        if(ties!=null && !ties.isEmpty() && !isItemBanned(ties)) {
            stackTag.putString("ties", BuiltInRegistries.ITEM.getKey(ties.getItem()).toString());
        }
        if(ballast!=null && !ballast.isEmpty() && !isItemBanned(ballast)) {
            stackTag.putString("ballast", BuiltInRegistries.ITEM.getKey(ballast.getItem()).toString());
        }
        if(wires!=null && !wires.isEmpty()) {
            stackTag.putString("wires", BuiltInRegistries.ITEM.getKey(wires.getItem()).toString());
        }
        //set the stack tag to the actual stack
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stackTag));
        return stack;
    }

    public static boolean isItemBanned(ItemStack s){
        Block b = Block.byItem(s.getItem());
        return CommonUtil.stringContains(BuiltInRegistries.ITEM.getKey(s.getItem()).getPath(),"chisel")
                || (b != Blocks.AIR && b instanceof net.minecraft.world.level.block.EntityBlock);
    }

    /**the creative menu versions: iron and gold rails, no/gravel/stone ballast, log/plank/stone/no ties*/
    public static void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
        for (Item ingot : new Item[]{Items.IRON_INGOT, Items.GOLD_INGOT}) {
            for (Block b : new Block[]{null, Blocks.GRAVEL, Blocks.STONE}) {
                for (Block t : new Block[]{Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.STONE, null}) {
                    output.accept(setStackData(new ItemStack(TiMBlocks.railItem()), new ItemStack(ingot),
                            b == null ? null : new ItemStack(b), t == null ? null : new ItemStack(t), null));
                }
            }
        }
    }
}

package ebf.tim.items;

import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.CommonUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class ItemTransport extends Item {
    private final Supplier<? extends EntityType<? extends GenericRailTransport>> type;
    private final Class<? extends GenericRailTransport> transport;
    /**a world-less instance for the tooltip values (1.12 kept one per item too)*/
    private GenericRailTransport info;

    public ItemTransport(Supplier<? extends EntityType<? extends GenericRailTransport>> type, Class<? extends GenericRailTransport> transport, Properties properties) {
        super(properties);
        this.type = type;
        this.transport = transport;
    }

    public GenericRailTransport info() {
        if (info == null) {
            info = ebf.tim.registry.TiMGenericRegistry.definition(transport);
        }
        return info;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GenericRailTransport cart = info();
        if (cart == null) {
            return;
        }
        if(cart.transportYear()!=null) {
            tooltip.add(Component.literal(t("menu.item.year") + ": " + cart.transportYear()).withStyle(ChatFormatting.GRAY));
        }
        if(cart.transportcountry()!=null) {
            tooltip.add(Component.literal(t("menu.item.country") + ": " + t("menu.item." + cart.transportcountry().toLowerCase())).withStyle(ChatFormatting.GRAY));
        }
        if(cart.transportFuelType()!=null && !cart.transportFuelType().equals("")) {
            tooltip.add(Component.literal(t("menu.item.fueltype") + ": " + t("menu.item."+cart.transportFuelType().toLowerCase())).withStyle(ChatFormatting.RED));
        }
        if(cart.getTypes()!=null && cart.getTypes().size()>0){
            StringBuilder s = new StringBuilder();
            tooltip.add(Component.literal(t("menu.item.types")+":").withStyle(ChatFormatting.RED));
            s.append(TrainsInMotion.transportTypes.isTrain(cart.getTypes()) ? t("menu.item.locomotive") : t("menu.item.rollingstock"));
            for(TrainsInMotion.transportTypes type : cart.getTypes()){
                if(!type.isTrain()) {
                    s.append(", ").append(t("menu.item."+type.name()));
                }
            }
            tooltip.add(Component.literal(s.toString()).withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.literal(t("menu.item.weight") +": " + cart.weightKg() + "kg").withStyle(ChatFormatting.GREEN));
        if (cart.transportTopSpeed()!=0){
            tooltip.add(Component.literal(t("menu.item.speed") +": " + cart.transportTopSpeed() +" km/h").withStyle(ChatFormatting.GREEN));
            if (cart.transportMetricHorsePower() !=0){
                tooltip.add(Component.literal(t("menu.item.mhp") +": " + cart.transportMetricHorsePower()).withStyle(ChatFormatting.GREEN));
            }
            if (cart.transportTractiveEffort() != 0){
                tooltip.add(Component.literal(t("menu.item.tractiveeffort") +": " + cart.transportTractiveEffort() + " lbf").withStyle(ChatFormatting.GREEN));
            }
        }
        if(cart.getInventoryRows()>0){
            tooltip.add(Component.literal(t("menu.item.isizeof")+ ": " + (cart.getInventoryRows()*9) + " " + t("menu.item.slots")).withStyle(ChatFormatting.BLUE));
        }
        if(cart.getRiderOffsets()!=null){
            tooltip.add(Component.literal(t("menu.item.seats")+ ": " + cart.getRiderOffsets().length).withStyle(ChatFormatting.BLUE));
        }
        if (cart.isFictional()){
            tooltip.add(Component.literal(t("menu.item.fictional")).withStyle(ChatFormatting.WHITE));
        }
        if (cart.additionalItemText()!=null){
            for (String s : cart.additionalItemText()) {
                if(!s.equals("")) {
                    tooltip.add(Component.literal(s).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level worldObj = context.getLevel();
        Player playerEntity = context.getPlayer();
        if (worldObj.isClientSide || playerEntity == null) {
            return InteractionResult.SUCCESS;
        }
        GenericRailTransport entity = type.get().create(worldObj);
        if (entity == null) {
            return InteractionResult.FAIL;
        }
        entity.setOwner(playerEntity.getUUID());
        if(CommonUtil.placeOnRail(entity, playerEntity, context.getItemInHand(), worldObj,
                context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ())){
            if (!playerEntity.isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private static String t(String translate){
        return CommonUtil.translate(translate);
    }
}

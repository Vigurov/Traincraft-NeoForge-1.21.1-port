package train.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import train.common.blocks.BlockTCRail;
import train.common.blocks.BlockTCRailGag;
import train.common.items.ItemTCRail;
import train.common.library.BlockIDs;
import train.common.library.EnumTracks;
import train.common.library.ItemIDs;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

import java.util.Locale;

/**
 * The classic Traincraft 4 track pieces (straights, curves of several radii, slopes, switches, crossings), ported
 * from the TC 4.5 line: two blocks (the track and its "gag" filler blocks), their tile entities and one item per piece.
 */
public final class ClassicTracks {
    private ClassicTracks() {}

    public static BlockEntityType<TileTCRail> TILE_RAIL;
    public static BlockEntityType<TileTCRailGag> TILE_RAIL_GAG;

    public static void register(IEventBus modBus) {
        modBus.addListener(ClassicTracks::onRegister);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(train.library.Info.modID, name);
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.BLOCK, helper -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F)
                    .sound(SoundType.METAL).noOcclusion().noCollission().pushReaction(PushReaction.BLOCK);
            BlockIDs.tcRail.block = new BlockTCRail(props);
            BlockIDs.tcRailGag.block = new BlockTCRailGag(props);
            helper.register(id("tcrail"), BlockIDs.tcRail.block);
            helper.register(id("tcrailgag"), BlockIDs.tcRailGag.block);
        });
        e.register(Registries.ITEM, helper -> {
            ItemIDs.tcRailSmallStraight.item = new ItemTCRail(EnumTracks.SMALL_STRAIGHT);
            ItemIDs.tcRailMediumStraight.item = new ItemTCRail(EnumTracks.MEDIUM_STRAIGHT);
            ItemIDs.tcRailLongStraight.item = new ItemTCRail(EnumTracks.LONG_STRAIGHT);
            ItemIDs.tcRailVeryLongStraight.item = new ItemTCRail(EnumTracks.VERY_LONG_STRAIGHT);
            ItemIDs.tcRailEmbeddedSmallStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_SMALL_STRAIGHT);
            ItemIDs.tcRailEmbeddedMediumStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_STRAIGHT);
            ItemIDs.tcRailEmbeddedLongStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_LONG_STRAIGHT);
            ItemIDs.tcRailEmbeddedVeryLongStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LONG_STRAIGHT);
            ItemIDs.tcRailSmallDiagonalStraight.item = new ItemTCRail(EnumTracks.SMALL_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailMediumDiagonalStraight.item = new ItemTCRail(EnumTracks.MEDIUM_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailLongDiagonalStraight.item = new ItemTCRail(EnumTracks.LONG_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailVeryLongDiagonalStraight.item = new ItemTCRail(EnumTracks.VERY_LONG_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailEmbeddedSmallDiagonalStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_SMALL_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailEmbeddedMediumDiagonalStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailEmbeddedLongDiagonalStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_LONG_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailEmbeddedVeryLongDiagonalStraight.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LONG_DIAGONAL_STRAIGHT);
            ItemIDs.tcRailMedium45DegreeTurn.item = new ItemTCRail(EnumTracks.MEDIUM_45DEGREE_TURN);
            ItemIDs.tcRailLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.LARGE_45DEGREE_TURN);
            ItemIDs.tcRailVeryLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.VERY_LARGE_45DEGREE_TURN);
            ItemIDs.tcRailSuperLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.SUPER_LARGE_45DEGREE_TURN);
            ItemIDs.tcRail45DegreeTurn9x20.item = new ItemTCRail(EnumTracks.DIAGONAL_TURN_9X20);
            ItemIDs.tcRail45DegreeTurn10x22.item = new ItemTCRail(EnumTracks.DIAGONAL_TURN_10X22);
            ItemIDs.tcRailEmbeddedMedium45DegreeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_45DEGREE_TURN);
            ItemIDs.tcRailEmbeddedLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_45DEGREE_TURN);
            ItemIDs.tcRailEmbeddedVeryLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LARGE_45DEGREE_TURN);
            ItemIDs.tcRailEmbeddedSuperLarge45DegreeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_SUPER_LARGE_45DEGREE_TURN);
            ItemIDs.tcRailEmbedded45DegreeTurn9x20.item = new ItemTCRail(EnumTracks.EMBEDDED_DIAGONAL_TURN_9X20);
            ItemIDs.tcRailEmbedded45DegreeTurn10x22.item = new ItemTCRail(EnumTracks.EMBEDDED_DIAGONAL_TURN_10X22);
            ItemIDs.tcRailSmallParallelCurve.item = new ItemTCRail(EnumTracks.SMALL_PARALLEL_CURVE);
            ItemIDs.tcRailMediumParallelCurve.item = new ItemTCRail(EnumTracks.MEDIUM_PARALLEL_CURVE);
            ItemIDs.tcRailLargeParallelCurve.item = new ItemTCRail(EnumTracks.LARGE_PARALLEL_CURVE);
            ItemIDs.tcRailEmbeddedSmallParallelCurve.item = new ItemTCRail(EnumTracks.EMBEDDED_SMALL_PARALLEL_CURVE);
            ItemIDs.tcRailEmbeddedMediumParallelCurve.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_PARALLEL_CURVE);
            ItemIDs.tcRailEmbeddedLargeParallelCurve.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_PARALLEL_CURVE);
            ItemIDs.tcRail1X1Turn.item = new ItemTCRail(EnumTracks.TURN_1X1);
            ItemIDs.tcRailMediumTurn.item = new ItemTCRail(EnumTracks.MEDIUM_TURN);
            ItemIDs.tcRailLargeTurn.item = new ItemTCRail(EnumTracks.LARGE_TURN);
            ItemIDs.tcRailVeryLargeTurn.item = new ItemTCRail(EnumTracks.VERY_LARGE_TURN);
            ItemIDs.tcRailSuperLargeTurn.item = new ItemTCRail(EnumTracks.SUPER_LARGE_TURN);
            ItemIDs.tcRail29X29Turn.item = new ItemTCRail(EnumTracks.TURN_29X29);
            ItemIDs.tcRail32X32Turn.item = new ItemTCRail(EnumTracks.TURN_32X32);
            ItemIDs.tcRailEmbedded1X1Turn.item = new ItemTCRail(EnumTracks.EMBEDDED_TURN_1X1);
            ItemIDs.tcRailEmbeddedMediumTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_TURN);
            ItemIDs.tcRailEmbeddedLargeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_TURN);
            ItemIDs.tcRailEmbeddedVeryLargeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LARGE_TURN);
            ItemIDs.tcRailEmbeddedSuperLargeTurn.item = new ItemTCRail(EnumTracks.EMBEDDED_SUPER_LARGE_TURN);
            ItemIDs.tcRailEmbedded29X29Turn.item = new ItemTCRail(EnumTracks.EMBEDDED_TURN_29X29);
            ItemIDs.tcRailEmbedded32X32Turn.item = new ItemTCRail(EnumTracks.EMBEDDED_TURN_32X32);
            ItemIDs.tcRailSmallRoadCrossing.item = new ItemTCRail(EnumTracks.SMALL_ROAD_CROSSING);
            ItemIDs.tcRailSmallRoadCrossing1.item = new ItemTCRail(EnumTracks.SMALL_ROAD_CROSSING_1);
            ItemIDs.tcRailSmallRoadCrossing2.item = new ItemTCRail(EnumTracks.SMALL_ROAD_CROSSING_2);
            ItemIDs.tcRailSmallRoadCrossingDynamic.item = new ItemTCRail(EnumTracks.SMALL_ROAD_CROSSING_DYNAMIC);
            ItemIDs.tcRailTwoWaysCrossing.item = new ItemTCRail(EnumTracks.TWO_WAYS_CROSSING);
            ItemIDs.tcRailDiamondCrossing.item = new ItemTCRail(EnumTracks.DIAMOND_CROSSING);
            ItemIDs.tcRailDoubleDiamondCrossing.item = new ItemTCRail(EnumTracks.DOUBLE_DIAMOND_CROSSING);
            ItemIDs.tcRailFourWaysCrossing.item = new ItemTCRail(EnumTracks.FOUR_WAYS_CROSSING);
            ItemIDs.tcRailEmbeddedTwoWaysCrossing.item = new ItemTCRail(EnumTracks.EMBEDDED_TWO_WAYS_CROSSING);
            ItemIDs.tcRailEmbeddedDiamondCrossing.item = new ItemTCRail(EnumTracks.EMBEDDED_DIAMOND_CROSSING);
            ItemIDs.tcRailEmbeddedDoubleDiamondCrossing.item = new ItemTCRail(EnumTracks.EMBEDDED_DOUBLE_DIAMOND_CROSSING);
            ItemIDs.tcRailEmbeddedFourWaysCrossing.item = new ItemTCRail(EnumTracks.EMBEDDED_FOUR_WAYS_CROSSING);
            ItemIDs.tcRailMediumSwitch.item = new ItemTCRail(EnumTracks.MEDIUM_SWITCH);
            ItemIDs.tcRailLargeSwitch.item = new ItemTCRail(EnumTracks.LARGE_SWITCH);
            ItemIDs.tcRailVeryLargeSwitch.item = new ItemTCRail(EnumTracks.VERY_LARGE_SWITCH);
            ItemIDs.tcRailMediumParallelSwitch.item = new ItemTCRail(EnumTracks.MEDIUM_PARALLEL_SWITCH);
            ItemIDs.tcRailLargeParallelSwitch.item = new ItemTCRail(EnumTracks.LARGE_PARALLEL_SWITCH);
            ItemIDs.tcRailMedium45DegreeSwitch.item = new ItemTCRail(EnumTracks.MEDIUM_45DEGREE_SWITCH);
            ItemIDs.tcRailLarge45DegreeSwitch.item = new ItemTCRail(EnumTracks.LARGE_45DEGREE_SWITCH);
            ItemIDs.tcRailEmbeddedMediumSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_SWITCH);
            ItemIDs.tcRailEmbeddedLargeSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_SWITCH);
            ItemIDs.tcRailEmbeddedVeryLargeSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LARGE_SWITCH);
            ItemIDs.tcRailEmbeddedMediumParallelSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_PARALLEL_SWITCH);
            ItemIDs.tcRailEmbeddedLargeParallelSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_PARALLEL_SWITCH);
            ItemIDs.tcRailEmbeddedMedium45DegreeSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_MEDIUM_45DEGREE_SWITCH);
            ItemIDs.tcRailEmbeddedLarge45DegreeSwitch.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_45DEGREE_SWITCH);
            ItemIDs.tcRailSlopeWood.item = new ItemTCRail(EnumTracks.SLOPE_WOOD);
            ItemIDs.tcRailSlopeGravel.item = new ItemTCRail(EnumTracks.SLOPE_GRAVEL);
            ItemIDs.tcRailSlopeBallast.item = new ItemTCRail(EnumTracks.SLOPE_BALLAST);
            ItemIDs.tcRailSlopeSnowGravel.item = new ItemTCRail(EnumTracks.SLOPE_SNOW_GRAVEL);
            ItemIDs.tcRailSlopeDynamic.item = new ItemTCRail(EnumTracks.SLOPE_DYNAMIC);
            ItemIDs.tcRailLargeSlopeWood.item = new ItemTCRail(EnumTracks.LARGE_SLOPE_WOOD);
            ItemIDs.tcRailLargeSlopeGravel.item = new ItemTCRail(EnumTracks.LARGE_SLOPE_GRAVEL);
            ItemIDs.tcRailLargeSlopeBallast.item = new ItemTCRail(EnumTracks.LARGE_SLOPE_BALLAST);
            ItemIDs.tcRailLargeSlopeSnowGravel.item = new ItemTCRail(EnumTracks.LARGE_SLOPE_SNOW_GRAVEL);
            ItemIDs.tcRailLargeSlopeDynamic.item = new ItemTCRail(EnumTracks.LARGE_SLOPE_DYNAMIC);
            ItemIDs.tcRailVeryLargeSlopeWood.item = new ItemTCRail(EnumTracks.VERY_LARGE_SLOPE_WOOD);
            ItemIDs.tcRailVeryLargeSlopeGravel.item = new ItemTCRail(EnumTracks.VERY_LARGE_SLOPE_GRAVEL);
            ItemIDs.tcRailVeryLargeSlopeBallast.item = new ItemTCRail(EnumTracks.VERY_LARGE_SLOPE_BALLAST);
            ItemIDs.tcRailVeryLargeSlopeSnowGravel.item = new ItemTCRail(EnumTracks.VERY_LARGE_SLOPE_SNOW_GRAVEL);
            ItemIDs.tcRailVeryLargeSlopeDynamic.item = new ItemTCRail(EnumTracks.VERY_LARGE_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedLargeSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedVeryLargeSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LARGE_SLOPE_DYNAMIC);
            ItemIDs.tcRailLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.LARGE_CURVED_SLOPE_DYNAMIC);
            ItemIDs.tcRailVeryLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.VERY_LARGE_CURVED_SLOPE_DYNAMIC);
            ItemIDs.tcRailSuperLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.SUPER_LARGE_CURVED_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_LARGE_CURVED_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedVeryLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_VERY_LARGE_CURVED_SLOPE_DYNAMIC);
            ItemIDs.tcRailEmbeddedSuperLargeCurvedSlopeDynamic.item = new ItemTCRail(EnumTracks.EMBEDDED_SUPER_LARGE_CURVED_SLOPE_DYNAMIC);
            for (ItemIDs id : ItemIDs.values()) {
                if (id.item != null) {
                    helper.register(id(id.name().toLowerCase(Locale.ROOT)), id.item);
                }
            }
        });
        e.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            TILE_RAIL = BlockEntityType.Builder.of(TileTCRail::new, BlockIDs.tcRail.block).build(null);
            TILE_RAIL_GAG = BlockEntityType.Builder.of(TileTCRailGag::new, BlockIDs.tcRailGag.block).build(null);
            helper.register(id("tiletcrail"), TILE_RAIL);
            helper.register(id("tiletcrailgag"), TILE_RAIL_GAG);
        });
    }
}

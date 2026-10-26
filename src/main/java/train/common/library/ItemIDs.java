package train.common.library;

import net.minecraft.world.item.Item;

/** the classic Traincraft 4 track items (TC 4.5 ItemIDs, track entries only) */
public enum ItemIDs implements train.common.api.IItemIDs {
	tcRailSmallStraight("ItemTCRail", "item_rail_straight_small", 5),
	tcRailMediumStraight("ItemTCRail", "item_rail_straight_medium", 5),
	tcRailLongStraight("ItemTCRail", "item_rail_straight_large", 5),
	tcRailVeryLongStraight("ItemTCRail", "item_rail_straight_very_large", 5),
	tcRailSmallDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_small", 5),
	tcRailMediumDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_medium", 5),
	tcRailLongDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_long", 5),
	tcRailVeryLongDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_very_long", 5),
	tcRail1X1Turn("ItemTCRail", "item_rail_tc_turn_1", 5),
	tcRailMediumTurn("ItemTCRail", "item_rail_turn_medium", 5),
	tcRailLargeTurn("ItemTCRail", "item_rail_turn_large", 5),
	tcRailVeryLargeTurn("ItemTCRail", "item_rail_turn_veryLarge", 5),
	tcRailSuperLargeTurn("ItemTCRail", "item_rail_tc_super_large_turn", 5),
	tcRail29X29Turn("ItemTCRail", "item_rail_tc_turn_29", 5),
	tcRail32X32Turn("ItemTCRail", "item_rail_tc_turn_32", 5),
	tcRailMedium45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_medium", 5),
	tcRailLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_large", 5),
	tcRailVeryLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_very_large", 5),
	tcRailSuperLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_super_large", 5),
	tcRail45DegreeTurn9x20("ItemTCRail", "item_rail_45degree_turn_9x20", 5),
	tcRail45DegreeTurn10x22("ItemTCRail", "item_rail_45degree_turn_10x22", 5),
	tcRailSmallParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_small", 3),
	tcRailMediumParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_medium", 3),
	tcRailLargeParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_large", 3),
	tcRail20x2SCurve("ItemTCRail", "item_rail_tc_parallel_curve_2x20", 3),
	tcRailTwoWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),
	tcRailDiamondCrossing("ItemTCRail", "item_rail_diamond_crossing", 5),
	tcRailDoubleDiamondCrossing("ItemTCRail", "item_rail_tc_double_diamond_crossing", 5),
	tcRailDiagonalTwoWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),
	tcRailFourWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),
	tcRailMediumSwitch("ItemTCRail", "item_rail_switch_medium", 5),
	tcRailLargeSwitch("ItemTCRail", "item_rail_switch_large", 5),
	tcRailVeryLargeSwitch("ItemTCRail", "item_rail_switch_very_large", 5),
	tcRailMediumParallelSwitch("ItemTCRail", "item_rail_switch_parallel_4x11", 5),
	tcRailLargeParallelSwitch("ItemTCRail", "item_rail_switch_parallel_4x17", 5),
	tcRailMedium45DegreeSwitch("ItemTCRail", "item_rail_switch_45degree_medium", 5),
	tcRailLarge45DegreeSwitch("ItemTCRail", "item_rail_switch_45degree_large", 5),
	tcRailSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),
	tcRailLargeSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailLargeSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailLargeSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailLargeSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailLargeSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),
	tcRailVeryLargeSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailVeryLargeSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailVeryLargeSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailVeryLargeSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailVeryLargeSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),
	tcRailLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),
	tcRailVeryLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),
	tcRailSuperLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),
	tcRailEmbeddedSmallStraight("ItemTCRail", "item_rail_straight_embedded_small_", 5),
	tcRailEmbeddedMediumStraight("ItemTCRail", "item_rail_straight_embedded_medium", 5),
	tcRailEmbeddedLongStraight("ItemTCRail", "item_rail_straight_embedded_large", 5),
	tcRailEmbeddedVeryLongStraight("ItemTCRail", "item_rail_straight_embedded_very_large", 5),
	tcRailEmbeddedSmallDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_small", 5),
	tcRailEmbeddedMediumDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_medium", 5),
	tcRailEmbeddedLongDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_long", 5),
	tcRailEmbeddedVeryLongDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_very_long", 5),
	tcRailEmbedded1X1Turn("ItemTCRail", "item_rail_tc_embedded_turn_1", 5),
	tcRailEmbeddedMediumTurn("ItemTCRail", "item_rail_tc_embedded_medium_turn", 5),
	tcRailEmbeddedLargeTurn("ItemTCRail", "item_rail_tc_embedded_large_turn", 5),
	tcRailEmbeddedVeryLargeTurn("ItemTCRail", "item_rail_tc_embedded_very_large_turn", 5),
	tcRailEmbeddedSuperLargeTurn("ItemTCRail", "item_rail_tc_embedded_super_large_turn", 5),
	tcRailEmbedded29X29Turn("ItemTCRail", "item_rail_tc_embedded_turn_29", 5),
	tcRailEmbedded32X32Turn("ItemTCRail", "item_rail_tc_embedded_turn_32", 5),
	tcRailEmbeddedMedium45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_medium", 5),
	tcRailEmbeddedLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_large", 5),
	tcRailEmbeddedVeryLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_very_large", 5),
	tcRailEmbeddedSuperLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_super_large", 5),
	tcRailEmbedded45DegreeTurn9x20("ItemTCRail", "item_rail_embedded_45degree_turn_9x20", 5),
	tcRailEmbedded45DegreeTurn10x22("ItemTCRail", "item_rail_embedded_45degree_turn_10x22", 5),
	tcRailEmbeddedSmallParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_small", 3),
	tcRailEmbeddedMediumParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_medium", 3),
	tcRailEmbeddedLargeParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_large", 3),
	tcRailEmbedded20x2SCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_2x20", 3),
	tcRailEmbeddedTwoWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),
	tcRailEmbeddedDiamondCrossing("ItemTCRail", "item_rail_embedded_diamond_crossing", 5),
	tcRailEmbeddedDoubleDiamondCrossing("ItemTCRail", "item_rail_tc_embedded_double_diamond_crossing", 5),
	tcRailEmbeddedDiagonalTwoWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),
	tcRailEmbeddedFourWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),
	tcRailEmbeddedMediumSwitch("ItemTCRail", "item_rail_embedded_switch_medium", 5),
	tcRailEmbeddedLargeSwitch("ItemTCRail", "item_rail_embedded_switch_large", 5),
	tcRailEmbeddedVeryLargeSwitch("ItemTCRail", "item_rail_embedded_switch_large", 5),
	tcRailEmbeddedMediumParallelSwitch("ItemTCRail", "item_rail_embedded_switch_parallel_4x11", 5),
	tcRailEmbeddedLargeParallelSwitch("ItemTCRail", "item_rail_embedded_switch_parallel_4x17", 5),
	tcRailEmbeddedMedium45DegreeSwitch("ItemTCRail", "item_rail_embedded_switch_45degree_medium", 5),
	tcRailEmbeddedLarge45DegreeSwitch("ItemTCRail", "item_rail_embedded_switch_45degree_large", 5),
	tcRailEmbeddedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),
	tcRailEmbeddedLargeSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),
	tcRailEmbeddedVeryLargeSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),
	tcRailEmbeddedLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),
	tcRailEmbeddedVeryLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),
	tcRailEmbeddedSuperLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),
	tcRailSmallRoadCrossing("ItemTCRail", "item_rail_small_road_crossing", 5),
	tcRailSmallRoadCrossing1("ItemTCRail", "item_rail_small_road_crossing_1", 5),
	tcRailSmallRoadCrossing2("ItemTCRail", "item_rail_small_road_crossing_2", 5),
	tcRailSmallRoadCrossingDynamic("ItemTCRail", "item_rail_small_road_crossing_dynamic", 5)
	;

	public Item item;
	public String className;
	public String iconName;
	public int amountForEmerald;

	ItemIDs(String classMethodName, String iconName, int amountForEmerald) {
		this.className = classMethodName;
		this.iconName = iconName;
		this.amountForEmerald = amountForEmerald;
	}

	@Override
	public Item getItem() {
		return this.item;
	}

	/**
	 * the tight 90 degree turns (1x1 and 3x3, plain and embedded): too sharp for a train, so they are no longer
	 * made or in the creative tab. They stay registered: switches use the medium turn inside, and pieces already
	 * in a world still drop their item.
	 */
	public static boolean isRetired(ItemIDs id) {
		return id == tcRail1X1Turn || id == tcRailMediumTurn || id == tcRailEmbedded1X1Turn || id == tcRailEmbeddedMediumTurn;
	}
}

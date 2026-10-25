package train.common.library;

import net.minecraft.world.item.Item;
import train.common.api.TrackRecord;

import java.util.HashMap;
import java.util.Map;

/** the track part of the TC 4.5 registry */
public class TraincraftRegistry {
	public static TrackRecord findTrackRecordByName(String label) {
		for (TrackRecord track : EnumTracks.values()) {
			if (track.getLabel().equals(label)) {
				return track;
			}
		}
		return null;
	}

	public static TrackRecord findTrackRecordByItem(Item item) {
		for (TrackRecord track : EnumTracks.values()) {
			if (track.getItem() != null && track.getItem().getItem() == item) {
				return track;
			}
		}
		return null;
	}
}

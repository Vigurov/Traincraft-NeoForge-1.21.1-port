package train.library;

import ebf.tim.registry.TiMGenericRegistry;
import train.entity.trains.*;

/**
 * the Traincraft transports
 * todo: the rest of the 1.12 list (train.library.TrainRegistry)
 */
public class TrainRegistry {
    public static void register() {
        TiMGenericRegistry.registerTransport(EntityLocoDieselChME3.class, EntityLocoDieselChME3::new);
    }
}

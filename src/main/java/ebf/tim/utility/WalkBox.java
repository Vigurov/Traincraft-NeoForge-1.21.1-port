package ebf.tim.utility;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * A piece of what players and mobs walk on in an open car (WalkShapes): its floor, or a bit of a wall. Solid to
 * them only: clicks and hits go to the car's CollisionBoxes, and the cars do not bump into these.
 */
public class WalkBox extends PartEntity<GenericRailTransport> {
    public final GenericRailTransport host;
    /** where it stands on the car: along it, across it (from its middle), and its bottom over the rails */
    public final float along, side, bottom;
    private final EntityDimensions size;

    public WalkBox(GenericRailTransport transport, float along, float side, float bottom, float width, float height) {
        super(transport);
        host = transport;
        this.along = along;
        this.side = side;
        this.bottom = bottom;
        size = EntityDimensions.scalable(width, height);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return size == null ? super.getDimensions(pose) : size;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}

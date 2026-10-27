package ebf.tim.utility;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * One of the boxes along a transport that players click and hit, and that other transports bump into.
 * 1.12 spawned these as entities of their own; in 1.21 they are NeoForge part entities of the transport.
 */
public class CollisionBox extends PartEntity<GenericRailTransport> {
    public final GenericRailTransport host;
    private final EntityDimensions size;
    /** whether players and mobs bump into it: not on an open car, whose WalkBoxes they walk on instead */
    public boolean solid = true;

    public CollisionBox(GenericRailTransport transport) {
        super(transport);
        host = transport;
        size = EntityDimensions.scalable(transport.getHitboxSize()[2], transport.getHitboxSize()[1]);
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
        return true;
    }

    /** solid, like a boat: players and mobs walk into it and can stand on it (1.12 only pushed them away, so they
     * got through with a little effort) */
    @Override
    public boolean canBeCollidedWith() {
        return solid;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return host.interact(player, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !isInvulnerableTo(source) && host.hurt(source, amount);
    }

    @Override
    public ItemStack getPickResult() {
        return host.getPickResult();
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || host == entity;
    }
}

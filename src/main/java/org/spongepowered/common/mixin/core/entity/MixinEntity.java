/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataList;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.dismount.DismountTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGravityData;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = Entity.class, prefix = "entity$"))
public abstract class MixinEntity implements IMixinEntity {

    private static final String LAVA_DAMAGESOURCE_FIELD = "Lnet/minecraft/util/DamageSource;LAVA:Lnet/minecraft/util/DamageSource;";
    private static final String ATTACK_ENTITY_FROM_METHOD = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z";
    private static final String FIRE_DAMAGESOURCE_FIELD = "Lnet/minecraft/util/DamageSource;IN_FIRE:Lnet/minecraft/util/DamageSource;";
    private static final String WORLD_SPAWN_PARTICLE = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V";
    private static final String RIDING_ENTITY_FIELD = "Lnet/minecraft/entity/Entity;ridingEntity:Lnet/minecraft/entity/Entity;";
    @SuppressWarnings("unused")
    private static final String
            ENTITY_ITEM_INIT =
            "Lnet/minecraft/entity/item/EntityItem;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V";
    // @formatter:off
    private EntityType entityType = SpongeImpl.getRegistry().getTranslated(this.getClass(), EntityType.class);
    private boolean teleporting;
    private net.minecraft.entity.Entity teleportVehicle;
    private float origWidth;
    private float origHeight;
    @Nullable private DamageSource originalLava;
    protected boolean isConstructing = true;
    @Nullable private Text displayName;
    protected Cause destructCause;
    private BlockState currentCollidingBlock;
    private BlockPos lastCollidedBlockPos;
    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    @SuppressWarnings("unused")
    private SpongeProfileManager spongeProfileManager;
    @SuppressWarnings("unused")
    private UserStorageService userStorageService;
    private Timing timing;

    @Shadow public net.minecraft.entity.Entity ridingEntity;
    @Shadow @Final private List<net.minecraft.entity.Entity> riddenByEntities;
    @Shadow private UUID entityUniqueID;
    @Shadow public net.minecraft.world.World world;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public boolean velocityChanged;
    @Shadow public double prevPosX;
    @Shadow public double prevPosY;
    @Shadow public double prevPosZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow public float fallDistance;
    @Shadow public boolean isDead;
    @Shadow public boolean onGround;
    @Shadow public boolean inWater;
    @Shadow protected boolean isImmuneToFire;
    @Shadow public int hurtResistantTime;
    @Shadow public int fire; // fire
    @Shadow public int dimension;
    @Shadow protected Random rand;
    @Shadow public float prevDistanceWalkedModified;
    @Shadow public float distanceWalkedModified;
    @Shadow protected EntityDataManager dataManager;
    @Shadow public int ticksExisted;

    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract void setDead();
    @Shadow public abstract int getAir();
    @Shadow public abstract void setAir(int air);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract void setCustomNameTag(String name);
    @Shadow public abstract UUID getUniqueID();
    @Shadow @Nullable public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow public abstract void setFire(int seconds);
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int getEntityId();
    @Shadow public abstract boolean isBeingRidden();
    @Shadow public abstract SoundCategory getSoundCategory();
    @Shadow public abstract List<net.minecraft.entity.Entity> shadow$getPassengers();
    @Shadow public abstract net.minecraft.entity.Entity getLowestRidingEntity();
    @Shadow public abstract net.minecraft.entity.Entity getRidingEntity();
    @Shadow public abstract void removePassengers();
    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract boolean isEntityInvulnerable(DamageSource source);
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract boolean isInWater();
    @Shadow public abstract boolean isRiding();
    @Shadow public abstract boolean isOnSameTeam(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract double getDistanceSqToEntity(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void setLocationAndAngles(double x, double y, double z, float yaw, float pitch);
    @Shadow public abstract boolean hasNoGravity();
    @Shadow public abstract void setNoGravity(boolean noGravity);
    @Shadow public abstract void setPositionAndUpdate(double x, double y, double z);
    @Shadow protected abstract void removePassenger(net.minecraft.entity.Entity passenger);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow protected abstract void setSize(float width, float height);
    @Shadow protected abstract void applyEnchantments(EntityLivingBase entityLivingBaseIn, net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void extinguish();
    @Shadow protected abstract void setFlag(int flag, boolean set);

    // @formatter:on

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimension:I", opcode = Opcodes.PUTFIELD))
    private void onSet(net.minecraft.entity.Entity self, int dimensionId, net.minecraft.world.World worldIn) {
        if (worldIn instanceof IMixinWorldServer) {
            self.dimension = ((IMixinWorldServer) worldIn).getDimensionId();
        } else {
            self.dimension = dimensionId;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(net.minecraft.world.World worldIn, CallbackInfo ci) {
        if (this.entityType instanceof SpongeEntityType) {
            SpongeEntityType spongeEntityType = (SpongeEntityType) this.entityType;
            if (spongeEntityType.getEnumCreatureType() == null) {
                for (EnumCreatureType type : EnumCreatureType.values()) {
                    if (SpongeImplHooks.isCreatureOfType((net.minecraft.entity.Entity) (Object) this, type)) {
                        spongeEntityType.setEnumCreatureType(type);
                        break;
                    }
                }
            }
        }
        if (worldIn != null && !worldIn.isRemote) {
            this.spongeProfileManager = ((SpongeProfileManager) Sponge.getServer().getGameProfileManager());
            this.userStorageService = SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get();
        }
    }

    @Override
    public boolean isInConstructPhase() {
        return this.isConstructing;
    }

    @Override
    public void firePostConstructEvents() {
        this.isConstructing = false;
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "FIELD", target = RIDING_ENTITY_FIELD, ordinal = 0),
            cancellable = true)
    public void onStartRiding(net.minecraft.entity.Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> ci) {
        if (!this.world.isRemote && ShouldFire.RIDE_ENTITY_EVENT_MOUNT) {
            if (SpongeImpl.postEvent(SpongeEventFactory.createRideEntityEventMount(Cause.of(NamedCause.source(this)), (Entity) vehicle))) {
                ci.cancel();
            }
        }
    }

    /**
     * @author rexbut - December 16th, 2016
     *
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void dismountRidingEntity() {
        if (this.ridingEntity != null) {
            if (this.getRidingEntity().isDead) {
                this.dismountRidingEntity(DismountTypes.DEATH);
            } else {
                this.dismountRidingEntity(DismountTypes.PLAYER);
            }
        }
    }

    @Override
    public boolean dismountRidingEntity(DismountType type) {
        if (!this.world.isRemote && ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT) {
            if (SpongeImpl.postEvent(SpongeEventFactory
                    .createRideEntityEventDismount(Cause
                            .of(NamedCause.source(this), NamedCause.of("DismountType", type)),
                            type,
                            (Entity) this.getRidingEntity()))
                    ) {
                return false;
            }
        }

        if (this.ridingEntity != null) {
            MixinEntity entity = (MixinEntity) (Object) this.ridingEntity;
            this.ridingEntity = null;
            entity.removePassenger((net.minecraft.entity.Entity) (Object) this);
        }
        return true;
    }

    @Override
    public boolean removePassengers(DismountType type) {
        boolean dismount = false;
        for (int i = this.riddenByEntities.size() - 1; i >= 0; --i) {
            dismount = ((IMixinEntity)this.riddenByEntities.get(i)).dismountRidingEntity(type) || dismount;
        }
        return dismount;
    }

    @Inject(method = "setSize", at = @At("RETURN"))
    public void onSetSize(float width, float height, CallbackInfo ci) {
        if (this.origWidth == 0 || this.origHeight == 0) {
            this.origWidth = this.width;
            this.origHeight = this.height;
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void onMoveEntity(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (!this.world.isRemote && !SpongeHooks.checkEntitySpeed(((net.minecraft.entity.Entity) (Object) this), x, y, z)) {
            ci.cancel();
        }
    }

    @Inject(method = "setOnFireFromLava()V", at = @At(value = "FIELD", target = LAVA_DAMAGESOURCE_FIELD, opcode = Opcodes.GETSTATIC)) // setOnFireFromLava
    public void preSetOnFire(CallbackInfo callbackInfo) {
        if (!this.world.isRemote) {
            this.originalLava = DamageSource.LAVA;
            AxisAlignedBB bb = this.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
            Location<World> location = DamageEventHandler.findFirstMatchingBlock((net.minecraft.entity.Entity) (Object) this, bb, block ->
                block.getMaterial() == Material.LAVA);
            DamageSource.LAVA = new MinecraftBlockDamageSource("lava", location).setFireDamage();
        }
    }

    @Inject(method = "setOnFireFromLava()V", at = @At(value = "INVOKE_ASSIGN", target = ATTACK_ENTITY_FROM_METHOD)) // setOnFireFromLava
    public void postSetOnFire(CallbackInfo callbackInfo) {
        if (!this.world.isRemote) {
            if (this.originalLava == null) {
                SpongeImpl.getLogger().error("Original lava is null!");
                Thread.dumpStack();
            }
            DamageSource.LAVA = this.originalLava;
        }
    }

    private DamageSource originalInFire;

    @Inject(method = "dealFireDamage", at = @At(value = "FIELD", target = FIRE_DAMAGESOURCE_FIELD, opcode = Opcodes.GETSTATIC))
    public void preFire(CallbackInfo callbackInfo) {
        // Sponge Start - Find the fire block!
        if (!this.world.isRemote) {
            this.originalInFire = DamageSource.IN_FIRE;
            AxisAlignedBB bb = this.getEntityBoundingBox().expand(-0.001D, -0.001D, -0.001D);
            Location<World> location = DamageEventHandler.findFirstMatchingBlock((net.minecraft.entity.Entity) (Object) this, bb, block ->
                block.getBlock() == Blocks.FIRE || block.getBlock() == Blocks.FLOWING_LAVA || block.getBlock() == Blocks.LAVA);
            DamageSource.IN_FIRE = new MinecraftBlockDamageSource("inFire", location).setFireDamage();
        }
    }

    @Inject(method = "dealFireDamage", at = @At(value = "INVOKE_ASSIGN", target = ATTACK_ENTITY_FROM_METHOD))
    public void postDealFireDamage(CallbackInfo callbackInfo) {
        if (!this.world.isRemote) {
            if (this.originalInFire == null) {
                SpongeImpl.getLogger().error("Original fire is null!");
                Thread.dumpStack();
            }
            DamageSource.IN_FIRE = this.originalInFire;
        }
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        Optional<VehicleData> vehicleData = get(VehicleData.class);
        if (vehicleData.isPresent()) {
            manipulators.add(vehicleData.get());
        }
        if (this.fire > 0) {
            manipulators.add(get(IgniteableData.class).get());
        }
        manipulators.add(new SpongeGravityData(!this.hasNoGravity()));
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return new SpongeEntitySnapshotBuilder().from(this).build();
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }

    @Inject(method = "setPosition", at = @At("HEAD"))
    public void onSetPosition(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) (Object) this;
            if (player.connection != null) {
                ((IMixinNetHandlerPlayServer) player.connection).captureCurrentPlayerPosition();
            }
        }
    }

    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Location<World> getLocation() {
        return new Location<>((World) this.world, getPosition());
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        boolean result = setLocation(location);
        if (result) {
            setRotation(rotation);
            return true;
        }

        return false;
    }

    @Override
    public boolean setLocation(Location<World> location) {
        checkNotNull(location, "The location was null!");
        if (isRemoved()) {
            return false;
        }

        MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent((net.minecraft.entity.Entity) (Object) this, location);
        if (event.isCancelled()) {
            return false;
        } else {
            location = event.getToTransform().getLocation();
            this.rotationPitch = (float) event.getToTransform().getPitch();
            this.rotationYaw = (float) event.getToTransform().getYaw();
        }

        IMixinChunkProviderServer chunkProviderServer = (IMixinChunkProviderServer) ((WorldServer) this.world).getChunkProvider();
        chunkProviderServer.setForceChunkRequests(true);
        // detach passengers
        final net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity) (Object) this;
        final List<net.minecraft.entity.Entity> passengers = thisEntity.getPassengers();

        net.minecraft.world.World nmsWorld = null;
        if (location.getExtent().getUniqueId() != ((World) this.world).getUniqueId()) {
            nmsWorld = (net.minecraft.world.World) location.getExtent();
            if ((net.minecraft.entity.Entity) (Object) this instanceof EntityPlayerMP) {
                // Close open containers
                final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) (net.minecraft.entity.Entity) (Object) this;
                if (entityPlayerMP.openContainer != entityPlayerMP.inventoryContainer) {
                    entityPlayerMP.closeContainer();
                }
            }
            EntityUtil.changeWorld((net.minecraft.entity.Entity) (Object) this, location, ((IMixinWorldServer) this.world).getDimensionId(), ((IMixinWorldServer) nmsWorld).getDimensionId());
        } else {
            if (thisEntity instanceof EntityPlayerMP && ((EntityPlayerMP) thisEntity).connection != null) {
                EntityPlayerMP thisPlayer = (EntityPlayerMP) thisEntity;
                ((WorldServer) location.getExtent()).getChunkProvider().loadChunk(location.getChunkPosition().getX(), location.getChunkPosition().getZ());
                thisPlayer.connection.setPlayerLocation(location.getX(), location.getY(), location.getZ(), thisEntity.rotationYaw, thisEntity.rotationPitch);
                ((IMixinNetHandlerPlayServer) thisPlayer.connection).setLastMoveLocation(null); // Set last move to teleport target
            } else {
                setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            }
        }

        // Re-attach passengers
        for (net.minecraft.entity.Entity passenger : passengers) {
            passenger.startRiding(thisEntity, true);
        }

        chunkProviderServer.setForceChunkRequests(false);
        return true;
    }

    // always use these methods internally when setting locations from a transform or location
    // to avoid firing a DisplaceEntityEvent.Teleport
    @Override
    public void setLocationAndAngles(Location<World> location) {
        if (((Entity) this) instanceof EntityPlayerMP) {
            ((EntityPlayerMP) (Object) this).connection.setPlayerLocation(location.getX(), location.getY(), location.getZ(), this.rotationYaw, this.rotationPitch);
        } else {
            this.setPosition(location.getX(), location.getY(), location.getZ());
        }
        if (this.world != location.getExtent()) {
            this.world = (net.minecraft.world.World) location.getExtent();
        }
    }

    @Override
    public void setLocationAndAngles(Transform<World> transform) {
        Vector3d position = transform.getPosition();
        EntityPlayerMP player = null;
        if (((Entity) this) instanceof EntityPlayerMP) {
            player = ((EntityPlayerMP) (Object) this);
        }
        if (player != null && player.connection != null) {
            player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) transform.getYaw(), (float) transform.getPitch());
        } else {
            this.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) transform.getYaw(), (float) transform.getPitch());
        }
        if (this.world != transform.getExtent()) {
            this.world = (net.minecraft.world.World) transform.getExtent();
        }
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        boolean relocated = true;

        if (relativePositions.isEmpty()) {
            // This is just a normal teleport that happens to set both.
            relocated = setLocation(location);
            setRotation(rotation);
        } else {
            if (((Entity) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) this).connection != null) {
                // Players use different logic, as they support real relative movement.
                EnumSet<SPacketPlayerPosLook.EnumFlags> relativeFlags = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

                if (relativePositions.contains(RelativePositions.X)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.X);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Y);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Z);
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
                }

                ((EntityPlayerMP) (Entity) this).connection.setPlayerLocation(location.getPosition().getX(), location.getPosition()
                        .getY(), location.getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), relativeFlags);
            } else {
                Location<World> resultantLocation = getLocation();
                Vector3d resultantRotation = getRotation();

                if (relativePositions.contains(RelativePositions.X)) {
                    resultantLocation = resultantLocation.add(location.getPosition().getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    resultantLocation = resultantLocation.add(0, location.getPosition().getY(), 0);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    resultantLocation = resultantLocation.add(0, 0, location.getPosition().getZ());
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    resultantRotation = resultantRotation.add(rotation.getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    resultantRotation = resultantRotation.add(0, rotation.getY(), 0);
                }

                // From here just a normal teleport is needed.
                relocated = setLocation(resultantLocation);
                setRotation(resultantRotation);
            }
        }
        return relocated;
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void spongeOnUpdate(CallbackInfo callbackInfo) {
        if (this.pendingVisibilityUpdate && !this.world.isRemote) {
            final EntityTracker entityTracker = ((WorldServer) this.world).getEntityTracker();
            final EntityTrackerEntry lookup = entityTracker.trackedEntityHashTable.lookup(this.getEntityId());
            if (this.visibilityTicks % 4 == 0) {
                if (this.isVanished) {
                    for (EntityPlayerMP entityPlayerMP : lookup.trackingPlayers) {
                        entityPlayerMP.connection.sendPacket(new SPacketDestroyEntities(this.getEntityId()));
                        if (((Object) this) instanceof EntityPlayerMP) {
                            entityPlayerMP.connection.sendPacket(
                                    new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, (EntityPlayerMP) (Object) this));
                        }
                    }
                } else {
                    this.visibilityTicks = 1;
                    this.pendingVisibilityUpdate = false;
                    for (EntityPlayerMP entityPlayerMP : SpongeImpl.getServer().getPlayerList().getPlayers()) {
                        if (((Object) this) == entityPlayerMP) {
                            continue;
                        }
                        if (((Object) this) instanceof EntityPlayerMP) {
                            Packet<?> packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, (EntityPlayerMP) (Object) this);
                            entityPlayerMP.connection.sendPacket(packet);
                        }
                        Packet<?> newPacket = lookup.createSpawnPacket(); // creates the spawn packet for us
                        entityPlayerMP.connection.sendPacket(newPacket);
                    }
                }
            }
            if (this.visibilityTicks > 0) {
                this.visibilityTicks--;
            } else {
                this.pendingVisibilityUpdate = false;
            }
        }
    }

    @Override
    public Vector3d getScale() {
        return Vector3d.ONE;
    }

    @Override
    public void setScale(Vector3d scale) {
        // do nothing, Minecraft doesn't properly support this yet
    }

    @Override
    public Transform<World> getTransform() {
        return new Transform<>(getWorld(), getPosition(), getRotation(), getScale());
    }

    @Override
    public boolean setTransform(Transform<World> transform) {
        checkNotNull(transform, "The transform cannot be null!");
        boolean result = setLocation(transform.getLocation());
        if (result) {
            setRotation(transform.getRotation());
            setScale(transform.getScale());
            return true;
        }

        return false;
    }

    @Override
    public boolean transferToWorld(World world, Vector3d position) {
        checkNotNull(world, "World was null!");
        checkNotNull(position, "Position was null!");
        return setLocation(new Location<>(world, position));
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setRotation(Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        if (isRemoved()) {
            return;
        }
        if (((Entity) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) this).connection != null) {
            // Force an update, this also set the rotation in this entity
            ((EntityPlayerMP) (Entity) this).connection.setPlayerLocation(getPosition().getX(), getPosition().getY(),
                getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), (Set) EnumSet.noneOf(RelativePositions.class));
        } else {
            if (!this.world.isRemote) { // We can't set the rotation update on client worlds.
                ((IMixinWorldServer) getWorld()).addEntityRotationUpdate((net.minecraft.entity.Entity) (Entity) this, rotation);
            }

            // Let the entity tracker do its job, this just updates the variables
            shadow$setRotation((float) rotation.getY(), (float) rotation.getX());
        }
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        final AxisAlignedBB boundingBox = getEntityBoundingBox();
        if (boundingBox == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(VecHelper.toSponge(boundingBox));
        } catch (IllegalArgumentException exception) {
            // Bounding box is degenerate, the entity doesn't actually have one
            return Optional.empty();
        }
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public boolean isRemoved() {
        return this.isDead;
    }

    @Override
    public boolean isLoaded() {
        // TODO - add flag for entities loaded/unloaded into world
        return !isRemoved();
    }

    @Override
    public void remove() {
        this.isDead = true;
    }

    @Override
    public boolean damage(double damage, org.spongepowered.api.event.cause.entity.damage.source.DamageSource damageSource, Cause cause) {
        if (!(damageSource instanceof DamageSource)) {
            SpongeImpl.getLogger().error("An illegal DamageSource was provided in the cause! The damage source must extend AbstractDamageSource!");
            return false;
        }
        // todo hook the damage entity event with the cause.
        return attackEntityFrom((DamageSource) damageSource, (float) damage);
    }

    @Override
    public boolean isTeleporting() {
        return this.teleporting;
    }

    @Override
    public net.minecraft.entity.Entity getTeleportVehicle() {
        return this.teleportVehicle;
    }

    @Override
    public void setIsTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    @Override
    public void setTeleportVehicle(net.minecraft.entity.Entity vehicle) {
        this.teleportVehicle = vehicle;
    }

    @Override
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Intrinsic
    public List<Entity> entity$getPassengers() {
        return (List) shadow$getPassengers();
    }

    @Override
    public Optional<Entity> getVehicle() {
        return Optional.ofNullable((Entity) getRidingEntity());
    }

    @Override
    public Entity getBaseVehicle() {
        return (Entity) this.getLowestRidingEntity();
    }

    @Override
    public boolean hasPassenger(Entity entity) {
        checkNotNull(entity);
        return entity.getPassengers().contains(this);
    }

    @Override
    public boolean addPassenger(Entity entity) {
        checkNotNull(entity);
        if (entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot add entity %s as a passenger of %s, because the former already has the latter as a passenger!", entity, this));
        }

        return ((net.minecraft.entity.Entity) entity).startRiding((net.minecraft.entity.Entity) (Object) this, true);
    }

    @Override
    public void removePassenger(Entity entity) {
        checkNotNull(entity);
        if (!entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot remove entity %s, because it is not a passenger of %s ", entity, this));
        }

        ((net.minecraft.entity.Entity) entity).dismountRidingEntity();
    }

    @Override
    public void clearPassengers() {
        this.removePassengers();
    }

    @Override
    public boolean setVehicle(@Nullable Entity entity) {
        if (getRidingEntity() == null && entity == null) {
            return false;
        }
        if (getRidingEntity() != null) {
            dismountRidingEntity();
            return true;
        }
        return entity != null && entity.addPassenger(this);
    }


    /**
     * @author blood - May 28th, 2016
     * @author gabizou - May 31st, 2016 - Update for 1.9.4
     *
     * @reason - rewritten to support {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param toDimensionId The id of target dimension.
     */
    @Nullable
    @Overwrite
    public net.minecraft.entity.Entity changeDimension(int toDimensionId) {
        if (!this.world.isRemote && !this.isDead) {
            // Sponge Start - Handle teleportation solely in TrackingUtil where everything can be debugged.
            return EntityUtil.transferEntityToDimension(this, toDimensionId);
            // Sponge End
        }
        return null;
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #writeToNbt}.
     *
     * <p> This makes it easier for other entity mixins to override writeToNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write
     *        to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     *
     * <p> This makes it easier for other entity mixins to override readFromNbt
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read
     *        from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (this.isConstructing) {
            firePostConstructEvents(); // Do this early as possible
        }
        this.readFromNbt(this.getSpongeData());
    }

    @Override
    public boolean validateRawData(DataMap container) {
        return false;
    }

    @Override
    public void setRawData(DataMap container) throws InvalidDataException {

    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    @Override
    public void readFromNbt(NBTTagCompound compound) {
        if (this instanceof IMixinCustomDataHolder) {
            if (compound.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_LIST)) {
                final NBTTagList list = compound.getTagList(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_COMPOUND);
                final DataList dataList = new MemoryDataList();
                if (list != null && list.tagCount() != 0) {
                    for (int i = 0; i < list.tagCount(); i++) {
                        final NBTTagCompound internal = list.getCompoundTagAt(i);
                        dataList.add(NbtTranslator.getInstance().translateFrom(internal));
                    }
                }
                try {
                    final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(dataList);
                    final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
                    for (DataManipulator<?, ?> manipulator : manipulators) {
                        offer(manipulator);
                    }
                    if (!transaction.failedData.isEmpty()) {
                        ((IMixinCustomDataHolder) this).addFailedData(transaction.failedData);
                    }
                } catch (InvalidDataException e) {
                    SpongeImpl.getLogger().error("Could not translate custom plugin data! ", e);
                }
            }
            if (compound.hasKey(NbtDataUtil.FAILED_CUSTOM_DATA, NbtDataUtil.TAG_LIST)) {
                final NBTTagList list = compound.getTagList(NbtDataUtil.FAILED_CUSTOM_DATA, NbtDataUtil.TAG_COMPOUND);
                final DataList dataList = new MemoryDataList();
                if (list != null && list.tagCount() != 0) {
                    for (int i = 0; i < list.tagCount(); i++) {
                        final NBTTagCompound internal = list.getCompoundTagAt(i);
                        dataList.add(NbtTranslator.getInstance().translateFrom(internal));
                    }
                }
                // Re-attempt to deserialize custom data
                final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(dataList);
                final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
                for (DataManipulator<?, ?> manipulator : manipulators) {
                    offer(manipulator);
                }
                if (!transaction.failedData.isEmpty()) {
                    ((IMixinCustomDataHolder) this).addFailedData(transaction.failedData);
                }
            }
        }
        if (this instanceof IMixinGriefer && ((IMixinGriefer) this).isGriefer() && compound.hasKey(NbtDataUtil.CAN_GRIEF)) {
            ((IMixinGriefer) this).setCanGrief(compound.getBoolean(NbtDataUtil.CAN_GRIEF));
        }
    }

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    @Override
    public void writeToNbt(NBTTagCompound compound) {
        if (this instanceof IMixinCustomDataHolder) {
            final List<DataManipulator<?, ?>> manipulators = ((IMixinCustomDataHolder) this).getCustomManipulators();
            if (!manipulators.isEmpty()) {
                DataList manipulatorViews = new MemoryDataList();
                DataUtil.serializeManipulatorList(manipulatorViews, manipulators);
                final NBTTagList manipulatorTagList = new NBTTagList();
                manipulatorViews.forEachKey(i -> manipulatorTagList.appendTag(
                        NbtTranslator.getInstance().translateData(
                                manipulatorViews.getMap(i).get())));
                compound.setTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            }
            final List<DataView> failedData = ((IMixinCustomDataHolder) this).getFailedData();
            if (!failedData.isEmpty()) {
                final NBTTagList failedList = new NBTTagList();
                for (DataView failedDatum : failedData) {
                    failedList.appendTag(NbtTranslator.getInstance().translateData(failedDatum));
                }
                compound.setTag(NbtDataUtil.FAILED_CUSTOM_DATA, failedList);
            }
        }
        if (this instanceof IMixinGriefer && ((IMixinGriefer) this).isGriefer()) {
            compound.setBoolean(NbtDataUtil.CAN_GRIEF, ((IMixinGriefer) this).canGrief());
        }
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        final Transform<World> transform = getTransform();
        container
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(DataQueries.ENTITY_CLASS, this.getClass().getName())
            .set(Queries.WORLD_ID, transform.getExtent().getUniqueId().toString())
            .set(DataQueries.ENTITY_TYPE, this.entityType.getId());
        DataUtil.setVector3d(container.createMap(DataQueries.SNAPSHOT_WORLD_POSITION), transform.getPosition());
        DataUtil.setVector3d(container.createMap(DataQueries.ENTITY_ROTATION), transform.getRotation());
        DataUtil.setVector3d(container.createMap(DataQueries.ENTITY_SCALE), transform.getScale());

        container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(
                NbtDataUtil.filterSpongeCustomData( // We must filter the custom data so it isn't stored twice
                    writeToNBT(new NBTTagCompound()))));
        final Collection<DataManipulator<?, ?>> manipulators = getContainers();
        if (!manipulators.isEmpty()) {
            DataUtil.serializeManipulatorList(container.createList(DataQueries.DATA_MANIPULATORS), manipulators);
        }
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.supplyVanillaManipulators(list);
        if (this instanceof IMixinCustomDataHolder && ((IMixinCustomDataHolder) this).hasManipulators()) {
            list.addAll(((IMixinCustomDataHolder) this).getCustomManipulators());
        }
        return list;
    }

    @Override
    public Entity copy() {
        if ((Object) this instanceof Player) {
            throw new IllegalArgumentException("Cannot copy player entities!");
        }
        try {
            final NBTTagCompound compound = new NBTTagCompound();
            writeToNBT(compound);
            net.minecraft.entity.Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(this.entityType.getId()), this.world);
            compound.setUniqueId(NbtDataUtil.UUID, entity.getUniqueID());
            entity.readFromNBT(compound);
            return (Entity) entity;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not copy the entity:", e);
        }
    }

    @Override
    public Optional<User> getTrackedPlayer(String nbtKey) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getCreatorUser() {
        return Optional.empty();
    }

    @Override
    public Optional<User> getNotifierUser() {
        return Optional.empty();
    }

    @Override
    public void trackEntityUniqueId(String nbtKey, @Nullable UUID uuid) {
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty();
    }

    @Override
    public void setCreator(@Nullable UUID uuid) {
    }

    @Override
    public void setNotifier(@Nullable UUID uuid) {
    }

    @Override
    public void setImplVelocity(Vector3d velocity) {
        this.motionX = checkNotNull(velocity).getX();
        this.motionY = velocity.getY();
        this.motionZ = velocity.getZ();
        this.velocityChanged = true;
    }

    @Override
    public Vector3d getVelocity() {
        return new Vector3d(this.motionX, this.motionY, this.motionZ);
    }

    @Redirect(method = "move",at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"
                                                                        + "onEntityWalk(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    public void onEntityCollideWithBlock(Block block, net.minecraft.world.World world, BlockPos pos, net.minecraft.entity.Entity entity) {
        // if block can't collide, return
        if (!((IMixinBlock) block).hasCollideLogic()) {
            return;
        }

        if (world.isRemote) {
            block.onEntityWalk(world, pos, entity);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.NONE)) {
            block.onEntityWalk(world, pos, entity);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }

    @Redirect(method = "doBlockCollisions", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onEntityCollidedWithBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V")) // doBlockCollisions
    public void onEntityCollideWithBlockState(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity) {
        // if block can't collide, return
        if (!((IMixinBlock) block).hasCollideWithStateLogic()) {
            return;
        }

        if (world.isRemote) {
            block.onEntityCollidedWithBlock(world, pos, state, entity);
            return;
        }

        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.NONE)) {
            block.onEntityCollidedWithBlock(world, pos, state, entity);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }

    @Redirect(method = "updateFallState", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    public void onBlockFallenUpon(Block block, net.minecraft.world.World world, BlockPos pos, net.minecraft.entity.Entity entity, float fallDistance) {
        if (world.isRemote) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.UP)) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }

    @Override
    public Translation getTranslation() {
        return getType().getTranslation();
    }

    private boolean collision = false;
    private boolean untargetable = false;
    private boolean isVanished = false;

    private boolean pendingVisibilityUpdate = false;
    private int visibilityTicks = 0;

    @Override
    public boolean isVanished() {
        return this.isVanished;
    }

    @Override
    public void setVanished(boolean vanished) {
        this.isVanished = vanished;
        this.pendingVisibilityUpdate = true;
        this.visibilityTicks = 20;
    }

    @Override
    public boolean ignoresCollision() {
        return this.collision;
    }

    @Override
    public void setIgnoresCollision(boolean prevents) {
        this.collision = prevents;
    }

    @Override
    public boolean isUntargetable() {
        return this.untargetable;
    }

    @Override
    public void setUntargetable(boolean untargetable) {
        this.untargetable = untargetable;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @updated gabizou - January 27th, 2016 - Rewrite to a redirect
     *
     * This prevents sounds from being sent to the server by entities that are vanished
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSilent()Z"))
    public boolean checkIsSilentOrInvis(net.minecraft.entity.Entity entity) {
        return entity.isSilent() || this.isVanished;
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;noClip:Z", opcode = Opcodes.GETFIELD))
    private boolean spongeApplyEntityCollisionCheckVanish(net.minecraft.entity.Entity entity) {
        return entity.noClip || ((IMixinEntity) entity).isVanished();
    }

    @Redirect(method = "resetHeight", at = @At(value = "INVOKE", target = WORLD_SPAWN_PARTICLE))
    public void spawnParticle(net.minecraft.world.World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord,
            double xOffset, double yOffset, double zOffset, int ... p_175688_14_) {
        if (!this.isVanished) {
            this.world.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = WORLD_SPAWN_PARTICLE))
    public void runningSpawnParticle(net.minecraft.world.World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord,
            double xOffset, double yOffset, double zOffset, int ... p_175688_14_) {
        if (!this.isVanished) {
            this.world.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    @Nullable
    @Override
    public Text getDisplayNameText() {
        return this.displayName;
    }

    private boolean skipSettingCustomNameTag = false;

    @Override
    public void setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;

        this.skipSettingCustomNameTag = true;
        if (this.displayName == null) {
            this.setCustomNameTag("");
        } else {
            this.setCustomNameTag(SpongeTexts.toLegacy(this.displayName));
        }

        this.skipSettingCustomNameTag = false;
    }

    @Inject(method = "setCustomNameTag", at = @At("RETURN"))
    public void onSetCustomNameTag(String name, CallbackInfo ci) {
        if (!this.skipSettingCustomNameTag) {
            this.displayName = SpongeTexts.fromLegacy(name);
        }
    }

    @Override
    public boolean canSee(Entity entity) {
        // note: this implementation will be changing with contextual data
        Optional<Boolean> optional = entity.get(Keys.VANISH);
        return (!optional.isPresent() || !optional.get()) && !((IMixinEntity) entity).isVanished();
    }

    /**
     * @author gabizou - January 30th, 2016
     * @author blood - May 12th, 2016
     * @author gabizou - June 2nd, 2016
     *
     * @reason Rewrites the method entirely for several reasons:
     * 1) If we are in a forge environment, we do NOT want forge to be capturing the item entities, because we handle them ourselves
     * 2) If we are in a client environment, we should not perform any sort of processing whatsoever.
     * 3) This method is entirely managed from the standpoint where our events have final say, as per usual.
     *
     * @param itemStackIn
     * @param offsetY
     * @return
     */
    @Inject(method = "entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;", at = @At("HEAD"), cancellable = true)
    public void spongeEntityDropItem(net.minecraft.item.ItemStack itemStackIn, float offsetY, CallbackInfoReturnable<EntityItem> returnable) {
        // Gotta stick with the client side handling things
        if (this.world.isRemote) {
            if (itemStackIn.getCount() != 0 && itemStackIn.getItem() != null) {
                EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY + (double) offsetY, this.posZ, itemStackIn);
                entityitem.setDefaultPickupDelay();
                this.world.spawnEntity(entityitem);
                returnable.setReturnValue(entityitem);
                return;
            }
            returnable.setReturnValue(null);
            return;
        }
        returnable.setReturnValue(EntityUtil.entityOnDropItem((net.minecraft.entity.Entity) (Object) this, itemStackIn, offsetY));
    }

    @Override
    public void setCurrentCollidingBlock(BlockState state) {
        this.currentCollidingBlock = state;
    }

    @Override
    public BlockState getCurrentCollidingBlock() {
        if (this.currentCollidingBlock == null) {
            return (BlockState) Blocks.AIR.getDefaultState();
        }
        return this.currentCollidingBlock;
    }

    @Override
    public BlockPos getLastCollidedBlockPos() {
        return this.lastCollidedBlockPos;
    }

    @Override
    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getEntityTiming(this);
        }
        return this.timing;
    }

    @Override
    public EntityArchetype createArchetype() {
        return new SpongeEntityArchetypeBuilder().from(this).build();
    }

    @Override
    public Value<Boolean> gravity() {
        return this.getValue(Keys.HAS_GRAVITY).get();
    }
}

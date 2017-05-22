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
package org.spongepowered.common.mixin.core.tileentity;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataList;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntity.class)
@Implements(@Interface(iface = IMixinTileEntity.class, prefix = "tile$"))
public abstract class MixinTileEntity implements TileEntity, IMixinTileEntity {

    private final TileEntityType tileType = SpongeImpl.getRegistry().getTranslated(this.getClass(), TileEntityType.class);
    // uses different name to not clash with SpongeForge
    private final boolean isTileVanilla = getClass().getName().startsWith("net.minecraft.");
    private Timing timing;
    private LocatableBlock locatableBlock;

    @Shadow protected boolean tileEntityInvalid;
    @Shadow protected net.minecraft.world.World world;
    @Shadow private int blockMetadata;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos getPos();
    @Shadow public abstract Block getBlockType();
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Override @Shadow public abstract void markDirty();

    @Intrinsic
    public void tile$markDirty() {
        this.markDirty();
    }

    @Inject(method = "markDirty", at = @At(value = "HEAD"))
    public void onMarkDirty(CallbackInfo ci) {
        if (this.world != null && !this.world.isRemote) {
            // This handles transfers to this TE from a source such as a Hopper
            CauseTracker.getInstance().getCurrentPhaseData().context.getSource(TileEntity.class).ifPresent(currentTick -> {
                if (currentTick != this) {
                    net.minecraft.tileentity.TileEntity te = (net.minecraft.tileentity.TileEntity) currentTick;
//                    world.getCauseTracker().trackTargetBlockFromSource(te, te.getPos(), this.getBlockType(), this.pos, PlayerTracker.Type.NOTIFIER);
                }
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/Class;)V", at = @At(value = "RETURN"))
    private static void onRegister(String name, Class clazz, CallbackInfo callbackInfo) {
        if (clazz != null) {
            TileEntityTypeRegistryModule.getInstance().doTileEntityRegistration(clazz, name);
        }
    }

    @Override
    public Location<World> getLocation() {
        return new Location<>((World) this.world, VecHelper.toVector3i(this.getPos()));
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        container
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Queries.WORLD_ID, ((World) this.world).getUniqueId().toString())
            .set(Queries.POSITION_X, this.getPos().getX())
            .set(Queries.POSITION_Y, this.getPos().getY())
            .set(Queries.POSITION_Z, this.getPos().getZ())
            .set(DataQueries.BLOCK_ENTITY_TILE_TYPE, this.tileType.getId());
        final NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(compound));
        final Collection<DataManipulator<?, ?>> manipulators = getContainers();
        if (!manipulators.isEmpty()) {
            DataUtil.serializeManipulatorList(container.createList(DataQueries.DATA_MANIPULATORS), manipulators);
        }
    }

    @Override
    public boolean validateRawData(DataMap container) {
        // TODO: This checks to see if the right keys are exist, but not the right types!
        // TODO: I should really depricate DataView#contains()
        return container.contains(Queries.WORLD_ID)
            && container.contains(Queries.POSITION_X)
            && container.contains(Queries.POSITION_Y)
            && container.contains(Queries.POSITION_Z)
            && container.contains(DataQueries.BLOCK_ENTITY_TILE_TYPE)
            && container.contains(DataQueries.UNSAFE_NBT);
    }

    @Override
    public void setRawData(DataMap container) throws InvalidDataException {
        //TODO: wat? empty?
    }

    @Override
    public boolean isValid() {
        return !this.tileEntityInvalid;
    }

    @Override
    public void setValid(boolean valid) {
        this.tileEntityInvalid = valid;
    }

    @Override
    public final TileEntityType getType() {
        return this.tileType;
    }

    @Override
    public BlockState getBlock() {
        return (BlockState) this.world.getBlockState(this.getPos());
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #writeToNbt}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/tileentity/TileEntity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readFromNbt without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.readFromNbt(this.getSpongeData());
    }

    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
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
    }

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    @Override
    public void writeToNbt(NBTTagCompound compound) {
        if (this instanceof IMixinCustomDataHolder) {
            final List<DataManipulator<?, ?>> customManipulators = ((IMixinCustomDataHolder) this).getCustomManipulators();
            if (!customManipulators.isEmpty()) {
                final DataList manipulatorViews = new MemoryDataList();
                DataUtil.serializeManipulatorList(manipulatorViews, customManipulators);
                final NBTTagList manipulatorTagList = new NBTTagList();
                manipulatorViews.forEachKey(i ->
                        manipulatorViews.getMap(i).ifPresent(m ->
                                manipulatorTagList.appendTag(NbtTranslator.getInstance().translateData(m))));
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
    }

    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {

    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.supplyVanillaManipulators(list);
        if (this instanceof IMixinCustomDataHolder) {
            list.addAll(((IMixinCustomDataHolder) this).getCustomManipulators());
        }
        return list;
    }

    @Override
    public boolean isVanilla() {
        return this.isTileVanilla;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getTileEntityTiming((org.spongepowered.api.block.tileentity.TileEntity) (Object) this);
        }
        return this.timing;
    }

    @Override
    public TileEntityArchetype createArchetype() {
        return new SpongeTileEntityArchetypeBuilder().tile(this).build();
    }

    @Override
    public LocatableBlock getLocatableBlock() {
        if (this.locatableBlock == null) {
            this.locatableBlock = LocatableBlock.builder()
                    .location(new Location<World>((World) this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()))
                    .state(this.getBlock())
                    .build();
        }

        return this.locatableBlock;
    }
}

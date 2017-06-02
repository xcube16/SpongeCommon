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
package org.spongepowered.common.block;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeBlockSnapshotBuilder extends AbstractDataBuilder<BlockSnapshot> implements BlockSnapshot.Builder {

    BlockState blockState;
    BlockState extendedState;
    UUID worldUuid;
    UUID creatorUuid;
    UUID notifierUuid;
    Vector3i coords;
    @Nullable List<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable NBTTagCompound compound;


    public SpongeBlockSnapshotBuilder() {
        super(BlockSnapshot.class, 1);
    }

    @Override
    public SpongeBlockSnapshotBuilder world(WorldProperties worldProperties) {
        this.worldUuid = checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    public SpongeBlockSnapshotBuilder worldId(UUID worldUuid) {
        this.worldUuid = checkNotNull(worldUuid);
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder blockState(BlockState blockState) {
        this.blockState = checkNotNull(blockState);
        return this;
    }

    public SpongeBlockSnapshotBuilder extendedState(BlockState extendedState) {
        this.extendedState = checkNotNull(extendedState);
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder position(Vector3i position) {
        this.coords = checkNotNull(position);
        if (this.compound != null) {
            this.compound.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_X, position.getX());
            this.compound.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_Y, position.getY());
            this.compound.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_Z, position.getZ());
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder from(Location<World> location) {
        this.blockState = location.getBlock();
        this.worldUuid = location.getExtent().getUniqueId();
        this.coords = location.getBlockPosition();
        if (this.blockState.getType() instanceof ITileEntityProvider) {
            if (location.hasTileEntity()) {
                this.compound = new NBTTagCompound();
                ((TileEntity) location.getTileEntity().get()).writeToNBT(this.compound);
                this.manipulators = location.getContainers().stream().map(DataManipulator::asImmutable).collect(Collectors.toList());
            }
        }
        return this;
    }

    @Override
    public BlockSnapshot.Builder creator(UUID uuid) {
        this.creatorUuid = checkNotNull(uuid);
        return this;
    }

    @Override
    public BlockSnapshot.Builder notifier(UUID uuid) {
        this.notifierUuid = checkNotNull(uuid);
        return this;
    }

    public SpongeBlockSnapshotBuilder unsafeNbt(NBTTagCompound compound) {
        this.compound = compound.copy();
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder add(DataManipulator<?, ?> manipulator) {
        return add((ImmutableDataManipulator<?, ?>) checkNotNull(manipulator, "manipulator").asImmutable());
    }

    @Override
    public SpongeBlockSnapshotBuilder add(ImmutableDataManipulator<?, ?> manipulator) {
        checkNotNull(manipulator, "manipulator");
        if (this.manipulators == null) {
            this.manipulators = Lists.newArrayList();
        }
        for (Iterator<ImmutableDataManipulator<?, ?>> iterator = this.manipulators.iterator(); iterator.hasNext();) {
            final ImmutableDataManipulator<?, ?> existing = iterator.next();
            if (manipulator.getClass().isInstance(existing)) {
                iterator.remove();
            }
        }
        this.manipulators.add(manipulator);
        return this;
    }

    @Override
    public <V> BlockSnapshot.Builder add(Key<? extends BaseValue<V>> key, V value) {
        checkNotNull(key, "key");
        checkState(this.blockState != null);
        this.blockState = this.blockState.with(key, value).orElse(this.blockState);
        if(this.extendedState != null) {
            this.extendedState = this.extendedState.with(key, value).orElse(this.extendedState);
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder from(BlockSnapshot holder) {
        this.blockState = holder.getState();
        this.worldUuid = holder.getWorldUniqueId();
        if (holder.getCreator().isPresent()) {
            this.creatorUuid = holder.getCreator().get();
        }
        if (holder.getNotifier().isPresent()) {
            this.notifierUuid = holder.getNotifier().get();
        }
        this.coords = holder.getPosition();
        this.manipulators = Lists.newArrayList(holder.getManipulators());
        if (holder instanceof SpongeBlockSnapshot) {
            if (((SpongeBlockSnapshot) holder).compound != null) {
                this.compound = ((SpongeBlockSnapshot) holder).compound.copy();
            }
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder reset() {
        this.blockState = BlockTypes.AIR.getDefaultState();
        this.worldUuid = null;
        this.creatorUuid = null;
        this.notifierUuid = null;
        this.coords = null;
        this.manipulators = null;
        this.compound = null;
        return this;
    }

    @Override
    public BlockSnapshot build() {
        checkState(this.blockState != null);
        if (this.extendedState == null) {
            this.extendedState = this.blockState;
        }
        return new SpongeBlockSnapshot(this);
    }

    @Override
    protected Optional<BlockSnapshot> buildContent(DataMap container) throws InvalidDataException {
        if (!container.contains(DataQueries.BLOCK_STATE, Queries.WORLD_ID, DataQueries.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        checkDataExists(container, DataQueries.BLOCK_STATE);
        checkDataExists(container, Queries.WORLD_ID);
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
        final UUID worldUuid = UUID.fromString(container.getString(Queries.WORLD_ID).get());
        final Vector3i coordinate = DataUtil.getVector3i(container.getMap(DataQueries.SNAPSHOT_WORLD_POSITION).get());
        Optional<String> creatorUuid = container.getString(Queries.CREATOR_ID);
        Optional<String> notifierUuid = container.getString(Queries.NOTIFIER_ID);

        // We now reconstruct the custom data and all extra data.
        final BlockState blockState = container.getObject(DataQueries.BLOCK_STATE, BlockState.class).get();
        BlockState extendedState = null;
        if (container.contains(DataQueries.BLOCK_EXTENDED_STATE)) {
            extendedState = container.getObject(DataQueries.BLOCK_EXTENDED_STATE, BlockState.class).get();
        } else {
            extendedState = blockState;
        }

        builder.blockState(blockState)
                .extendedState(extendedState)
                .position(coordinate)
                .worldId(worldUuid);
        creatorUuid.ifPresent(s -> builder.creator(UUID.fromString(s)));
        notifierUuid.ifPresent(s -> builder.notifier(UUID.fromString(s)));

        container.getMap(DataQueries.UNSAFE_NBT)
                .map(dataMap -> NbtTranslator.getInstance().translateData(dataMap))
                .ifPresent(builder::unsafeNbt);

        container.getList(DataQueries.SNAPSHOT_TILE_DATA)
                .ifPresent(list -> DataUtil.deserializeImmutableManipulatorList(list).forEach(builder::add));

        return Optional.of(new SpongeBlockSnapshot(builder));
    }
}

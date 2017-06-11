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
package org.spongepowered.common.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeEntitySnapshotBuilder extends AbstractDataBuilder<EntitySnapshot> implements EntitySnapshot.Builder {

    UUID worldId;
    Vector3d position;
    Vector3d rotation;
    Vector3d scale;
    EntityType entityType;

    @Nullable UUID entityId;
    @Nullable List<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable NBTTagCompound compound;
    @Nullable List<ImmutableValue<?>> values;
    @Nullable WeakReference<Entity> entityReference;

    public SpongeEntitySnapshotBuilder() {
        super(EntitySnapshot.class, 1);
    }

    @Override
    public SpongeEntitySnapshotBuilder world(WorldProperties worldProperties) {
        this.worldId = checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    public SpongeEntitySnapshotBuilder worldId(UUID worldUuid) {
        this.worldId = checkNotNull(worldUuid);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder type(EntityType entityType) {
        this.entityType = checkNotNull(entityType);
        this.compound = null;
        this.manipulators = null;
        this.entityId = null;
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder position(Vector3d position) {
        this.position = checkNotNull(position);
        return this;
    }

    public SpongeEntitySnapshotBuilder rotation(Vector3d rotation) {
        this.rotation = checkNotNull(rotation);
        return this;
    }

    public SpongeEntitySnapshotBuilder scale(Vector3d scale) {
        this.scale = checkNotNull(scale);
        return this;
    }

    public SpongeEntitySnapshotBuilder id(UUID entityId) {
        this.entityId = checkNotNull(entityId);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SpongeEntitySnapshotBuilder from(Entity entity) {
        reset();
        this.entityReference = new WeakReference<>(entity);
        this.worldId = entity.getWorld().getUniqueId();
        this.position = entity.getTransform().getPosition();
        this.rotation = entity.getTransform().getRotation();
        this.scale = entity.getTransform().getScale();
        this.entityType = entity.getType();
        this.entityId = entity.getUniqueId();
        this.manipulators = Lists.newArrayList();
        for (DataManipulator<?, ?> manipulator : entity.getContainers()) {
            addManipulator((ImmutableDataManipulator) manipulator.asImmutable());
        }
        this.compound = new NBTTagCompound();
        ((net.minecraft.entity.Entity) entity).writeToNBT(this.compound);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SpongeEntitySnapshotBuilder add(DataManipulator<?, ?> manipulator) {
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        return add((ImmutableDataManipulator) manipulator.asImmutable());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SpongeEntitySnapshotBuilder add(ImmutableDataManipulator<?, ?> manipulator) {
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getImmutableProcessor((Class) manipulator.getClass());
        if (optional.isPresent()) {
            if (optional.get().supports(this.entityType)) {
                addManipulator(manipulator);
            } else {
                return this;
            }
        }
        return this;
    }

    @Override
    public <V> EntitySnapshot.Builder add(Key<? extends BaseValue<V>> key, V value) {
        checkNotNull(key, "key");
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        if (this.values == null) {
            this.values = Lists.newArrayList();
        }
        this.values.add(new ImmutableSpongeValue<>(key, value));
        return this;
    }

    private void addManipulator(ImmutableDataManipulator<?, ?> manipulator) {
        if (this.manipulators == null) {
            this.manipulators = Lists.newArrayList();
        }
        int replaceIndex = -1;
        for (ImmutableDataManipulator<?, ?> existing : this.manipulators) {
            replaceIndex++;
            if (existing.getClass().equals(manipulator.getClass())) {
                break;
            }
        }
        if (replaceIndex != -1) {
            this.manipulators.remove(replaceIndex);
        }
        this.manipulators.add(manipulator);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SpongeEntitySnapshotBuilder from(EntitySnapshot holder) {
        this.entityType = holder.getType();
        this.worldId = holder.getWorldUniqueId();
        if (holder.getUniqueId().isPresent()) {
            this.entityId = holder.getUniqueId().get();
        }
        this.position = holder.getPosition().toDouble();
        final Optional<Transform<World>> optional = holder.getTransform();
        if (optional.isPresent()) {
            this.position = optional.get().getPosition();
            this.rotation = optional.get().getRotation();
            this.scale = optional.get().getScale();
        }
        this.manipulators = Lists.newArrayList();
        for (ImmutableDataManipulator<?, ?> manipulator : holder.getContainers()) {
            add((ImmutableDataManipulator) manipulator);
        }
        if (holder instanceof SpongeEntitySnapshot) {
            this.compound = ((SpongeEntitySnapshot) holder).getCompound().orElse(null);
        }
        return this;
    }

    public SpongeEntitySnapshotBuilder from(net.minecraft.entity.Entity minecraftEntity) {
        this.entityType = ((Entity) minecraftEntity).getType();
        this.worldId = ((Entity) minecraftEntity).getWorld().getUniqueId();
        this.entityId = minecraftEntity.getUniqueID();
        final Transform<World> transform = ((Entity) minecraftEntity).getTransform();
        this.position = transform.getPosition();
        this.rotation = transform.getRotation();
        this.scale = transform.getScale();
        this.manipulators = Lists.newArrayList();
        for (DataManipulator<?, ?> manipulator : ((Entity) minecraftEntity).getContainers()) {
            addManipulator(manipulator.asImmutable());
        }
        this.compound = new NBTTagCompound();
        minecraftEntity.writeToNBT(this.compound);
        return this;
    }

    public SpongeEntitySnapshotBuilder unsafeCompound(NBTTagCompound compound) {
        this.compound = checkNotNull(compound).copy();
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder reset() {
        this.worldId = null;
        this.entityId = null;
        this.position = null;
        this.rotation = null;
        this.scale = null;
        this.entityType = null;
        this.entityId = null;
        this.manipulators = null;
        this.compound = null;
        this.entityReference = null;
        return this;
    }

    @Override
    public EntitySnapshot build() {
        EntitySnapshot snapshot = new SpongeEntitySnapshot(this);
        if(this.values != null) {
            for (ImmutableValue<?> value : this.values) {
                snapshot = snapshot.with(value).orElse(snapshot);
            }
        }
        return snapshot;
    }

    @Override
    protected Optional<EntitySnapshot> buildContent(DataMap container) throws InvalidDataException {
        Optional<UUID> worldId = container.getString(Queries.WORLD_ID).map(UUID::fromString);
        Optional<EntityType> type = container.getObject(DataQueries.ENTITY_TYPE, EntityType.class);
        Optional<Vector3d> rotation = container.getMap(DataQueries.ENTITY_ROTATION).map(DataUtil::getVector3d);
        Optional<Vector3d> scale = container.getMap(DataQueries.ENTITY_SCALE).map(DataUtil::getVector3d);
        Optional<Vector3d> pos = container.getMap(DataQueries.SNAPSHOT_WORLD_POSITION).map(DataUtil::getVector3d);
        if (!worldId.isPresent() || !type.isPresent() || !rotation.isPresent() || !scale.isPresent() || !pos.isPresent()) {
            return Optional.empty();
        }

        this.worldId = worldId.get();
        this.position = pos.get();
        this.rotation = rotation.get();
        this.scale = scale.get();
        this.entityType = type.get();

        this.manipulators = container.getList(DataQueries.DATA_MANIPULATORS)
                .map(DataUtil::deserializeImmutableManipulatorList)
                .orElseGet(ImmutableList::of);
        container.getMap(DataQueries.UNSAFE_NBT).ifPresent(m ->
                this.compound = NbtTranslator.getInstance().translateData(m));
        container.getString(DataQueries.ENTITY_ID).ifPresent(s ->
                this.entityId = UUID.fromString(s));
        return Optional.of(build());
    }
}

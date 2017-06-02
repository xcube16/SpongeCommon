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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.entity.EntityTypes.UNKNOWN;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataMap;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class SpongeEntityArchetypeBuilder extends AbstractDataBuilder<EntityArchetype> implements EntityArchetype.Builder {

    EntityType entityType = UNKNOWN;
    DataMap entityData = new MemoryDataMap();

    public SpongeEntityArchetypeBuilder() {
        super(EntityArchetype.class, DataVersions.EntityArchetype.BASE_VERSION);
    }

    @Override
    public EntityArchetype.Builder reset() {
        this.entityType = UNKNOWN;
        this.entityData = new MemoryDataMap();
        return this;
    }

    @Override
    public EntityArchetype.Builder from(EntityArchetype value) {
        this.entityType = value.getType();
        this.entityData = value.getEntityData();
        return this;
    }

    @Override
    protected Optional<EntityArchetype> buildContent(DataMap container) throws InvalidDataException {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        builder.type(container.getObject(DataQueries.EntityArchetype.ENTITY_TYPE, EntityType.class)
                .orElseThrow(() -> new InvalidDataException("Could not deserialize a TileEntityType!")));
        builder.entityData(container.getMap(DataQueries.EntityArchetype.ENTITY_DATA)
                .orElseThrow(() -> new InvalidDataException("No DataView found for the TileEntity data tag!")));
        return Optional.of(builder.build());
    }

    @Override
    public EntityArchetype.Builder type(EntityType type) {
        checkNotNull(type, "EntityType cannot be null!");
        checkArgument(type != UNKNOWN, "EntityType cannot be set to UNKNOWN!");
        if (this.entityType != type) {
            this.entityData = new MemoryDataMap();
        }
        this.entityType = type;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(Entity entity) {
        checkNotNull(entity, "Cannot build an EntityArchetype for a null entity!");
        this.entityType = checkNotNull(entity.getType(), "Entity is returning a null EntityType!");
        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final NBTTagCompound compound = new NBTTagCompound();
        minecraftEntity.writeToNBT(compound);
        compound.removeTag(NbtDataUtil.UUID);
        this.entityData = NbtTranslator.getInstance().translate(compound);
        return this;
    }

    @Override
    public EntityArchetype.Builder entityData(DataMap view) {
        checkNotNull(view, "Provided DataMap cannot be null!");
        final DataMap copy = view.copy();
        DataUtil.getValidators(Validations.ENTITY).validate(copy);
        this.entityData = copy;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityArchetype.Builder setData(DataManipulator<?, ?> manipulator) {
        DataUtil.getRawNbtProcessor(NbtDataTypes.ENTITY, manipulator.getClass())
                .ifPresent(processor -> processor.storeToView(this.entityData, manipulator));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> EntityArchetype.Builder set(V value) {
        DataUtil.getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, value.getKey())
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> EntityArchetype.Builder set(Key<V> key, E value) {
        DataUtil.getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, key)
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @Override
    public EntityArchetype build() {
        checkState(this.entityType != UNKNOWN);
        this.entityData.remove(DataQuery.of("Pos"));
        this.entityData.remove(DataQuery.of("UUID"));
        return new SpongeEntityArchetype(this);
    }
}

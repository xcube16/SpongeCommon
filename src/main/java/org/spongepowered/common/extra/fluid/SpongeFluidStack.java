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
package org.spongepowered.common.extra.fluid;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeFluidStack implements FluidStack {

    private FluidType fluidType;
    private int volume;
    @Nullable private DataMap extraData;

    SpongeFluidStack(SpongeFluidStackBuilder builder) {
        this.fluidType = builder.fluidType;
        this.volume = builder.volume;
        this.extraData = builder.extra == null ? null : builder.extra.copy();
    }

    @Override
    public FluidType getFluid() {
        return this.fluidType;
    }

    @Override
    public int getVolume() {
        return this.volume;
    }

    @Override
    public FluidStack setVolume(int volume) {
        checkArgument(volume > 0, "Volume must be at least 0!");
        this.volume = volume;
        return this;
    }

    @Override
    public FluidStackSnapshot createSnapshot() {
        return new SpongeFluidStackSnapshotBuilder().from(this).build();
    }

    @Override
    public boolean validateRawData(DataMap container) {
        //TODO: the data still might not be valid
        return container.contains(Queries.CONTENT_VERSION, DataQueries.FLUID_TYPE, DataQueries.FLUID_VOLUME);
    }

    @Override
    public void setRawData(DataMap container) throws InvalidDataException {
        // TODO: duplicate code of SpongeFluidStackBuilder#buildContent()
        try {
            final int contentVersion = container.getInt(Queries.CONTENT_VERSION).get();
            if (contentVersion != this.getContentVersion()) {
                throw new InvalidDataException("Older content found! Cannot set raw data of older content!");
            }
            final String fluidId = container.getString(DataQueries.FLUID_TYPE).get();
            final int volume = container.getInt(DataQueries.FLUID_VOLUME).get();
            final Optional<FluidType> fluidType = Sponge.getRegistry().getType(FluidType.class, fluidId);
            if (!fluidType.isPresent()) {
                throw new InvalidDataException("Unknown FluidType found! Requested: " + fluidId + "but got none.");
            }
            this.fluidType = fluidType.get();
            this.volume = volume;
            container.getMap(DataQueries.UNSAFE_NBT).ifPresent(m ->
                    this.extraData = m.copy());
        } catch (Exception e) {
            throw new InvalidDataException("DataMap contained invalid data!", e);
        }
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        return false;
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value, Cause cause) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function, Cause cause) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        container
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(DataQueries.FLUID_TYPE, this.fluidType.getId())
            .set(DataQueries.FLUID_VOLUME, this.volume);
        if (this.extraData != null) {
            container.set(DataQueries.UNSAFE_NBT, this.extraData);
        }
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public DataHolder copy() {
        return FluidStack.builder().from(this).build();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return Collections.emptySet();
    }
}

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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

public class HorseTameableDataProcessor
        extends AbstractEntitySingleDataProcessor<EntityHorse, Optional<UUID>, OptionalValue<UUID>, TameableData, ImmutableTameableData> {

    public HorseTameableDataProcessor() {
        super(EntityHorse.class, Keys.TAMED_OWNER);
    }

    @Override
    protected Optional<Optional<UUID>> getVal(EntityHorse tameable) {
        return Optional.of(Optional.ofNullable(tameable.getOwnerUniqueId()));
    }

    @Override
    protected boolean set(EntityHorse horse, Optional<UUID> uuidOptional) {
        horse.setOwnerUniqueId(uuidOptional.orElse(null));
        if (uuidOptional.isPresent()) {
            horse.setHorseTamed(true);
        } else {
            horse.setHorseTamed(false);
        }
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected OptionalValue<UUID> constructValue(Optional<UUID> defaultValue) {
        return new SpongeOptionalValue<>(this.getKey(), defaultValue);
    }

    @Override
    protected ImmutableValue<Optional<UUID>> constructImmutableValue(Optional<UUID> value) {
        return new ImmutableSpongeValue<>(Keys.TAMED_OWNER, Optional.empty(), value);
    }

    @Override
    protected TameableData createManipulator() {
        return new SpongeTameableData();
    }

}

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
package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class TileEntitySkullDataProcessor
        extends AbstractTileEntitySingleDataProcessor<TileEntitySkull, SkullType, Value<SkullType>, SkullData, ImmutableSkullData> {

    public TileEntitySkullDataProcessor() {
        super(TileEntitySkull.class, Keys.SKULL_TYPE);
    }

    @Override
    protected boolean supports(TileEntitySkull skull) {
        return SkullUtils.supportsObject(skull);
    }

    @Override
    protected Optional<SkullType> getVal(TileEntitySkull skull) {
        return Optional.of(SkullUtils.getSkullType(skull));
    }

    @Override
    protected boolean set(TileEntitySkull skull, SkullType type) {
        SkullUtils.setSkullType(skull, ((SpongeSkullType) type).getByteId());
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<SkullType> constructValue(SkullType value) {
        return new SpongeValue<>(Keys.SKULL_TYPE, SkullTypes.SKELETON, value);
    }

    @Override
    protected ImmutableValue<SkullType> constructImmutableValue(SkullType value) {
        return ImmutableSpongeValue.cachedOf(Keys.SKULL_TYPE, SkullTypes.SKELETON, value);
    }

    @Override
    protected SkullData createManipulator() {
        return new SpongeSkullData();
    }

}

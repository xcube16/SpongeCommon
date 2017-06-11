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
package org.spongepowered.common.data.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.nbt.data.NbtDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class IsFlyingNbtProcessor extends AbstractSpongeNbtProcessor<FlyingData, ImmutableFlyingData> implements
        NbtDataProcessor<FlyingData, ImmutableFlyingData> {

    public IsFlyingNbtProcessor() {
        super(NbtDataTypes.ENTITY);
    }

    @Override
    public boolean isCompatible(NBTTagCompound nbtDataType) {
        return false;
    }

    @Override
    public Optional<FlyingData> readFrom(NBTTagCompound compound) {
        final NBTBase tag = compound.getTag(NbtDataUtil.Minecraft.IS_FLYING);
        if (tag != null) {
            return Optional.of(new SpongeFlyingData(((NBTTagByte) tag).getByte() != 0));
        }
        return Optional.empty();
    }

    @Override
    public Optional<FlyingData> readFrom(DataMap view) {
        return view.getBoolean(Keys.IS_FLYING.getQuery()).map(SpongeFlyingData::new);
    }

    @Override
    public Optional<NBTTagCompound> storeToCompound(NBTTagCompound compound, FlyingData manipulator) {
        compound.setBoolean(NbtDataUtil.Minecraft.IS_FLYING, manipulator.flying().get());
        return Optional.of(compound);
    }

    @Override
    public Optional<DataMap> storeToView(DataMap view, FlyingData manipulator) {
        view.set(Keys.IS_FLYING, manipulator.flying().get());
        return Optional.of(view);
    }

    @Override
    public DataTransactionResult remove(NBTTagCompound data) {
        return null;
    }

    @Override
    public DataTransactionResult remove(DataMap data) {
        return null;
    }
}

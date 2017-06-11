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

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.common.data.nbt.data.NbtDataProcessor;

import java.util.Optional;

public class SpongeNbtProcessorDelegate<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements NbtDataProcessor<M, I> {

    private final ImmutableList<NbtDataProcessor<M, I>> processors;
    private final NbtDataType nbtDataType;

    public SpongeNbtProcessorDelegate(ImmutableList<NbtDataProcessor<M, I>> processors, NbtDataType nbtDataType) {
        this.processors = processors;
        this.nbtDataType = nbtDataType;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public NbtDataType getTargetType() {
        return this.nbtDataType;
    }

    @Override
    public boolean isCompatible(NBTTagCompound compound) {
        for (NbtDataProcessor<M, I> processor : this.processors) {
            if (processor.isCompatible(compound)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<M> readFrom(NBTTagCompound compound) {
        for (NbtDataProcessor<M, I> processor : this.processors) {
            final Optional<M> returnVal = processor.readFrom(compound);
            if (returnVal.isPresent()) {
                return returnVal;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> readFrom(DataMap view) {
        for (NbtDataProcessor<M, I> processor : this.processors) {
            final Optional<M> returnVal = processor.readFrom(view);
            if (returnVal.isPresent()) {
                return returnVal;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<NBTTagCompound> storeToCompound(NBTTagCompound compound, M manipulator) {
        for (NbtDataProcessor<M, I> processor : this.processors) {
            final Optional<NBTTagCompound> returnVal = processor.storeToCompound(compound, manipulator);
            if (returnVal.isPresent()) {
                return returnVal;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<DataMap> storeToView(DataMap view, M manipulator) {
        for (NbtDataProcessor<M, I> processor : this.processors) {
            final Optional<DataMap> returnVal = processor.storeToView(view, manipulator);
            if (returnVal.isPresent()) {
                return returnVal;
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(NBTTagCompound data) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(DataMap data) {
        return DataTransactionResult.failNoData();
    }
}

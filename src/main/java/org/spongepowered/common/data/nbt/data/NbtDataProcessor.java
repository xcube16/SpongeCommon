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
package org.spongepowered.common.data.nbt.data;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.common.data.nbt.NbtDataType;

import java.util.Optional;

//TODO: There are generic translators for NBT <-> DataView so I think this interface needs to be redesigned or removed
public interface NbtDataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> {

    int getPriority();

    NbtDataType getTargetType();

    boolean isCompatible(NBTTagCompound compound);

    Optional<M> readFrom(NBTTagCompound compound);

    Optional<M> readFrom(DataMap view);

    Optional<NBTTagCompound> storeToCompound(NBTTagCompound compound, M manipulator);

    Optional<DataMap> storeToView(DataMap view, M manipulator);

    DataTransactionResult remove(NBTTagCompound data);

    DataTransactionResult remove(DataMap data);
}

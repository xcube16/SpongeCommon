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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePickupDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.PickupDelayData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePickupDelayData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.interfaces.entity.item.IMixinEntityItem;

import java.util.Map;

public final class PickupDelayDataProcessor extends AbstractEntityDataProcessor<EntityItem, PickupDelayData, ImmutablePickupDelayData> {

    public PickupDelayDataProcessor() {
        super(EntityItem.class);
    }

    @Override
    protected boolean doesDataExist(EntityItem container) {
        return true;
    }

    @Override
    protected boolean set(EntityItem container, Map<Key<?>, Object> keyValues) {
        ((IMixinEntityItem) container).setPickupDelay(
                (Integer) keyValues.get(Keys.PICKUP_DELAY),
                (Boolean) keyValues.get(Keys.INFINITE_PICKUP_DELAY)
        );
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityItem container) {
        return ImmutableMap.<Key<?>, Object> builder()
                .put(Keys.PICKUP_DELAY, ((IMixinEntityItem) container).getPickupDelay())
                .put(Keys.INFINITE_PICKUP_DELAY, ((IMixinEntityItem) container).infinitePickupDelay())
                .build();
    }

    @Override
    protected PickupDelayData createManipulator() {
        return new SpongePickupDelayData();
    }

    @Override
    public DataTransactionResult remove(DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}

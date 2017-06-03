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
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;

import java.util.Map;

public class FoodDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, FoodData, ImmutableFoodData> {

    public FoodDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected FoodData createManipulator() {
        return new SpongeFoodData(20, 20, 0);
    }

    @Override
    protected boolean doesDataExist(EntityPlayer entity) {
        return true;
    }

    @Override
    protected boolean set(EntityPlayer entity, Map<Key<?>, Object> keyValues) {
        entity.getFoodStats().setFoodLevel((Integer) keyValues.get(Keys.FOOD_LEVEL));
        entity.getFoodStats().foodSaturationLevel = ((Double) keyValues.get(Keys.SATURATION)).floatValue();
        entity.getFoodStats().foodExhaustionLevel = ((Double) keyValues.get(Keys.EXHAUSTION)).floatValue();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityPlayer entity) {
        final int food = entity.getFoodStats().getFoodLevel();
        final double saturation = entity.getFoodStats().foodSaturationLevel;
        final double exhaustion = entity.getFoodStats().foodExhaustionLevel;
        return ImmutableMap.<Key<?>, Object>of(Keys.FOOD_LEVEL, food,
                                               Keys.SATURATION, saturation,
                                               Keys.EXHAUSTION, exhaustion);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

}

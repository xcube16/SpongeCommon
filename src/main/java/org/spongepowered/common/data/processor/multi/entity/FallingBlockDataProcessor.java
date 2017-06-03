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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallingBlockData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;

import java.util.Map;

public class FallingBlockDataProcessor extends AbstractEntityDataProcessor<EntityFallingBlock, FallingBlockData, ImmutableFallingBlockData> {

    public FallingBlockDataProcessor() {
        super(EntityFallingBlock.class);
    }

    @Override
    protected boolean doesDataExist(EntityFallingBlock entity) {
        return true;
    }

    @Override
    protected boolean set(EntityFallingBlock entity, Map<Key<?>, Object> keyValues) {
        entity.fallHurtAmount = ((Double) keyValues.get(Keys.FALL_DAMAGE_PER_BLOCK)).floatValue();
        entity.fallHurtMax = ((Double) keyValues.get(Keys.MAX_FALL_DAMAGE)).intValue();
        entity.fallTile = (IBlockState) keyValues.get(Keys.FALLING_BLOCK_STATE);
        entity.dontSetBlock = !(Boolean) keyValues.get(Keys.CAN_PLACE_AS_BLOCK);
        entity.shouldDropItem = (Boolean) keyValues.get(Keys.CAN_DROP_AS_ITEM);
        entity.fallTime = (Integer) keyValues.get(Keys.FALL_TIME);
        entity.hurtEntities = (Boolean) keyValues.get(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityFallingBlock entity) {
        return ImmutableMap.<Key<?>, Object> builder()
                .put(Keys.FALL_DAMAGE_PER_BLOCK, (double)entity.fallHurtAmount)
                .put(Keys.MAX_FALL_DAMAGE, (double)entity.fallHurtMax)
                .put(Keys.FALLING_BLOCK_STATE, entity.fallTile)
                .put(Keys.CAN_PLACE_AS_BLOCK, !entity.dontSetBlock)
                .put(Keys.CAN_DROP_AS_ITEM, entity.shouldDropItem)
                .put(Keys.FALL_TIME, entity.fallTime)
                .put(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, entity.hurtEntities)
                .build();
    }

    @Override
    protected FallingBlockData createManipulator() {
        return new SpongeFallingBlockData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

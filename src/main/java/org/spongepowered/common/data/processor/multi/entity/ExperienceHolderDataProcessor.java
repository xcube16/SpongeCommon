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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;

import java.util.Map;

public class ExperienceHolderDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, ExperienceHolderData, ImmutableExperienceHolderData> {

    public ExperienceHolderDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected ExperienceHolderData createManipulator() {
        return new SpongeExperienceHolderData();
    }

    @Override
    protected boolean doesDataExist(EntityPlayer entity) {
        return true;
    }

    @Override
    protected boolean set(EntityPlayer entity, Map<Key<?>, Object> keyValues) {
        entity.experienceLevel = (Integer) keyValues.get(Keys.EXPERIENCE_LEVEL);
        entity.experienceTotal = (Integer) keyValues.get(Keys.TOTAL_EXPERIENCE);
        entity.experience = (float) (Integer) keyValues.get(Keys.EXPERIENCE_SINCE_LEVEL) / entity.xpBarCap();
        ((IMixinEntityPlayerMP) entity).refreshExp();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityPlayer entity) {
        final int level = entity.experienceLevel;
        final int totalExp = entity.experienceTotal;
        final int expSinceLevel = (int) (entity.experience * entity.xpBarCap());
        final int expBetweenLevels = entity.xpBarCap();
        return ImmutableMap.<Key<?>, Object>of(Keys.EXPERIENCE_LEVEL, level,
                Keys.TOTAL_EXPERIENCE, totalExp,
                Keys.EXPERIENCE_SINCE_LEVEL, expSinceLevel,
                Keys.EXPERIENCE_FROM_START_OF_LEVEL, expBetweenLevels);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

}

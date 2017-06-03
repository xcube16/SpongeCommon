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
import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArmorStandData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;

import java.util.Map;

public class ArmorStandDataProcessor extends AbstractEntityDataProcessor<EntityArmorStand, ArmorStandData, ImmutableArmorStandData> {

    public ArmorStandDataProcessor() {
        super(EntityArmorStand.class);
    }

    @Override
    protected boolean doesDataExist(EntityArmorStand dataHolder) {
        return true;
    }

    @Override
    protected boolean set(EntityArmorStand dataHolder, Map<Key<?>, Object> keyValues) {
        final boolean hasArms = (boolean) keyValues.get(Keys.ARMOR_STAND_HAS_ARMS);
        final boolean hasBasePlate = (boolean) keyValues.get(Keys.ARMOR_STAND_HAS_BASE_PLATE);
        final boolean isSmall = (boolean) keyValues.get(Keys.ARMOR_STAND_IS_SMALL);
        final boolean isMarker = (boolean) keyValues.get(Keys.ARMOR_STAND_MARKER);
        dataHolder.setSmall(isSmall);
        dataHolder.setMarker(isMarker);
        dataHolder.setNoBasePlate(!hasBasePlate);
        dataHolder.setShowArms(hasArms);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityArmorStand dataHolder) {
        return ImmutableMap.<Key<?>, Object>builder()
                .put(Keys.ARMOR_STAND_HAS_ARMS, dataHolder.getShowArms())
                .put(Keys.ARMOR_STAND_HAS_BASE_PLATE, !dataHolder.hasNoBasePlate())
                .put(Keys.ARMOR_STAND_MARKER, dataHolder.hasMarker())
                .put(Keys.ARMOR_STAND_IS_SMALL, dataHolder.isSmall())
                .build();
    }

    @Override
    protected ArmorStandData createManipulator() {
        return new SpongeArmorStandData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

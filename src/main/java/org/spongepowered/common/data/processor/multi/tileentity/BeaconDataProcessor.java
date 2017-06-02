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
package org.spongepowered.common.data.processor.multi.tileentity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;

import java.util.Map;
import java.util.Optional;

public class BeaconDataProcessor extends AbstractTileEntityDataProcessor<TileEntityBeacon, BeaconData, ImmutableBeaconData> {

    public BeaconDataProcessor() {
        super(TileEntityBeacon.class);
    }

    @Override
    protected boolean doesDataExist(TileEntityBeacon dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(TileEntityBeacon dataHolder, Map<Key<?>, Object> keyValues) {
        Potion primary = ((Optional<Potion>) keyValues.get(Keys.BEACON_PRIMARY_EFFECT)).orElse(null);
        Potion secondary = ((Optional<Potion>) keyValues.get(Keys.BEACON_SECONDARY_EFFECT)).orElse(null);
        final int primaryId = primary == null ? 0 : Potion.getIdFromPotion(primary);
        dataHolder.setField(1, primaryId);
        final int secondaryId = secondary == null ? 0 : Potion.getIdFromPotion(secondary);
        dataHolder.setField(2, secondaryId);

        dataHolder.markDirty();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(TileEntityBeacon dataHolder) {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        int primaryID = dataHolder.getField(1);
        int secondaryID = dataHolder.getField(2);
        if (primaryID > 0) {
            builder.put(Keys.BEACON_PRIMARY_EFFECT, Optional.ofNullable(Potion.getPotionById(primaryID)));
        }
        if (secondaryID > 0 && dataHolder.getField(0) == 4) {
            builder.put(Keys.BEACON_SECONDARY_EFFECT, Optional.ofNullable(Potion.getPotionById(secondaryID)));
        }
        return builder.build();
    }

    @Override
    protected BeaconData createManipulator() {
        return new SpongeBeaconData();
    }

    @Override
    public Optional<BeaconData> fill(DataMap container, BeaconData beaconData) {
        beaconData.primaryEffect().set(
                container.getObject(Keys.BEACON_PRIMARY_EFFECT.getQuery(), PotionEffectType.class));
        beaconData.secondaryEffect().set(
                container.getObject(Keys.BEACON_SECONDARY_EFFECT.getQuery(), PotionEffectType.class));
        return Optional.of(beaconData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

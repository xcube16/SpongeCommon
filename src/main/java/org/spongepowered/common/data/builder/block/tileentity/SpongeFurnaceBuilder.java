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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;

public class SpongeFurnaceBuilder extends SpongeLockableBuilder<Furnace> {

    public SpongeFurnaceBuilder() {
        super(Furnace.class, 1);
    }

    @Override
    protected Optional<Furnace> buildContent(DataMap container) throws InvalidDataException {
        return super.buildContent(container).flatMap(furnace -> {
            final TileEntityFurnace tileEntityFurnace = (TileEntityFurnace) furnace;

            container.getString(DataQueries.CUSTOM_NAME).ifPresent(tileEntityFurnace::setCustomInventoryName);

            final Optional<Integer> burnTime = container.getInt(Keys.PASSED_BURN_TIME.getQuery());
            final Optional<Integer> maxBurnTime = container.getInt(Keys.MAX_BURN_TIME.getQuery());
            final Optional<Integer> passedCookTime = container.getInt(Keys.PASSED_COOK_TIME.getQuery());
            final Optional<Integer> maxCookTime = container.getInt(Keys.MAX_COOK_TIME.getQuery());
            if (!burnTime.isPresent() || !maxBurnTime.isPresent() || !passedCookTime.isPresent() || !maxCookTime.isPresent()) {
                ((TileEntity) furnace).invalidate();
                return Optional.empty();
            }

            tileEntityFurnace.setField(0, maxBurnTime.get() - burnTime.get());
            tileEntityFurnace.setField(1, maxBurnTime.get());
            tileEntityFurnace.setField(2, passedCookTime.get());
            tileEntityFurnace.setField(3, maxCookTime.get());
            tileEntityFurnace.markDirty();
            return Optional.of(furnace);
        });
    }
}

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

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;

public class SpongeLockableBuilder<T extends TileEntityCarrier> extends AbstractTileBuilder<T> {

    protected SpongeLockableBuilder(Class<T> clazz, int version) {
        super(clazz, version);
    }

    @Override
    protected Optional<T> buildContent(DataMap container) throws InvalidDataException {
        return super.buildContent(container).flatMap(lockable -> {
            //TODO: don't use contains(), and do a proper check before calling get() methods
            if (!container.contains(DataQueries.BLOCK_ENTITY_ITEM_CONTENTS)) {
                ((TileEntity) lockable).invalidate();
                return Optional.empty();
            }
            DataList contents = container.getList(DataQueries.BLOCK_ENTITY_ITEM_CONTENTS).get();
            contents.forEachKey(key -> {
                DataMap content = contents.getMap(key).get();
                net.minecraft.item.ItemStack stack = (net.minecraft.item.ItemStack) content
                        .getObject(DataQueries.BLOCK_ENTITY_SLOT_ITEM, ItemStack.class).get();
                ((IInventory) lockable).setInventorySlotContents(content.getInt(DataQueries.BLOCK_ENTITY_SLOT).get(), stack);
            });
            if (container.contains(Keys.LOCK_TOKEN.getQuery())) {
                lockable.offer(Keys.LOCK_TOKEN, container.getString(Keys.LOCK_TOKEN.getQuery()).get());
            }
            return Optional.of(lockable);
        });
    }
}

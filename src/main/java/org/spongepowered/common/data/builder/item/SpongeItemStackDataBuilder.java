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
package org.spongepowered.common.data.builder.item;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.Item;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.List;
import java.util.Optional;

public class SpongeItemStackDataBuilder extends AbstractDataBuilder<ItemStack> implements DataBuilder<ItemStack> {

    public SpongeItemStackDataBuilder() {
        super(ItemStack.class, 1);
    }

    @Override
    protected Optional<ItemStack> buildContent(DataMap container) throws InvalidDataException {
        checkNotNull(container);
        final Optional<ItemType> itemType = container.getObject(DataQueries.ITEM_TYPE, ItemType.class);
        if (!itemType.isPresent()) {
            return Optional.empty();
        }

        final int count = container.getInt(DataQueries.ITEM_COUNT).orElse(1);
        final int damage = container.getInt(DataQueries.ITEM_DAMAGE_VALUE).orElse(0);
        final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack((Item) itemType.get(), count, damage);

        container.getMap(DataQueries.UNSAFE_NBT).ifPresent(m ->
                itemStack.setTagCompound(NbtTranslator.getInstance().translate(m)));

        container.getList(DataQueries.DATA_MANIPULATORS).ifPresent(views -> {
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            for (DataManipulator<?, ?> manipulator : manipulators) {
                ((IMixinCustomDataHolder) itemStack).offerCustom(manipulator, MergeFunction.IGNORE_ALL);
            }
            if (!transaction.failedData.isEmpty()) {
                ((IMixinCustomDataHolder) itemStack).addFailedData(transaction.failedData);
            }
        });
        return Optional.of((ItemStack) itemStack);
    }

}

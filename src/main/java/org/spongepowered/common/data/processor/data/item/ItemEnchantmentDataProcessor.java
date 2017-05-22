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
package org.spongepowered.common.data.processor.data.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemEnchantmentDataProcessor
        extends AbstractItemSingleDataProcessor<List<ItemEnchantment>, ListValue<ItemEnchantment>, EnchantmentData, ImmutableEnchantmentData> {

    public ItemEnchantmentDataProcessor() {
        super(input -> true, Keys.ITEM_ENCHANTMENTS);
    }

    @Override
    protected EnchantmentData createManipulator() {
        return new SpongeEnchantmentData();
    }

    @Override
    protected boolean set(ItemStack itemStack, List<ItemEnchantment> value) {
        NbtDataUtil.setItemEnchantments(itemStack, value);
        return true;
    }

    @Override
    protected Optional<List<ItemEnchantment>> getVal(ItemStack itemStack) {
        if (itemStack.isItemEnchanted()) {
            return Optional.of(NbtDataUtil.getItemEnchantments(itemStack));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected ListValue<ItemEnchantment> constructValue(List<ItemEnchantment> actualValue) {
        return new SpongeListValue<>(Keys.ITEM_ENCHANTMENTS, actualValue);
    }

    @Override
    protected ImmutableValue<List<ItemEnchantment>> constructImmutableValue(List<ItemEnchantment> value) {
        return new ImmutableSpongeListValue<>(Keys.ITEM_ENCHANTMENTS, ImmutableList.copyOf(value));
    }

    @Override
    public Optional<EnchantmentData> fill(DataMap container, EnchantmentData enchantmentData) {
        final List<ItemEnchantment> enchantments = new ArrayList<>();
        container.getList(Keys.ITEM_ENCHANTMENTS.getQuery()).ifPresent(l ->
                l.forEachKey(i -> l.getSpongeObject(i, ItemEnchantment.class).ifPresent(enchantments::add)));
        enchantmentData.setElements(enchantments);
        return Optional.of(enchantmentData);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            ItemStack stack = (ItemStack) container;
            Optional<List<ItemEnchantment>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            stack.getTagCompound().removeTag(NbtDataUtil.ITEM_ENCHANTMENT_LIST);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

}

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
package org.spongepowered.common.data.manipulator.mutable.item;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractListData;

import java.util.List;

public class SpongeEnchantmentData extends AbstractListData<ItemEnchantment, EnchantmentData, ImmutableEnchantmentData> implements EnchantmentData {

    public SpongeEnchantmentData() {
        this(Lists.newArrayList());
    }

    public SpongeEnchantmentData(List<ItemEnchantment> enchantments) {
        super(EnchantmentData.class, enchantments, Keys.ITEM_ENCHANTMENTS, ImmutableSpongeEnchantmentData.class);
    }

    @Override
    public void toContainer(DataMap container) {
        super.toContainer(container);
        container.set(Keys.ITEM_ENCHANTMENTS.getQuery(), this.getValue());
    }

}

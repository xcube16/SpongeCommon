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
package org.spongepowered.common.data.processor.multi.item;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableHideData;
import org.spongepowered.api.data.manipulator.mutable.item.HideData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeHideData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Map;

public class HideDataProcessor extends AbstractMultiDataSingleTargetProcessor<ItemStack, HideData, ImmutableHideData> {

    public HideDataProcessor() {
        super(ItemStack.class);
    }

    @Override
    protected boolean doesDataExist(ItemStack dataHolder) {
        return dataHolder.hasTagCompound();
    }

    @Override
    protected boolean set(ItemStack dataHolder, Map<Key<?>, Object> keyValues) {
        if (!dataHolder.hasTagCompound()) {
            dataHolder.setTagCompound(new NBTTagCompound());
        }
        int flag = 0;
        if ((boolean) keyValues.get(Keys.HIDE_ENCHANTMENTS)) {
            flag |= DataConstants.HIDE_ENCHANTMENTS_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_ATTRIBUTES)) {
            flag |= DataConstants.HIDE_ATTRIBUTES_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_UNBREAKABLE)) {
            flag |= DataConstants.HIDE_UNBREAKABLE_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_CAN_DESTROY)) {
            flag |= DataConstants.HIDE_CAN_DESTROY_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_CAN_PLACE)) {
            flag |= DataConstants.HIDE_CAN_PLACE_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_MISCELLANEOUS)) {
            flag |= DataConstants.HIDE_MISCELLANEOUS_FLAG;
        }
        dataHolder.getTagCompound().setInteger(NbtDataUtil.ITEM_HIDE_FLAGS, flag);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(ItemStack dataHolder) {
        if (!dataHolder.hasTagCompound()) {
            return Maps.newHashMap();
        }
        Map<Key<?>, Boolean> map = Maps.newHashMap();
        int flag = dataHolder.getTagCompound().getInteger(NbtDataUtil.ITEM_HIDE_FLAGS);

        map.put(Keys.HIDE_MISCELLANEOUS, (flag & DataConstants.HIDE_MISCELLANEOUS_FLAG) != 0);
        map.put(Keys.HIDE_CAN_PLACE, (flag & DataConstants.HIDE_CAN_PLACE_FLAG) != 0);
        map.put(Keys.HIDE_CAN_DESTROY, (flag & DataConstants.HIDE_CAN_DESTROY_FLAG) != 0);
        map.put(Keys.HIDE_UNBREAKABLE, (flag & DataConstants.HIDE_UNBREAKABLE_FLAG) != 0);
        map.put(Keys.HIDE_ATTRIBUTES, (flag & DataConstants.HIDE_ATTRIBUTES_FLAG) != 0);
        map.put(Keys.HIDE_ENCHANTMENTS, (flag & DataConstants.HIDE_ENCHANTMENTS_FLAG) != 0);

        return map;
    }

    @Override
    protected HideData createManipulator() {
        return new SpongeHideData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            ItemStack data = (ItemStack) dataHolder;
            if (data.hasTagCompound() && data.getTagCompound().hasKey(NbtDataUtil.ITEM_HIDE_FLAGS, NbtDataUtil.TAG_INT)) {
                data.getTagCompound().removeTag(NbtDataUtil.ITEM_HIDE_FLAGS);
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}

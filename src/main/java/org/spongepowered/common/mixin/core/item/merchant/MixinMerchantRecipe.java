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
package org.spongepowered.common.mixin.core.item.merchant;

import net.minecraft.village.MerchantRecipe;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(MerchantRecipe.class)
public abstract class MixinMerchantRecipe implements TradeOffer {

    @Shadow public abstract net.minecraft.item.ItemStack getItemToBuy();
    @Shadow public abstract boolean hasSecondItemToBuy();
    @Shadow @Nullable public abstract net.minecraft.item.ItemStack getSecondItemToBuy();
    @Shadow public abstract net.minecraft.item.ItemStack getItemToSell();
    @Shadow public abstract int getToolUses();
    @Shadow public abstract int getMaxTradeUses();
    @Shadow public abstract boolean isRecipeDisabled();
    @Shadow public abstract boolean getRewardsExp();

    @Override
    public ItemStackSnapshot getFirstBuyingItem() {
        return ((ItemStack) getItemToBuy()).createSnapshot();
    }

    @Override
    public boolean hasSecondItem() {
        return hasSecondItemToBuy();
    }

    @Override
    public Optional<ItemStackSnapshot> getSecondBuyingItem() {
        if (getSecondItemToBuy() == null) {
            return Optional.empty();
        } else {
            return Optional.of(((ItemStack) getSecondItemToBuy()).createSnapshot());
        }
    }

    @Override
    public ItemStackSnapshot getSellingItem() {
        return ((ItemStack) getItemToSell()).createSnapshot();
    }

    @Override
    public int getUses() {
        return getToolUses();
    }

    @Override
    public int getMaxUses() {
        return getMaxTradeUses();
    }

    @Override
    public boolean hasExpired() {
        return isRecipeDisabled();
    }

    @Override
    public boolean doesGrantExperience() {
        return getRewardsExp();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        container
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.TRADE_OFFER_FIRST_ITEM, this.getFirstBuyingItem())
                .set(DataQueries.TRADE_OFFER_SECOND_ITEM, this.hasSecondItem() ? this.getSecondBuyingItem().get() : "none")
                .set(DataQueries.TRADE_OFFER_BUYING_ITEM, this.getItemToBuy())
                .set(DataQueries.TRADE_OFFER_GRANTS_EXPERIENCE, this.doesGrantExperience())
                .set(DataQueries.TRADE_OFFER_MAX_USES, this.getMaxTradeUses())
                .set(DataQueries.TRADE_OFFER_USES, this.getUses());
    }
}

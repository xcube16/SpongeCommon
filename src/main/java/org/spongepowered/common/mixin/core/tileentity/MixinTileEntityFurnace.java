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
package org.spongepowered.common.mixin.core.tileentity;

import static net.minecraft.inventory.SlotFurnaceFuel.isBucket;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.FuelSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.OutputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.FuelSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.OutputSlotLensImpl;

@NonnullByDefault
@Mixin(TileEntityFurnace.class)
public abstract class MixinTileEntityFurnace extends MixinTileEntityLockable implements Furnace, IMixinCustomNameable {

    @Shadow private String furnaceCustomName;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.slots = new SlotCollection.Builder().add(1)
                .add(FuelSlotAdapter.class, (i) -> new FuelSlotLensImpl(i, (s) -> TileEntityFurnace.isItemFuel((ItemStack) s) || isBucket(
                        (ItemStack) s), t -> {
                            final ItemStack nmsStack = (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1);
                    return TileEntityFurnace.isItemFuel(nmsStack) || isBucket(nmsStack);
                }))
                .add(OutputSlotAdapter.class, (i) -> new OutputSlotLensImpl(i, (s) -> false, (t) -> false))
                .build();
        this.lens = new FurnaceInventoryLens((InventoryAdapter<IInventory, ItemStack>) this, this.slots);
    }

    @Override
    public void toContainer(DataMap container) {
        container.set("BurnTime", this.getField(0));
        container.set("BurnTimeTotal", this.getField(1));
        container.set("CookTime", this.getField(3) - this.getField(2));
        container.set("CookTimeTotal", this.getField(3));
        if (this.furnaceCustomName != null) {
            container.set(DataQueries.CUSTOM_NAME, this.furnaceCustomName);
        }
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityFurnace) (Object) this).setCustomInventoryName(customName);
    }

}

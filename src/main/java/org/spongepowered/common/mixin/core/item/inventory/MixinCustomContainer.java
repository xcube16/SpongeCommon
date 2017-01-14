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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;
import org.spongepowered.common.item.inventory.util.ContainerUtil;

@Mixin(CustomContainer.class)
public abstract class MixinCustomContainer implements MinecraftInventoryAdapter, Inventory {

    @Shadow(remap = false) private CustomInventory inv;

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(EntityPlayer player, CustomInventory inventory, CallbackInfo ci) {
        this.fabric = MinecraftFabric.of(this);
        // CustomInventory + Main PlayerInventory + HotBar PlayerInventory
        this.slots = new SlotCollection.Builder().add(inventory.getSizeInventory()).add(36).build();
        this.lens = ContainerUtil.getLens(this.fabric, ((Container)(Object) this), slots);
    }

    @Override
    public Lens<IInventory, ItemStack> getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric<IInventory> getInventory() {
        return this.fabric;
    }

    @Override
    public Inventory getChild(Lens<IInventory, ItemStack> lens) {
        return null; // TODO ?
    }

    @Override
    public void notify(Object source, InventoryEventArgs eventArgs) {
    }

    @Override
    public InventoryArchetype getArchetype() {
        return this.inv.getArchetype();
    }
}

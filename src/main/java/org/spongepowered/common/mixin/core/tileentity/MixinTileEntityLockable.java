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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.item.InventoryItemData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(TileEntityLockable.class)
@Implements({@Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$"),
             @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),})
public abstract class MixinTileEntityLockable extends MixinTileEntity implements TileEntityCarrier, IInventory {

    @Shadow private LockCode code;

    protected Fabric<IInventory> fabric; // is set when constructed
    protected SlotCollection slots; // is set by Mixin further down the line OR fallback in getter
    @Nullable protected Lens<IInventory, ItemStack> lens = null; // is set by Mixin further down the line OR fallback in getter

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
    }

    @Override
    public void toContainer(DataMap container) {
        super.toContainer(container);
        if (this.code != null) {
            container.set(DataQueries.BLOCK_ENTITY_LOCK_CODE, this.code.getLock());
        }
        DataList items = container.createList(DataQueries.BLOCK_ENTITY_ITEM_CONTENTS);
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                // todo make a helper object for this
                items.addMap()
                    .set(Queries.CONTENT_VERSION, 1)
                    .set(DataQueries.BLOCK_ENTITY_SLOT, i)
                    .set(DataQueries.BLOCK_ENTITY_SLOT_ITEM, stack);
            }
        }
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<LockableData> lockData = get(LockableData.class);
        if (lockData.isPresent()) {
            manipulators.add(lockData.get());
        }
        Optional<InventoryItemData> inventoryData = get(InventoryItemData.class);
        if (inventoryData.isPresent()) {
            manipulators.add(inventoryData.get());
        }
        if (((TileEntityLockable) (Object) this).hasCustomName()) {
            manipulators.add(get(DisplayNameData.class).get());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TileEntityInventory<TileEntityCarrier> getInventory() {
        return (TileEntityInventory<TileEntityCarrier>) this;
    }

    public Optional<? extends TileEntityCarrier> tileentityinventory$getTileEntity() {
        return Optional.of(this);
    }

    public Optional<? extends TileEntityCarrier> tileentityinventory$getCarrier() {
        return Optional.of(this);
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        if (this.slots == null) {
            this.slots = new SlotCollection.Builder().add(this.getSizeInventory()).build(); // Fallback
        }
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        if (this.lens == null) {
            this.lens = new OrderedInventoryLensImpl(0, this.getSizeInventory(), 1, inventory$getSlotProvider());
        }
        return this.lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        return this.fabric;
    }
}

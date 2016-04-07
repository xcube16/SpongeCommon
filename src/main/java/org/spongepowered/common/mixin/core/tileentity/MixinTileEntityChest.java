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

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityChest.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),
             @Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$")})
public abstract class MixinTileEntityChest extends MixinTileEntityLockable implements Chest, IMixinCustomNameable {

    @Shadow public String customName;

    private Fabric<IInventory> inventory;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.inventory = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(27).build();
        this.lens = new GridInventoryLensImpl(0, 9, 3, 9, slots);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        if (this.customName != null) {
            container.set(of("CustomName"), this.customName);
        }
        return container;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<ConnectedDirectionData> connectedChestData = get(ConnectedDirectionData.class);
        if (connectedChestData.isPresent()) {
            manipulators.add(connectedChestData.get());
        }
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityChest) (Object) this).setCustomName(customName);
    }

    @Override
    public TileEntityInventory<TileEntityCarrier> getInventory() {
        return (TileEntityInventory<TileEntityCarrier>) (Object) this;
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        return slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        return lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        return inventory;
    }

    public void tilentityinventory$markDirty() {
        ((IInventory) (Object) this).markDirty();
    }

    public Optional<Chest> tileentityinventory$getTileEntity() {
        return Optional.of(this);
    }

    public Optional<Chest> tileentityinventory$getCarrier() {
        return Optional.of(this);
    }
}

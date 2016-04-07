package org.spongepowered.common.mixin.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

@Mixin(InventoryEnderChest.class)
@Implements(value = @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"))
public abstract class MixinInventoryEnderChest extends InventoryBasic {

    private Fabric<IInventory> inventory;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    public MixinInventoryEnderChest(String title, boolean customName, int slotCount) {
        super(title, customName, slotCount);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.inventory = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(27).build();
        this.lens = new GridInventoryLensImpl(0, 9, 3, 9, slots);
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
}

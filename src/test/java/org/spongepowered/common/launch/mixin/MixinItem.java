package org.spongepowered.common.launch.mixin;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;

@Mixin(value = Item.class, remap = false)
public abstract class MixinItem {

    // Register items
    @Inject(method = "registerItem(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/Item;)V", at = @At("RETURN"))
    private static void registerMinecraftItem(int id, ResourceLocation name, Item item, CallbackInfo ci) {
        ItemTypeRegistryModule.getInstance().registerAdditionalCatalog((ItemType) item);
    }

}

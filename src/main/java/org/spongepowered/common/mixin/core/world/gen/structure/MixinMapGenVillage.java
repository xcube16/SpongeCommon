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
package org.spongepowered.common.mixin.core.world.gen.structure;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.MapGenVillage.Start;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.structure.Village;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.gen.IFlaggedPopulator;
import org.spongepowered.common.world.gen.WorldGenConstants;

import java.util.List;
import java.util.Random;

/**
 * The placement of villages within a chunk has impact can have impact on other
 * populators. Possibly this should be added as a concept in the API in order to
 * allow better access and control over the function of these populators while
 * preserving vanilla functionality.
 */
@Mixin(MapGenVillage.class)
public abstract class MixinMapGenVillage extends MapGenStructure implements IFlaggedPopulator, Village {

    @Shadow private int terrainType;
    @Shadow private int field_82665_g; // distance
    
    @SuppressWarnings("unchecked")
    private List<BiomeType> validBiomes = Lists.newArrayList();


    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        for(BiomeGenBase biome: MapGenVillage.VILLAGE_SPAWN_BIOMES) {
            this.validBiomes.add((BiomeType) biome);
        }
    }
    
    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random, List<String> flags) {
        Vector3i min = extent.getBlockMin();
        World world = (World) worldIn;
        boolean flag = generateStructure(world, random, new ChunkPos((min.getX() - 8) / 16, (min.getZ() - 8) / 16));
        if (flag) {
            flags.add(WorldGenConstants.VILLAGE_FLAG);
        }
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        World world = (World) worldIn;
        generateStructure(world, random, new ChunkPos((min.getX() - 8) / 16, (min.getZ() - 8) / 16));
    }
    
    @Override
    public int getSize() {
        return this.terrainType;
    }
    
    @Override
    public void setSize(int range) {
        this.terrainType = range;
    }
    
    @Override
    public List<BiomeType> getValidBiomes() {
        return this.validBiomes;
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "canSpawnStructureAtCoords", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/biome/BiomeProvider;areBiomesViable(IIILjava/util/List;)Z") )
    private boolean onAreBiomesViable(int x, int z, int radius, List<BiomeGenBase> types) {
        //Replace the list with our own
        return this.worldObj.getBiomeProvider().areBiomesViable(x, z, radius,(List<BiomeGenBase>)(Object) this.validBiomes);
    }
    
}

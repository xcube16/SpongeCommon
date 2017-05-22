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
package org.spongepowered.common.mixin.core.potion;

import net.minecraft.potion.Potion;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.DataQueries;

@NonnullByDefault
@Mixin(net.minecraft.potion.PotionEffect.class)
@Implements(@Interface(iface = PotionEffect.class, prefix = "potionEffect$"))
public abstract class MixinPotionEffect implements PotionEffect {

    @Shadow @Final private Potion potion;
    @Shadow private int duration;
    @Shadow private int amplifier;
    @Shadow private boolean isAmbient;
    @Shadow private boolean showParticles;

    @Override
    public PotionEffectType getType() {
        return (PotionEffectType) this.potion;
    }

    @Intrinsic
    public int potionEffect$getDuration() {
        return this.duration;
    }

    @Intrinsic
    public int potionEffect$getAmplifier() {
        return this.amplifier;
    }

    @Override
    public boolean isAmbient() {
        return this.isAmbient;
    }

    @Override
    public boolean getShowParticles() {
        return this.showParticles;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        container
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.POTION_TYPE, this.potion.getName())
                .set(DataQueries.POTION_DURATION, this.duration)
                .set(DataQueries.POTION_AMPLIFIER, this.amplifier)
                .set(DataQueries.POTION_AMBIANCE, this.isAmbient)
                .set(DataQueries.POTION_SHOWS_PARTICLES, this.showParticles);
    }
}

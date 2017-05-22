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
package org.spongepowered.common.interfaces.world;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

public interface IMixinWorldSettings {

    void setId(String id);

    void setName(String name);

    boolean isFromBuilder();

    void setDimensionType(DimensionType dimensionType);

    void setDifficulty(Difficulty difficulty);

    void setSerializationBehavior(SerializationBehavior behavior);

    void setGeneratorSettings(DataMap generatorSettings);

    void setGeneratorModifiers(ImmutableList<WorldGeneratorModifier> generatorModifiers);

    void setEnabled(boolean state);

    void setLoadOnStartup(boolean state);

    void setKeepSpawnLoaded(boolean state);

    void setGenerateSpawnOnLoad(boolean state);

    void setPVPEnabled(boolean state);

    void setCommandsAllowed(boolean state);

    void setGenerateBonusChest(boolean state);

    void setPortalAgentType(PortalAgentType type);

    void fromBuilder(boolean state);

    void setRandomSeed(boolean state);
}

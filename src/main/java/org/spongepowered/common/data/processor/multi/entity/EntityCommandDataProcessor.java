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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.mutable.SpongeCommandData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Map;
import java.util.Optional;

public class EntityCommandDataProcessor extends AbstractEntityDataProcessor<EntityMinecartCommandBlock, CommandData, ImmutableCommandData> {

    public EntityCommandDataProcessor() {
        super(EntityMinecartCommandBlock.class);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean doesDataExist(EntityMinecartCommandBlock entity) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(EntityMinecartCommandBlock entity, Map<Key<?>, Object> keyValues) {
        CommandBlockBaseLogic logic = entity.getCommandBlockLogic();
        logic.setLastOutput(SpongeTexts.toComponent(((Optional<Text>) keyValues.get(Keys.LAST_COMMAND_OUTPUT)).orElse(Text.of())));
        logic.commandStored = (String) keyValues.get(Keys.COMMAND);
        logic.successCount = (int) keyValues.get(Keys.SUCCESS_COUNT);
        logic.setTrackOutput((boolean) keyValues.get(Keys.TRACKS_OUTPUT));
        entity.onUpdate();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityMinecartCommandBlock entity) {
        CommandBlockBaseLogic logic = entity.getCommandBlockLogic();
        Map<Key<?>, Object> values = Maps.newHashMapWithExpectedSize(4);
        Optional<Text> lastCommandOutput = logic.getLastOutput() != null ? Optional.of(SpongeTexts.toText(logic.getLastOutput())) : Optional.empty();
        values.put(Keys.LAST_COMMAND_OUTPUT, lastCommandOutput);
        values.put(Keys.COMMAND, logic.commandStored);
        values.put(Keys.SUCCESS_COUNT, logic.successCount);
        values.put(Keys.TRACKS_OUTPUT, logic.shouldTrackOutput());
        return values;
    }

    @Override
    protected CommandData createManipulator() {
        return new SpongeCommandData();
    }

}

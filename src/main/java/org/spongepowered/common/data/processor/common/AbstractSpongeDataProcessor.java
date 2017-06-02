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
package org.spongepowered.common.data.processor.common;

import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.DataProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractSpongeDataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataProcessor<M, I> {

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean supports(EntityType entityType) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<M> fill(DataMap container, M manipulator) {
        for (Key key : manipulator.getKeys()) {
            //TODO: further abstraction would be nice

            if (MapValue.class.isAssignableFrom(key.getValueToken().getRawType())) {
                //TODO: don't know MapValue generics at runtime :(
                throw new UnsupportedOperationException("MapValue is not supported yet");

            } else if (ListValue.class.isAssignableFrom(key.getValueToken().getRawType())) {
                List value = new ArrayList<>();
                container.getList(key.getQuery()).ifPresent(l ->
                        l.forEachKey(i ->
                                l.getObject(i, key.getElementToken().getRawType()).ifPresent(value::add)));
                manipulator.set((Key<? extends BaseValue<List>>) key, value);
            }

            Optional<?> value = container.getObject(key.getQuery(), key.getElementToken().getRawType());
            if (OptionalValue.class.isAssignableFrom(key.getValueToken().getRawType())) {
                manipulator.set((Key<? extends BaseValue<Object>>) key, value);
            } else if (value.isPresent()) {
                manipulator.set((Key<? extends BaseValue<Object>>) key, value.get());
            } else { //TODO: ability for some keys to be loaded with default values to make it more easy on config files that dont define all values
                return Optional.empty();
            }
        }
        return Optional.of(manipulator);
    }
}

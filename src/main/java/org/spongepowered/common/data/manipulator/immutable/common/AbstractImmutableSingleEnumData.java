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
package org.spongepowered.common.data.manipulator.immutable.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;

public abstract class AbstractImmutableSingleEnumData<E extends Enum<E>, I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>> extends
        AbstractImmutableSingleData<E, I, M> {

    private final Class<? extends M> mutableClass;
    private final E defaultValue;
    private final ImmutableValue<E> cachedValue;

    protected AbstractImmutableSingleEnumData(Class<I> immutableClass, E value, E defaultValue, Key<? extends BaseValue<E>> usedKey, Class<? extends M> mutableClass) {
        super(immutableClass, value, usedKey);
        checkArgument(!Modifier.isAbstract(mutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(mutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutableClass = checkNotNull(mutableClass);
        this.defaultValue = defaultValue;
        this.cachedValue = ImmutableSpongeValue.cachedOf(this.usedKey, this.defaultValue, this.value);
    }

    public final ImmutableValue<E> type() {
        return this.cachedValue;
    }

    @Override
    protected final ImmutableValue<E> getValueGetter() {
        return this.cachedValue;
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutableClass, this.getValue());
    }

    @Override
    public void toContainer(DataMap container) {
        super.toContainer(container);
        container.set(this.usedKey.getQuery(), this.value.name());
    }
}

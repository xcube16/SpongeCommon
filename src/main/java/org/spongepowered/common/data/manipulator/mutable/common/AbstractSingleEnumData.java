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
package org.spongepowered.common.data.manipulator.mutable.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;

/**
 * Another abstract helper class further simplifying implementing various
 * single value enum based {@link DataManipulator}s.
 *
 * @param <E>
 * @param <M>
 * @param <I>
 */
public abstract class AbstractSingleEnumData<E extends Enum<E>, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractSingleData<E, M, I> {

    private final Class<? extends I> immutableClass;
    private final E defaultValue;

    protected AbstractSingleEnumData(Class<M> manipulatorClass, E value, Key<? extends BaseValue<E>> usedKey, Class<? extends I> immutableClass, E defaultValue) {
        super(manipulatorClass, value, usedKey);
        checkArgument(!Modifier.isAbstract(immutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(immutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.immutableClass = checkNotNull(immutableClass);
        this.defaultValue = checkNotNull(defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public M copy() {
        return (M) ReflectionUtil.createInstance(this.getClass(), this.getValue());
    }

    @Override
    public I asImmutable() {
        return ImmutableDataCachingUtil.getManipulator(this.immutableClass, getValue());
    }

    @Override
    protected Value<E> getValueGetter() {
        return new SpongeValue<>(this.usedKey, this.defaultValue, this.getValue());
    }
}

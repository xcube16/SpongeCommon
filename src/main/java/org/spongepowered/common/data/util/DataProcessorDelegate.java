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
package org.spongepowered.common.data.util;

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.util.ServerUtils;

import java.util.Optional;

public final class DataProcessorDelegate<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataProcessor<M, I> {

    private final ImmutableList<Tuple<DataProcessor<M, I>, Timing>> processors;

    public DataProcessorDelegate(ImmutableList<DataProcessor<M, I>> processors) {
        ImmutableList.Builder<Tuple<DataProcessor<M, I>, Timing>> builder = ImmutableList.builder();
        for (DataProcessor<M, I> processor : processors) {
            builder.add(new Tuple<>(processor, SpongeTimingsFactory.ofSafe(SpongeImpl.getPlugin(), processor.getClass().getCanonicalName())));
        }
        this.processors = builder.build();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                if (callingFromMinecraftThread) {
                    tuple.getSecond().stopTiming();
                }
                return true;
            }
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
        }
        return false;
    }

    @Override
    public boolean supports(EntityType entityType) {
        return false;
    }

    @Override
    public Optional<M> from(DataHolder dataHolder) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                final Optional<M> optional = tuple.getFirst().from(dataHolder);
                if (callingFromMinecraftThread) {
                    tuple.getSecond().stopTiming();
                }
                if (optional.isPresent()) {
                    return optional;
                }
            }

            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, M manipulator, MergeFunction overlap) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                final Optional<M> optional = tuple.getFirst().fill(dataHolder, manipulator, overlap);
                if (callingFromMinecraftThread) {
                    tuple.getSecond().stopTiming();
                }
                if (optional.isPresent()) {
                    return optional;
                }
            }
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }

        }
        return Optional.empty();
    }

    @Override
    public Optional<M> fill(DataMap container, M m) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            final Optional<M> optional = tuple.getFirst().fill(container, m);
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                final DataTransactionResult result = tuple.getFirst().set(dataHolder, manipulator, function);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    if (callingFromMinecraftThread) {
                        tuple.getSecond().stopTiming();
                    }
                    return result;
                }
            }
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
        }
        return DataTransactionResult.failResult(manipulator.asImmutable().getValues());
    }

    @Override
    public Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            final Optional<I> optional = tuple.getFirst().with(key, value, immutable);
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
            if (optional.isPresent()) {
                return optional;

            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                final DataTransactionResult result = tuple.getFirst().remove(dataHolder);
                if (callingFromMinecraftThread) {
                    tuple.getSecond().stopTiming();
                }
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }

        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        final boolean callingFromMinecraftThread = ServerUtils.isCallingFromMainThread();

        for (Tuple<DataProcessor<M, I>, Timing> tuple : this.processors) {
            if (callingFromMinecraftThread) {
                tuple.getSecond().startTiming();
            }
            if (tuple.getFirst().supports(dataHolder)) {
                final Optional<M> optional = tuple.getFirst().createFrom(dataHolder);
                if (callingFromMinecraftThread) {
                    tuple.getSecond().stopTiming();
                }
                if (optional.isPresent()) {
                    return optional;
                }
            }
            if (callingFromMinecraftThread) {
                tuple.getSecond().stopTiming();
            }
        }
        return Optional.empty();
    }

}

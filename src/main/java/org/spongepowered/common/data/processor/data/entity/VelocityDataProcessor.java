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
package org.spongepowered.common.data.processor.data.entity;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Optional;

public class VelocityDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Vector3d, Value<Vector3d>, VelocityData, ImmutableVelocityData> {

    public VelocityDataProcessor() {
        super(Entity.class, Keys.VELOCITY);
    }

    @Override
    protected VelocityData createManipulator() {
        return new SpongeVelocityData();
    }

    @Override
    protected boolean set(Entity entity, Vector3d value) {
        ((IMixinEntity) entity).setImplVelocity(value);
        return true;
    }

    @Override
    protected Optional<Vector3d> getVal(Entity entity) {
        return Optional.of(((IMixinEntity) entity).getVelocity());
    }

    @Override
    public Optional<VelocityData> fill(DataMap container, VelocityData velocityData) {
        return container.getMap(Keys.VELOCITY.getQuery())
                .map(DataUtil::getVector3d)
                .map(vector3d -> velocityData.set(Keys.VELOCITY, vector3d));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<Vector3d> constructValue(Vector3d actualValue) {
        return new SpongeValue<>(Keys.VELOCITY, Vector3d.ZERO, actualValue);
    }

    @Override
    protected ImmutableValue<Vector3d> constructImmutableValue(Vector3d value) {
        return new ImmutableSpongeValue<>(Keys.VELOCITY, Vector3d.ZERO, value);
    }

}

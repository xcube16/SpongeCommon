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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBodyPartRotationalData;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBodyPartRotationalData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Map;
import java.util.Optional;

public class ImmutableSpongeBodyPartRotationalData extends AbstractImmutableData<ImmutableBodyPartRotationalData, BodyPartRotationalData>
        implements ImmutableBodyPartRotationalData {

    private final Map<BodyPart, Vector3d> rotations;
    private final ImmutableMapValue<BodyPart, Vector3d> rotationsValue;
    private final ImmutableValue<Vector3d> headRotationValue;
    private final ImmutableValue<Vector3d> chestRotationValue;
    private final ImmutableValue<Vector3d> leftArmRotationValue;
    private final ImmutableValue<Vector3d> rightArmRotationValue;
    private final ImmutableValue<Vector3d> leftLegRotationValue;
    private final ImmutableValue<Vector3d> rightLegRotationValue;

    public ImmutableSpongeBodyPartRotationalData(Map<BodyPart, Vector3d> rotations) {
        super(ImmutableBodyPartRotationalData.class);

        this.rotations = ImmutableMap.copyOf(checkNotNull(rotations, "rotations"));
        this.rotationsValue = new ImmutableSpongeMapValue<>(Keys.BODY_ROTATIONS, this.rotations);
        this.headRotationValue = new ImmutableSpongeValue<>(Keys.HEAD_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.HEAD)).orElse(DataConstants.DEFAULT_HEAD_ROTATION));
        this.chestRotationValue = new ImmutableSpongeValue<>(Keys.CHEST_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.CHEST)).orElse(DataConstants.DEFAULT_CHEST_ROTATION));
        this.leftArmRotationValue = new ImmutableSpongeValue<>(Keys.LEFT_ARM_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.LEFT_ARM)).orElse(DataConstants.DEFAULT_LEFT_ARM_ROTATION));
        this.rightArmRotationValue = new ImmutableSpongeValue<>(Keys.RIGHT_ARM_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.RIGHT_ARM)).orElse(DataConstants.DEFAULT_RIGHT_ARM_ROTATION));
        this.leftLegRotationValue = new ImmutableSpongeValue<>(Keys.LEFT_LEG_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.LEFT_LEG)).orElse(DataConstants.DEFAULT_LEFT_LEG_ROTATION));
        this.rightLegRotationValue = new ImmutableSpongeValue<>(Keys.RIGHT_LEG_ROTATION,
                Optional.ofNullable(this.rotations.get(BodyParts.RIGHT_LEG)).orElse(DataConstants.DEFAULT_RIGHT_LEG_ROTATION));

        registerGetters();
    }

    @Override
    public BodyPartRotationalData asMutable() {
        return new SpongeBodyPartRotationalData(this.rotations);
    }

    @Override
    public ImmutableMapValue<BodyPart, Vector3d> partRotation() {
        return this.rotationsValue;
    }

    @Override
    public ImmutableValue<Vector3d> headDirection() {
        return this.headRotationValue;
    }

    @Override
    public ImmutableValue<Vector3d> bodyRotation() {
        return this.chestRotationValue;
    }

    @Override
    public ImmutableValue<Vector3d> leftArmDirection() {
        return this.leftArmRotationValue;
    }

    @Override
    public ImmutableValue<Vector3d> rightArmDirection() {
        return this.rightArmRotationValue;
    }

    @Override
    public ImmutableValue<Vector3d> leftLegDirection() {
        return this.leftLegRotationValue;
    }

    @Override
    public ImmutableValue<Vector3d> rightLegDirection() {
        return this.rightLegRotationValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BODY_ROTATIONS, () -> this.rotations);
        registerKeyValue(Keys.BODY_ROTATIONS, this::partRotation);

        registerFieldGetter(Keys.HEAD_ROTATION, () -> this.rotations.get(BodyParts.HEAD));
        registerKeyValue(Keys.HEAD_ROTATION, this::headDirection);

        registerFieldGetter(Keys.CHEST_ROTATION, () -> this.rotations.get(BodyParts.CHEST));
        registerKeyValue(Keys.CHEST_ROTATION, this::bodyRotation);

        registerFieldGetter(Keys.LEFT_ARM_ROTATION, () -> this.rotations.get(BodyParts.LEFT_ARM));
        registerKeyValue(Keys.LEFT_ARM_ROTATION, this::leftArmDirection);

        registerFieldGetter(Keys.RIGHT_ARM_ROTATION, () -> this.rotations.get(BodyParts.RIGHT_ARM));
        registerKeyValue(Keys.RIGHT_ARM_ROTATION, this::rightArmDirection);

        registerFieldGetter(Keys.LEFT_LEG_ROTATION, () -> this.rotations.get(BodyParts.LEFT_LEG));
        registerKeyValue(Keys.LEFT_LEG_ROTATION, this::leftLegDirection);

        registerFieldGetter(Keys.RIGHT_LEG_ROTATION, () -> this.rotations.get(BodyParts.RIGHT_LEG));
        registerKeyValue(Keys.RIGHT_LEG_ROTATION, this::rightLegDirection);
    }

}

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
package org.spongepowered.common.data.builder.data.meta;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.data.meta.SpongePatternLayer;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;

/**
 * The de-facto builder for a {@link PatternLayer}.
 */
public class SpongePatternLayerBuilder extends AbstractDataBuilder<PatternLayer> implements PatternLayer.Builder, DataBuilder<PatternLayer> {


    private DyeColor color;
    private BannerPatternShape shape;

    public SpongePatternLayerBuilder() {
        super(PatternLayer.class, 1);
    }

    @Override
    protected Optional<PatternLayer> buildContent(DataMap container) throws InvalidDataException {
        Optional<BannerPatternShape> shape = container.getObject(DataQueries.BANNER_SHAPE_ID, BannerPatternShape.class);
        Optional<DyeColor> color = container.getObject(DataQueries.BANNER_COLOR, DyeColor.class);
        if (shape.isPresent() && color.isPresent()) {
            return Optional.of(new SpongePatternLayer(shape.get(), color.get()));
        }
        return Optional.empty();
    }

    @Override
    public SpongePatternLayerBuilder reset() {
        this.shape = null;
        this.color = null;
        return this;
    }

    @Override
    public PatternLayer.Builder pattern(BannerPatternShape shape) {
        this.shape = checkNotNull(shape);
        return this;
    }

    @Override
    public PatternLayer.Builder color(DyeColor color) {
        this.color = checkNotNull(color);
        return this;
    }

    @Override
    public PatternLayer build() {
        checkState(this.shape != null);
        checkState(this.color != null);
        return new SpongePatternLayer(this.shape, this.color);
    }
}

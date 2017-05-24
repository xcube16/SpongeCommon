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
package org.spongepowered.common.world.storage;

import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SpongePlayerData implements DataSerializable {

    UUID uuid;
    long firstJoined;
    long lastJoined;

    SpongePlayerData() {
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("uuid", this.uuid)
                .add("firstJoined", this.firstJoined)
                .add("lastJoined", this.lastJoined)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.firstJoined, this.lastJoined);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongePlayerData other = (SpongePlayerData) obj;
        return Objects.equals(this.uuid, other.uuid)
               && Objects.equals(this.firstJoined, other.firstJoined)
               && Objects.equals(this.lastJoined, other.lastJoined);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public void toContainer(DataMap container) {
        container
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.USER_UUID, this.uuid.toString())
                .set(DataQueries.PLAYER_DATA_JOIN, this.firstJoined)
                .set(DataQueries.PLAYER_DATA_LAST, this.lastJoined);
    }

    public static final class Builder extends AbstractDataBuilder<SpongePlayerData> implements DataBuilder<SpongePlayerData> {

        public Builder() {
            super(SpongePlayerData.class, 1);
        }

        @Override
        protected Optional<SpongePlayerData> buildContent(DataMap container) throws InvalidDataException {
            return container.getString(DataQueries.USER_UUID).map(UUID::fromString).map(uuid -> {
                final SpongePlayerData data = new SpongePlayerData();
                data.uuid = uuid;
                container.getLong(DataQueries.PLAYER_DATA_JOIN).ifPresent(f ->
                        data.firstJoined = f);
                container.getLong(DataQueries.PLAYER_DATA_LAST).ifPresent(l ->
                        data.lastJoined = l);
                return data;
            });
        }
    }
}

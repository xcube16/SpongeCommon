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
package org.spongepowered.common.data.processor.multi;

import com.google.common.collect.Maps;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.interfaces.IMixinMobSpawner;

import java.util.Map;
import java.util.Optional;

public class MobSpawnerDataProcessor extends AbstractMultiDataSingleTargetProcessor<IMixinMobSpawner, MobSpawnerData, ImmutableMobSpawnerData> {

    public MobSpawnerDataProcessor() {
        super(IMixinMobSpawner.class);
    }

    @Override
    protected boolean doesDataExist(IMixinMobSpawner entity) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(IMixinMobSpawner entity, Map<Key<?>, Object> values) {
        MobSpawnerBaseLogic logic = entity.getLogic();
        SpawnerUtils.applyData(logic, values);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(IMixinMobSpawner spawner) {
        MobSpawnerBaseLogic logic = spawner.getLogic();
        Map<Key<?>, Object> values = Maps.newIdentityHashMap();

        values.put(Keys.SPAWNER_REMAINING_DELAY, (short) logic.spawnDelay);
        values.put(Keys.SPAWNER_MINIMUM_DELAY, (short) logic.minSpawnDelay);
        values.put(Keys.SPAWNER_MAXIMUM_DELAY, (short) logic.maxSpawnDelay);
        values.put(Keys.SPAWNER_SPAWN_COUNT, (short) logic.spawnCount);
        values.put(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, (short) logic.maxNearbyEntities);
        values.put(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, (short) logic.activatingRangeFromPlayer);
        values.put(Keys.SPAWNER_SPAWN_RANGE, (short) logic.spawnRange);
        values.put(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, SpawnerUtils.getNextEntity(logic));
        values.put(Keys.SPAWNER_ENTITIES, SpawnerUtils.getEntities(logic));

        return values;
    }

    @Override
    protected MobSpawnerData createManipulator() {
        return new SpongeMobSpawnerData();
    }

    @Override
    public Optional<MobSpawnerData> fill(DataMap container, MobSpawnerData data) {
        // TODO: universal support for Key<Value<WeightedSerializableObject<EntityArchetype>>>
        // TODO: universal support for Key<WeightedCollectionValue<EntityArchetype>>

        container.getShort(Keys.SPAWNER_REMAINING_DELAY.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_REMAINING_DELAY, v));
        container.getShort(Keys.SPAWNER_MINIMUM_DELAY.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_MINIMUM_DELAY, v));
        container.getShort(Keys.SPAWNER_MAXIMUM_DELAY.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_MAXIMUM_DELAY, v));
        container.getShort(Keys.SPAWNER_SPAWN_COUNT.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_SPAWN_COUNT, v));
        container.getShort(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, v));
        container.getShort(Keys.SPAWNER_REQUIRED_PLAYER_RANGE.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, v));
        container.getShort(Keys.SPAWNER_SPAWN_RANGE.getQuery()).ifPresent(v ->
                data.set(Keys.SPAWNER_SPAWN_RANGE, v));

        container.getMap(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN.getQuery()).ifPresent(m ->
                getWeightedEntity(m).ifPresent(e ->
                        data.set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, e)));

        WeightedTable<EntityArchetype> spawns = new WeightedTable<>();
        container.getList(Keys.SPAWNER_ENTITIES.getQuery()).ifPresent(dl ->
                dl.forEachKey(i ->
                        dl.getMap(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN.getQuery()).ifPresent(m ->
                                getWeightedEntity(m).ifPresent(spawns::add))));
        data.set(Keys.SPAWNER_ENTITIES, spawns);

        return Optional.of(data);
    }

    private Optional<WeightedSerializableObject<EntityArchetype>> getWeightedEntity(DataMap data) {
        Optional<EntityArchetype> entity = data.getObject(Queries.WEIGHTED_SERIALIZABLE, EntityArchetype.class);
        Optional<Double> weight = data.getDouble(Queries.WEIGHTED_SERIALIZABLE_WEIGHT);
        if (entity.isPresent() && weight.isPresent()) {
            return Optional.of(new WeightedSerializableObject<>(entity.get(), weight.get().intValue()));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

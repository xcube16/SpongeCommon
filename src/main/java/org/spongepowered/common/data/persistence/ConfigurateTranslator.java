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
package org.spongepowered.common.data.persistence;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A translator for translating {@link DataView}s into {@link ConfigurationNode}
 * s.
 */
public class ConfigurateTranslator implements DataTranslator<ConfigurationNode> {

    private static final ConfigurateTranslator instance = new ConfigurateTranslator();
    private static final TypeToken<ConfigurationNode> TOKEN = TypeToken.of(ConfigurationNode.class);

    private ConfigurateTranslator() {
    }

    @Override
    public String getId() {
        return "sponge:configuration_node";
    }

    @Override
    public String getName() {
        return "ConfigurationNodeTranslator";
    }

    @Override
    public TypeToken<ConfigurationNode> getToken() {
        return TOKEN;
    }

    @Override
    public ConfigurationNode translate(DataView view) throws InvalidDataException {
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        translateIntoNode(node, view);
        return node;
    }

    public void translateIntoNode(ConfigurationNode node, DataView view) {
        checkNotNull(node, "node");
        checkNotNull(view, "container");
        node.setValue(viewsToMaps(view));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataContainer translate(ConfigurationNode node) throws InvalidDataException {
        checkNotNull(node, "node");
        DataContainer container = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        Object value = node.getValue();
        if (value == null) {
            return container; // empty node, so empty container
        }

        if (value instanceof Map) {
            ((Map) value).forEach((k, v) -> container.set(of(k.toString()), v));
        } else {
            throw new InvalidDataException("The ConfigurationNode's value was not a Map");
        }
        return container;
    }

    /**
     * Get the instance of this translator.
     *
     * @return The instance of this translator
     */
    public static ConfigurateTranslator instance() {
        return instance;
    }

    private static Object viewsToMaps(Object obj) {
        if (obj instanceof DataView) {
            DataView view = (DataView) obj;
            Map<DataQuery, Object> map = new LinkedHashMap<>();
            for (DataQuery key : view.getKeys(false)) {
                map.put(key, viewsToMaps(view.get(key)));
            }
            return map;
        } else if (obj instanceof List) {
            List list = (List) obj;
            List<Object> newList = new ArrayList<>(list.size());
            for (Object element : list) {
                newList.add(viewsToMaps(element));
            }
            return newList;
        }
        return obj;
    }
}
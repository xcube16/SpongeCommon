package org.spongepowered.common.data.generator;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.common.data.generator.key.KeyObject;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

public final class ValueGroupInfo {
    /* General Info */
    public final String keyValueId;
    public final Key key;

    /* Fields */
    public final String staticKeyFieldName;
    public final String instanceValueFieldName;
    public final Type fieldType;

    /* Methods */
    public final String valueMethodName;

    /* Getters (Maybe?) */
    @Nullable public final String getterDescriptor;
    /* Setters (Maybe?) */
    @Nullable public final String setterDescriptor;

    /* KeyObject for value generation */
    public final KeyObject<?> keyObject;

    public ValueGroupInfo(String keyValueId, Key key, String staticKeyFieldName, String instanceValueFieldName, Type fieldType,
            String valueMethodName,
            @Nullable String getterDescriptor, @Nullable String setterDescriptor, KeyObject<?> keyObject) {
        this.keyValueId = keyValueId;
        this.key = key;
        this.staticKeyFieldName = staticKeyFieldName;
        this.instanceValueFieldName = instanceValueFieldName;
        this.fieldType = fieldType;
        this.valueMethodName = valueMethodName;
        this.getterDescriptor = getterDescriptor;
        this.setterDescriptor = setterDescriptor;
        this.keyObject = keyObject;
    }
}

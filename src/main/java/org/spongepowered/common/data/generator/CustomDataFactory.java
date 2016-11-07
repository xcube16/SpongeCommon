package org.spongepowered.common.data.generator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.generator.key.BoundedKeyObject;
import org.spongepowered.common.data.generator.key.KeyObject;
import org.spongepowered.common.event.gen.DefineableClassLoader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CustomDataFactory {

    private final DefineableClassLoader classLoader = new DefineableClassLoader(getClass().getClassLoader());

    private static final CustomDataFactory INSTANCE = new CustomDataFactory();
    private CustomDataFactory() {

    }

    public static CustomDataFactory getInstance() {
        return INSTANCE;
    }

    Class<? extends DataManipulator<?, ?>> generateManipulatorClass(DataImplObject object) {
        return this.classLoader.defineClass(object.manipulatorClassName, generateMutableClass(object));
    }

    Class<? extends ImmutableDataManipulator<?, ?>> generateImmutableManipulatorClass(DataImplObject object) {
        return this.classLoader.defineClass(object.immutableClassName, generateImmutableClass(object));
    }

    Class<? extends DataManipulatorBuilder<?, ?>> generateManipulatorBuilderClass(DataImplObject data) {
        return this.classLoader.defineClass(data.manipulatorClassName + "_Builder", generateBuilderClass(data));
    }

    private byte[] generateMutableClass(DataImplObject object) {
        return new byte[0];
    }

    private byte[] generateImmutableClass(DataImplObject object) {
        return new byte[0];
    }

    private byte[] generateBuilderClass(DataImplObject object) {
        return new byte[0];
    }

    public static void resolveDisambiguators(DataImplObject data, List<ValueGroupDisambiguator> disambiguators) {
        final List<ValueGroupInfo> groupInfos = new ArrayList<>(disambiguators.size());
        for (ValueGroupDisambiguator disambiguator : disambiguators) {
            final String keyValueId = checkNotNull(disambiguator.matchedNameId);
            final String keyStaticFieldName = checkNotNull(disambiguator.generatedId);
            final String valueInstanceFieldName = checkNotNull(disambiguator.fieldInstanceId);
            final Type fieldType = checkNotNull(disambiguator.resolvedType.getType());
            final String valueMethodName = checkNotNull(disambiguator.valueDescriptor);
            final KeyObject keyObject = generateKeyObject(disambiguator);
            final String getterDescriptor = disambiguator.getterDescriptor;
            final String setterDescriptor = disambiguator.setterDescriptor;
            final ValueGroupInfo info = new ValueGroupInfo(keyValueId, disambiguator.key, keyStaticFieldName, valueInstanceFieldName, fieldType, valueMethodName, getterDescriptor, setterDescriptor, keyObject);
            groupInfos.add(info);
        }
        checkState(!groupInfos.isEmpty(), "No disambiguators discovered!");
        data.valueGroups = groupInfos;

    }

    private static KeyObject generateKeyObject(ValueGroupDisambiguator disambiguator) {
        if (disambiguator.key.getValueToken().isAssignableFrom(BoundedValue.class)) {
            final BoundedKeyObject keyObject = new BoundedKeyObject();
            keyObject.defaultValue = disambiguator.defaultValue;
            keyObject.minimum = checkNotNull(disambiguator.minimumValue);
            keyObject.maximum = checkNotNull(disambiguator.maximumValue);
            return keyObject;
        }
        final KeyObject keyObject = new KeyObject();
        keyObject.defaultValue = disambiguator.defaultValue;
        return keyObject;
    }
}

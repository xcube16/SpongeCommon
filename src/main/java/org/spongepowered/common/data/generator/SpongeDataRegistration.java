package org.spongepowered.common.data.generator;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.generator.DataRegistration;

public class SpongeDataRegistration<T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> implements DataRegistration<T, I> {

    private final Class<T> superManipulatorClass;
    private final Class<? extends T> mutableImplementationClass;
    private final Class<I> superImmutableClass;
    private final Class<? extends I> immutableImplementationClass;
    private final Class<? extends DataManipulatorBuilder<T, I>> builderImplementationClass;
    private final DataManipulatorBuilder<T, I> builderInstance;
    private final Object pluginInstance;

    public SpongeDataRegistration(Class<T> superManipulatorClass,
            Class<? extends T> mutableImplementationClass,
            Class<I> superImmutableClass,
            Class<? extends I> immutableImplementationClass,
            Class<? extends DataManipulatorBuilder<T, I>> builderImplementationClass,
            DataManipulatorBuilder<T, I> builderInstance, Object pluginInstance) {
        this.superManipulatorClass = superManipulatorClass;
        this.mutableImplementationClass = mutableImplementationClass;
        this.superImmutableClass = superImmutableClass;
        this.immutableImplementationClass = immutableImplementationClass;
        this.builderImplementationClass = builderImplementationClass;
        this.builderInstance = builderInstance;
        this.pluginInstance = pluginInstance;
    }

    @Override
    public Class<T> getSuperManipulator() {
        return this.superManipulatorClass;
    }

    @Override
    public Class<? extends T> getGeneratedImplClass() {
        return this.mutableImplementationClass;
    }

    @Override
    public Class<I> getSuperImmutable() {
        return this.superImmutableClass;
    }

    @Override
    public Class<? extends I> getGeneratedImmutable() {
        return this.immutableImplementationClass;
    }

    @Override
    public Class<? extends DataManipulatorBuilder<T, I>> getBuilderClass() {
        return this.builderImplementationClass;
    }

    @Override
    public Object getPluginInstance() {
        return this.pluginInstance;
    }

    @Override
    public T makeDefaultMutable() {
        return this.builderInstance.create();
    }

    @Override
    public I makeDefaultImmutable() {
        return this.builderInstance.create().asImmutable();
    }

    @Override
    public DataManipulatorBuilder<T, I> getBuilder() {
        return this.builderInstance;
    }
}

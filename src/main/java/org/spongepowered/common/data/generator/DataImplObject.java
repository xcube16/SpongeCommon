package org.spongepowered.common.data.generator;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

import java.util.List;
import java.util.function.Predicate;

public class DataImplObject {

    Class<? extends DataManipulator<?, ?>> superManipulatorClass;
    public String manipulatorClassName;
    Class<? extends ImmutableDataManipulator<?, ?>> superImmutableClass;
    public String manipulatorDescriptor;
    public String immutableClassName;
    List<ValueGroupInfo> valueGroups;
    Predicate<? extends DataHolder> applyPredicate = (holder) -> true;
    public int version;
}

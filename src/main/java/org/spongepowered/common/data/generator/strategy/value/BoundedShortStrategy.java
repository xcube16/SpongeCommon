package org.spongepowered.common.data.generator.strategy.value;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.generator.DataImplObject;
import org.spongepowered.common.data.generator.ValueGroupDisambiguator;
import org.spongepowered.common.data.generator.ValueGroupInfo;
import org.spongepowered.common.data.generator.key.BoundedKeyObject;
import org.spongepowered.common.data.value.SpongeValueFactory;

/*
This is just realistically a template to use, all of the type information is going to be extracted out based
on the Type or TypeToken.
 */
public class BoundedShortStrategy {

    private static final Method SHORT_VALUE_OF = new Method("valueOf", Type.getType(Short.class), new Type[]{Type.SHORT_TYPE});
    private static final Method BOUNDED_BUILDER =
            new Method("boundedBuilder", Type.getType(ValueFactory.BoundedValueBuilder.class), new Type[]{Type.getType(Key.class)});
    private static final Method DEFAULT_VALUE =
            new Method("defaultValue", Type.getType(ValueFactory.BoundedValueBuilder.class), new Type[]{Type.getType(Object.class)});
    public static final Method MINIMUM =
            new Method("minimum", Type.getType(ValueFactory.BoundedValueBuilder.class), new Type[]{Type.getType(Object.class)});
    public static final Method MAXIMUM =
            new Method("maximum", Type.getType(ValueFactory.BoundedValueBuilder.class), new Type[]{Type.getType(Object.class)});
    public static final Method ACTUAL =
            new Method("actualValue", Type.getType(ValueFactory.BoundedValueBuilder.class), new Type[]{Type.getType(Object.class)});
    public static final Method BUILD = new Method("build", Type.getType(MutableBoundedValue.class), new Type[]{});

    public void visit(ClassWriter cw, DataImplObject data, ValueGroupInfo container, String methodName, BoundedKeyObject<Short> keyObject) {
        final String className = data.manipulatorClassName;
        final String baseFieldName = container.staticKeyFieldName;

        final Method valueGetter = new Method(methodName, Type.getType(MutableBoundedValue.class), new Type[]{});
        final GeneratorAdapter ga = new GeneratorAdapter(ACC_PUBLIC, valueGetter, null, null, cw);

        ga.getStatic(Type.getType(className), container.staticKeyFieldName, Type.getType(Key.class));
        ga.invokeStatic(Type.getType(SpongeValueFactory.class), BOUNDED_BUILDER);

        ga.push(keyObject.defaultValue);
        ga.invokeStatic(Type.getType(Short.class), SHORT_VALUE_OF);
        ga.invokeInterface(Type.getType(ValueFactory.BoundedValueBuilder.class), DEFAULT_VALUE);


        ga.push(keyObject.minimum);
        ga.invokeStatic(Type.getType(Short.class), SHORT_VALUE_OF);
        ga.invokeInterface(Type.getType(ValueFactory.BoundedValueBuilder.class), MINIMUM);

        ga.push(keyObject.maximum);
        ga.invokeStatic(Type.getType(Short.class), SHORT_VALUE_OF);
        ga.invokeInterface(Type.getType(ValueFactory.BoundedValueBuilder.class), MAXIMUM);

        ga.loadThis();
        ga.getField(Type.getType(className), baseFieldName, Type.SHORT_TYPE);
        ga.invokeStatic(Type.getType(Short.class), SHORT_VALUE_OF);
        ga.invokeInterface(Type.getType(ValueFactory.BoundedValueBuilder.class), ACTUAL);

        ga.invokeInterface(Type.getType(ValueFactory.BoundedValueBuilder.class), BUILD);

        ga.returnValue();
        ga.visitMaxs(0, 0);
        ga.endMethod();
        
    }

}

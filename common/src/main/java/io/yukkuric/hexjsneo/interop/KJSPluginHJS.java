package io.yukkuric.hexjsneo.interop;

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.TreeList;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.*;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import io.yukkuric.hexjsneo.casting.*;

public class KJSPluginHJS implements KubeJSPlugin {
    public void registerBindings(BindingRegistry event) {
        if (event.type().isClient()) return;
        event.add("HexPattern", HexPattern.class);
        event.add("OperatorSideEffect", OperatorSideEffect.class);

        // build HexJS object
        var context = event.context();
        var HexJS = new NativeObject(context.factory);
        context.addToScope(HexJS, "ActionJS", ActionJS.class);
        context.addToScope(HexJS, "ArgsJS", ArgsJS.class);
        context.addToScope(HexJS, "Args", ArgsJS.class);
        context.addToScope(HexJS, "ActionRegistryJS", ActionRegistryJS.class);
        event.add("HexJS", HexJS);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        // TODO, not working at all
        registry.register(TreeList.class, (Context c, Object raw) -> switch (raw) {
            case null -> null;
            case TreeList<?> yup -> yup;
            case Iterable<?> screwYouKJSWhyWrapNonStandardList -> TreeList.from(screwYouKJSWhyWrapNonStandardList);
            default ->
                    throw new KubeRuntimeException("Expected iterable, got %s".formatted(raw)).source(SourceLine.of(c));
        });
    }
}

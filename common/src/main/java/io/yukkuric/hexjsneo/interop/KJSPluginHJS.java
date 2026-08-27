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

import java.util.List;

public class KJSPluginHJS implements KubeJSPlugin {
    private static List<Class<?>> CUSTOM_JS_CLASSES = List.of(
            ActionJS.class,
            ActionRegistryJS.class,
            CastingEnvironmentComponentJS.class,

            ArgsJS.class
    );

    public void registerBindings(BindingRegistry event) {
        if (event.type().isClient()) return;
        event.add("TreeList", TreeList.class);
        event.add("HexPattern", HexPattern.class);
        event.add("OperatorSideEffect", OperatorSideEffect.class);

        // build HexJS object
        var context = event.context();
        var HexJS = new NativeObject(context.factory);
        for (var cls : CUSTOM_JS_CLASSES) {
            var name = cls.getSimpleName();
            context.addToScope(HexJS, name, cls);
            context.addToScope(HexJS, name.substring(0, name.length() - 2), cls);
        }
        event.add("HexJS", HexJS);
    }
}

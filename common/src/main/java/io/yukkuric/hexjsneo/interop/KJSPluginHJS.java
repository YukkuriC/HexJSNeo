package io.yukkuric.hexjsneo.interop;

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.rhino.NativeObject;
import io.yukkuric.hexjsneo.casting.ActionJS;
import io.yukkuric.hexjsneo.casting.ActionRegistryJS;

public class KJSPluginHJS implements KubeJSPlugin {
    public void registerBindings(BindingRegistry event) {
        if (event.type().isClient()) return;
        event.add("HexPattern", HexPattern.class);
        event.add("OperatorSideEffect", OperatorSideEffect.class);

        // build HexJS object
        var context = event.context();
        var HexJS = new NativeObject(context.factory);
        context.addToScope(HexJS, "ActionJS", ActionJS.class);
        context.addToScope(HexJS, "ActionRegistryJS", ActionRegistryJS.class);
        event.add("HexJS", HexJS);
    }
}

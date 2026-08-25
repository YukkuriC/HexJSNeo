package io.yukkuric.hexjsneo.interop;

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class KJSPluginHJS implements KubeJSPlugin {
    public void registerBindings(BindingRegistry event) {
        if (event.type().isClient()) return;
        event.add("HexJS", HexJS.class);
        event.add("HexPattern", HexPattern.class);
        event.add("OperatorSideEffect", OperatorSideEffect.class);
    }
}

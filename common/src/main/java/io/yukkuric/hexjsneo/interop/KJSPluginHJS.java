package io.yukkuric.hexjsneo.interop;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class KJSPluginHJS implements KubeJSPlugin {
    public void registerBindings(BindingRegistry event) {
        if (event.type().isClient()) return;
    }
}

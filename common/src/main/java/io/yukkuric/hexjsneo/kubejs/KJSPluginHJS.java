package io.yukkuric.hexjsneo.kubejs;

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.TreeList;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.*;
import io.yukkuric.hexjsneo.casting.*;
import io.yukkuric.hexjsneo.kubejs.sub.HexJS;
import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector;

import java.util.List;

public class KJSPluginHJS implements KubeJSPlugin {
    public void init() {
        HexAPICollector.INSTANCE.init();
    }

    public static final List<Class<?>> CUSTOM_JS_CLASSES = List.of(
            ActionJS.class,
            ActionRegistryJS.class,
            SpecialHandlerJS.class,
            IotaJS.class,
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
        event.add("HexJS", new HexJS(CUSTOM_JS_CLASSES, context.factory));
    }
}

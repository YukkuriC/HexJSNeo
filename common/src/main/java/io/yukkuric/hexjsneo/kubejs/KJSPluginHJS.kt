package io.yukkuric.hexjsneo.kubejs

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin
import dev.latvian.mods.kubejs.script.BindingRegistry
import io.yukkuric.hexjsneo.casting.*
import io.yukkuric.hexjsneo.kubejs.sub.HexJS
import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector

class KJSPluginHJS : KubeJSPlugin {
    override fun init() {
        HexAPICollector.init()
    }

    override fun registerBindings(event: BindingRegistry) {
        // build HexJS object
        event.add("HexJS", HexJS(CUSTOM_JS_CLASSES))
    }

    companion object {
        val CUSTOM_JS_CLASSES = listOf(
            ActionJS::class.java,
            ActionRegistryJS::class.java,
            SpecialHandlerJS::class.java,
            IotaJS::class.java,
            CastingEnvironmentComponentJS::class.java,

            ArgsJS::class.java
        )
    }
}
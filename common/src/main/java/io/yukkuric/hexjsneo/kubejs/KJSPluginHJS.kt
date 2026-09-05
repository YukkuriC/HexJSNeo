package io.yukkuric.hexjsneo.kubejs

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.lib.HexRegistries
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents
import dev.latvian.mods.kubejs.script.BindingRegistry
import dev.latvian.mods.kubejs.script.ScriptManager
import dev.latvian.mods.kubejs.script.ScriptType
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry
import dev.latvian.mods.kubejs.server.tag.TagKubeEvent
import io.yukkuric.hexjsneo.casting.*
import io.yukkuric.hexjsneo.ext.toIotaKJSStrict
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

    override fun registerTypeWrappers(registry: TypeWrapperRegistry) {
        registry.register(Iota::class.java) { it.toIotaKJSStrict() }
    }

    override fun beforeScriptsLoaded(manager: ScriptManager) {
        if (manager.scriptType != ScriptType.SERVER) return
        ServerEvents.TAGS.listenJava(ScriptType.SERVER, HexRegistries.ACTION.location()) {
            val e = (it as TagKubeEvent)
            val greatActionIds = ActionRegistryJS.MAP_GREAT.values.map { it.id }
            for (tag in GREAT_PATTERN_TAGS) {
                e.add(tag, greatActionIds)
            }
        }
    }

    companion object {
        val GREAT_PATTERN_TAGS = listOf(
            HexTags.Actions.PER_WORLD_PATTERN.location,
            HexTags.Actions.CAN_START_ENLIGHTEN.location,
            HexTags.Actions.REQUIRES_ENLIGHTENMENT.location,
        )
        val CUSTOM_JS_CLASSES = listOf(
            ActionJS::class.java,
            ActionRegistryJS::class.java,
            SpecialHandlerJS::class.java,
            IotaJS::class.java,
            CastingEnvironmentComponentJS::class.java,
            MishapJS::class.java,

            ArgsJS::class.java
        )
    }
}
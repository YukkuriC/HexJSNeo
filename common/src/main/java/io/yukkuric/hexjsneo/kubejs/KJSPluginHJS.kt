package io.yukkuric.hexjsneo.kubejs

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.lib.HexRegistries
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.bindings.event.ServerEvents
import dev.latvian.mods.kubejs.script.*
import dev.latvian.mods.kubejs.server.tag.TagEventJS
import dev.latvian.mods.rhino.util.wrap.TypeWrappers
import io.yukkuric.hexjsneo.casting.*
import io.yukkuric.hexjsneo.ext.toIotaKJSStrict
import io.yukkuric.hexjsneo.kubejs.sub.HexJS
import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector

class KJSPluginHJS : KubeJSPlugin() {
    override fun init() {
        HexAPICollector.init()
    }

    override fun registerBindings(event: BindingsEvent) {
        // build HexJS object
        event.add("HexJS", HexJS(CUSTOM_JS_CLASSES))
    }

    override fun registerTypeWrappers(type: ScriptType, typeWrappers: TypeWrappers) {
        typeWrappers.register(Iota::class.java) { _, it -> it.toIotaKJSStrict() }
    }

    override fun onServerReload() {
        ServerEvents.TAGS.listenJava(ScriptType.SERVER, HexRegistries.ACTION.location()) {
            val e = (it as TagEventJS)
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
            ArithmeticJS::class.java,
            OperatorJS::class.java,
            CastingEnvironmentComponentJS::class.java,
            MishapJS::class.java,

            ArgsJS::class.java
        )
    }
}
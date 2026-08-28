package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.latvian.mods.rhino.Undefined
import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.ext.asUnsupportedKJS
import io.yukkuric.hexjsneo.ext.wrapTryKJS
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * KJS special handler all in one: registry and logics
 */
class SpecialHandlerJS(val id: ResourceLocation, var handler: (HexPattern, CastingEnvironment) -> Any?) :
    SpecialHandler.Factory<SpecialHandler> {
    override fun tryMatch(pattern: HexPattern, env: CastingEnvironment) = wrapTryKJS {
        when (val ret = handler(pattern, env)) {
            null, is Undefined -> null
            is SpecialHandler -> ret
            else -> ret.asUnsupportedKJS
        }
    }

    /** KJS-ish chain setter */
    fun setTryMatch(func: (HexPattern, CastingEnvironment) -> Any?) {
        handler = func
    }

    companion object {
        /** helper for building `tryMatch` returns */
        @JvmStatic
        fun create(action: Action, name: Component) = Holder(action, name)

        val HOLDER = HashMap<ResourceLocation, SpecialHandlerJS>()

        fun register(regFunc: (ResourceLocation, SpecialHandlerJS) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value)
        }
    }

    init {
        HOLDER[id] = this

        IXplatAbstractions.INSTANCE?.specialHandlerRegistry?.let { reg ->
            // hot swap
            if (reg.containsKey(id)) {
                (reg[id] as? SpecialHandlerJS)?.let { it.handler = handler }
                    ?: HexJSNeo.LOGGER.warn("Non-KJS SpecialHandler overrides not supported: $id")
            }
        }
    }

    data class Holder(val action: Action, val display: Component) : SpecialHandler {
        override fun act() = action
        override fun getName() = display
    }
}
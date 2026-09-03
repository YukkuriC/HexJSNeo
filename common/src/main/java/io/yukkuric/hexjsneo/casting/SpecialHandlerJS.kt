package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.latvian.mods.kubejs.typings.Info
import dev.latvian.mods.rhino.Undefined
import io.yukkuric.hexjsneo.ext.*
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

@Info("KJS special handler all in one: registry and logics")
class SpecialHandlerJS(override val id: ResourceLocation, var handler: (HexPattern, CastingEnvironment) -> Any?) :
    BoxedContent, SpecialHandler.Factory<SpecialHandler>, PipeSelf<SpecialHandlerJS> {
    override fun tryMatch(pattern: HexPattern, env: CastingEnvironment) = wrapTryKJS {
        when (val ret = handler(pattern, env)) {
            null, is Undefined -> null
            is SpecialHandler -> ret
            else -> ret.asUnsupportedKJS
        }
    }

    @Info("KJS-ish chain setter for `tryMatch`")
    fun setTryMatch(func: (HexPattern, CastingEnvironment) -> Any?) = also { handler = func }

    companion object {
        @Info("helper for building `tryMatch` returns")
        @JvmStatic
        fun create(action: Action, name: Component) = Holder(action, name)

        val HOLDER = HashMap<ResourceLocation, SpecialHandlerJS>()

        fun register(regFunc: (ResourceLocation, SpecialHandler.Factory<*>) -> Any?) {
            for ((key, value) in HOLDER.entries) regFunc(key, BoxedSpecialHandler(value))
        }

        class BoxedSpecialHandler(override var inner: SpecialHandlerJS) : SpecialHandler.Factory<SpecialHandler>,
            BoxedRegistry<SpecialHandlerJS, SpecialHandler.Factory<SpecialHandler>, BoxedSpecialHandler> {
            override fun tryMatch(pattern: HexPattern, env: CastingEnvironment) = inner.tryMatch(pattern, env)
        }
    }

    init {
        HOLDER[id] = this
        hotSwap(IXplatAbstractions.INSTANCE?.specialHandlerRegistry)
    }

    data class Holder(val action: Action, val display: Component) : SpecialHandler {
        override fun act() = action
        override fun getName() = display
    }
}
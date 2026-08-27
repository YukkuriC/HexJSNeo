package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.ext.OverrideHelper
import io.yukkuric.hexjsneo.ext.asUnsupportedKJS
import io.yukkuric.hexjsneo.ext.unwrapKJS
import io.yukkuric.hexjsneo.mixin.MutableActionRegistryEntry
import net.minecraft.resources.ResourceLocation

/**
 * Helper class for registry & hot-reload KJS patterns & actions
 */
data class ActionRegistryJS(
    val prototype: HexPattern,
    val id: ResourceLocation,
    val action: ActionJS,
    val isGreat: Boolean
) {
    companion object {
        val HOLDER = HashMap<ResourceLocation, ActionRegistryJS>()
        val MAP_NORMAL = HashMap<String, ActionRegistryJS>()
        val MAP_GREAT = HashMap<HexPattern, ActionRegistryJS>()

        fun register(regFunc: (ResourceLocation, ActionRegistryEntry) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value.asEntry)
        }

        /**
         * convenient helper for weak-typed constructor
         */
        @JvmStatic
        fun of(vararg args: Any?): ActionRegistryJS {
            val argPattern = OverrideHelper<HexPattern>("pattern")
            val argId = OverrideHelper<ResourceLocation>("id")
            val argAction = OverrideHelper<ActionJS>("action")
            val argIsGreat = OverrideHelper<Boolean>("isGreat")

            args.forEach {
                when (it) {
                    is HexPattern -> argPattern.update(it)
                    is ActionJS -> argAction.update(it)
                    is String -> argId.update(ResourceLocation.tryParse(it) ?: throw it.asUnsupportedKJS)
                    is ResourceLocation -> argId.update(it)
                    is Boolean -> argIsGreat.update(it)
                    else -> throw it.asUnsupportedKJS
                }
            }

            val pat = argPattern.get() ?: throw IllegalArgumentException("missing prototype")
            return ActionRegistryJS(
                pat,
                argId.get { HexJSNeo.modLoc(pat.anglesSignature()) },
                argAction.get(::ActionJS),
                argIsGreat.get(false)
            )
        }
    }

    init {
        HOLDER[id] = this
        if (isGreat) MAP_GREAT[prototype] = this
        else MAP_NORMAL[prototype.anglesSignature()] = this

        IXplatAbstractions.INSTANCE?.actionRegistry?.let { reg ->
            // replace existing registry for hot swap
            if (reg.containsKey(id)) {
                reg[id].let {
                    (it as MutableActionRegistryEntry).let { entry ->
                        entry.setAction(action)
                        entry.setPrototype(prototype)
                    }
                }
            }
        }
    }

    val asEntry by lazy { ActionRegistryEntry(prototype, action) }

    //#region KJS transparent handlers
    fun setOperate(newFun: OperateMethodRaw<*>): ActionRegistryJS {
        action.setOperate(newFun)
        return this
    }

    fun setOperateInParens(newFun: OperateParenMethodRaw<*>): ActionRegistryJS {
        action.setOperateInParens(newFun)
        return this
    }

    fun setOperateMutableStack(newFun: MutableStackMethod): ActionRegistryJS {
        action.setOperateMutableStack(newFun)
        return this
    }
    //#endregion
}
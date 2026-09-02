package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.latvian.mods.kubejs.typings.Info
import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.ext.PipeSelf
import io.yukkuric.hexjsneo.ext.OverrideHelper
import io.yukkuric.hexjsneo.ext.asUnsupportedKJS
import io.yukkuric.hexjsneo.mixin.MutableActionRegistryEntry
import io.yukkuric.hexjsneo.mixin_interface.LazyCastingImage
import net.minecraft.resources.ResourceLocation

@Info("Helper class for registry & hot-reload KJS patterns & actions")
data class ActionRegistryJS(
    val prototype: HexPattern,
    val id: ResourceLocation,
    val action: ActionJS,
    val isGreat: Boolean
) : PipeSelf<ActionRegistryJS> {
    companion object {
        val HOLDER = HashMap<ResourceLocation, ActionRegistryJS>()
        val MAP_NORMAL = HashMap<String, ActionRegistryJS>()
        val MAP_GREAT = HashMap<HexPattern, ActionRegistryJS>()

        fun register(regFunc: (ResourceLocation, ActionRegistryEntry) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value.asEntry)
        }

        @Info("convenient helper for weak-typed constructor")
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
                    is String -> argId.update(ResourceLocation.tryParse(it) ?: it.asUnsupportedKJS)
                    is ResourceLocation -> argId.update(it)
                    is Boolean -> argIsGreat.update(it)
                    else -> it.asUnsupportedKJS
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
                (reg[id] as MutableActionRegistryEntry).let { entry ->
                    entry.setAction(action)
                    entry.setPrototype(prototype)
                }
            }
        }
    }

    val asEntry by lazy { ActionRegistryEntry(prototype, action) }

    //#region KJS transparent handlers
    @Info("KJS-ish operate method setter")
    fun setOperate(newFun: OperateMethodRaw<*>) = modify { action.setOperate(newFun) }

    @Info("KJS-ish paren operate method setter")
    fun setOperateInParens(newFun: OperateParenMethodRaw<*>) = modify { action.setOperateInParens(newFun) }

    @Info("Special KJS-ish paren operate method setter: accepts a mutable whole stack argument at first of the method")
    fun setOperateMutableStack(newFun: MutableStackMethod) = modify { action.setOperateMutableStack(newFun) }

    @Info("Special KJS-ish operate method setter: accepts how many stack elements to be transformed into initial `ArgsJS` object, and inserts it as the first argument, just like a ConstMediaAction or SpellAction")
    fun setOperateArgsSplit(argCount: Int, newFun: ArgsSplitMethod) =
        modify { action.setOperateArgsSplit(argCount, newFun) }

    // @Info("set >0 to auto-add a ConsumeMedia into default side effect list")
    val mediaCost by action::mediaCost
    @Info("mishaps if CastEnv can't afford the amount")
    fun preCheckMedia(env: CastingEnvironment, cost: Long) = action.preCheckMedia(env, cost)
    @Info("mishaps if CastEnv can't afford current set mediaCost")
    fun preCheckMedia(env: CastingEnvironment) = action.preCheckMedia(env)
    //#endregion
}
package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.latvian.mods.kubejs.typings.Info
import dev.latvian.mods.rhino.util.HideFromJS
import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.ext.*
import io.yukkuric.hexjsneo.mixin.MutableActionRegistryEntry
import net.minecraft.resources.ResourceLocation

@Info("Helper class for registry & hot-reload KJS patterns & actions")
data class ActionRegistryJS(
    val prototype: HexPattern,
    val id: ResourceLocation,
    val action: ActionJS,
    val isGreat: Boolean
) : PipeSelf<ActionRegistryJS> {
    companion object {
        val REGEX_PATTERN = Regex("(?:(east|west|north_east|north_west|south_east|south_west),\\s*)?([wedsaq]+)")

        val HOLDER = HashMap<ResourceLocation, ActionRegistryJS>()
        val MAP_NORMAL = HashMap<String, ActionRegistryJS>()
        val MAP_GREAT = HashMap<HexPattern, ActionRegistryJS>()

        @HideFromJS
        fun register(regFunc: (ResourceLocation, ActionRegistryEntry) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value.asEntry)
        }

        @Info("convenient helper for weak-typed constructor, receiving: HexPattern pattern, ResourceLocation id, [ActionJS action, boolean isGreat]")
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
                    is ResourceLocation -> argId.update(it)
                    is Boolean -> argIsGreat.update(it)

                    // string testing both pattern str and ResLoc
                    is String -> {
                        // try pattern
                        val match = REGEX_PATTERN.matchEntire(it.lowercase())
                        if (match != null) {
                            val dir = match.groupValues[1].uppercase().let { dir ->
                                if (dir == "") HexDir.EAST else HexDir.valueOf(dir)
                            }
                            val sig = match.groupValues[2]
                            argPattern.update(HexPattern.fromAngleString(sig, dir, false))
                        }

                        // try resloc or fail
                        else {
                            argId.update(ResourceLocation.tryParse(it) ?: it.asUnsupportedKJS)
                        }
                    }

                    else -> it.asUnsupportedKJS
                }
            }

            val pat = argPattern.get() ?: throw IllegalArgumentException("missing prototype")
            return ActionRegistryJS(
                pat,
                argId.get { HexJSNeo.modLoc(pat.signature.toAnglesString()) },
                argAction.get(::ActionJS),
                argIsGreat.get(false)
            )
        }
    }

    init {
        HOLDER[id] = this
        if (isGreat) MAP_GREAT[prototype] = this
        else MAP_NORMAL[prototype.signature.toAnglesString()] = this

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

    private val asEntry by lazy { ActionRegistryEntry(prototype, action) }

    //#region KJS transparent handlers
    @Info("KJS-ish operate method setter")
    fun setOperate(newFun: OperateMethodRaw<*>) = also { action.setOperate(newFun) }

    @Info("KJS-ish paren operate method setter")
    fun setOperateInParens(newFun: OperateParenMethodRaw<*>) = also { action.setOperateInParens(newFun) }

    @Info("Special KJS-ish paren operate method setter: accepts a mutable whole stack argument at first of the method")
    fun setOperateMutableStack(newFun: MutableStackMethod) = also { action.setOperateMutableStack(newFun) }

    @Info("Special KJS-ish operate method setter: accepts how many stack elements to be transformed into initial `ArgsJS` object, and inserts it as the first argument, just like a ConstMediaAction or SpellAction")
    fun setOperateArgsSplit(argCount: Int, newFun: ArgsSplitMethod) =
        also { action.setOperateArgsSplit(argCount, newFun) }

    @get:Info("set >0 to auto-add a ConsumeMedia into default side effect list")
    val mediaCost by action::mediaCost
    @Info("mishaps if CastEnv can't afford the amount")
    fun preCheckMedia(env: CastingEnvironment, cost: Long) = action.preCheckMedia(env, cost)
    @Info("mishaps if CastEnv can't afford current set mediaCost")
    fun preCheckMedia(env: CastingEnvironment) = action.preCheckMedia(env)
    //#endregion
}
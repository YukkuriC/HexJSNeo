package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import dev.latvian.mods.rhino.JavaScriptException
import dev.latvian.mods.rhino.Undefined
import dev.latvian.mods.rhino.WrappedException
import io.yukkuric.hexjsneo.ext.OverrideHelper
import io.yukkuric.hexjsneo.ext.asUnsupportedKJS
import io.yukkuric.hexjsneo.ext.toIotaKJS
import io.yukkuric.hexjsneo.ext.unwrapKJS
import net.minecraft.network.chat.Component

typealias OperateMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation) -> R
typealias OperateParenMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation, Iota) -> R
typealias OperateMethod = OperateMethodRaw<OperationResult>
typealias OperateParenMethod = OperateParenMethodRaw<ParenthesizedOperationResult>
typealias MutableStackMethod = (MutableList<Iota>, CastingEnvironment, CastingImage, SpellContinuation) -> Any

/**
 * Base for all KubeJS-registered actions
 */
class ActionJS(
    opRaw: OperateMethodRaw<*>? = null, opInParensRaw: OperateParenMethodRaw<*>? = null
) : Action {
    companion object {
        val DUMMY_OPERATE: OperateMethod =
            { env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation ->
                env.castingEntity?.sendSystemMessage(Component.literal("hello hexjs"))
                OperationResult(image, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE.get())
            }
        val DUMMY_OPERATE_PARENS: OperateParenMethod =
            { env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota ->
                ParenthesizedOperationResult(
                    image.withNewParenthesized(thisIota),
                    listOf(),
                    continuation,
                    HexEvalSounds.NORMAL_EXECUTE.get(),
                    ResolvedPatternType.ESCAPED
                )
            }
        val TODO_IMAGE = CastingImage()
    }

    var _operate: OperateMethod = DUMMY_OPERATE
    var _operateInParens: OperateParenMethod = DUMMY_OPERATE_PARENS
    override fun operate(
        env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation
    ): OperationResult {
        ArgsJS.InjectContext(env)
        return wrapTry { _operate(env, image, continuation) }
    }

    override fun operateInParens(
        env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota
    ): ParenthesizedOperationResult {
        ArgsJS.InjectContext(env)
        return wrapTry { _operateInParens(env, image, continuation, thisIota) }
    }

    //#region wrappers
    lateinit var lazyStack: Lazy<MutableList<Iota>>
    lateinit var lazyParenList: Lazy<MutableList<CastingImage.ParenthesizedIota>>

    private fun wrapJSReturn(
        ret: Any?,
        sideEffects: MutableList<OperatorSideEffect>,
        continuation: SpellContinuation,
        addIota: (iota: Iota) -> Unit,
        addParened: ((iota: CastingImage.ParenthesizedIota) -> Unit)? = null,
    ): OperationResult? {
        var ret = ret
        // env.castingEntity?.sendSystemMessage(Component.literal("$ret is class ${ret?.javaClass?.simpleName}"))

        // null or undefined = empty list
        if (ret is Undefined || ret == null) ret = listOf<Iota>()

        // list: add stack or sideEffect
        if (ret is Array<*>) ret = ret.asIterable()
        if (ret is Iterable<*>) {
            var overrideSound = OverrideHelper<EvalSound>("sound")
            var overrideCont = OverrideHelper<SpellContinuation>("continuation")
            ret.forEach { sub: Any? ->
                sub.toIotaKJS()?.let(addIota) ?: when (sub) {
                    is CastingImage.ParenthesizedIota -> (addParened ?: sub.asUnsupportedKJS)(sub)

                    is OperatorSideEffect -> sideEffects.add(sub)
                    is EvalSound -> overrideSound.update(sub)
                    is SpellContinuation -> overrideCont.update(sub)

                    else -> sub.asUnsupportedKJS
                }
            }
            return OperationResult(
                TODO_IMAGE,
                sideEffects,
                overrideCont.get(continuation),
                overrideSound.get(HexEvalSounds.NORMAL_EXECUTE::get),
            )
        }

        // full result: direct return
        if (ret is OperationResult) return ret

        // mishap: help throw
        if (ret is Mishap) throw ret

        // else
        return null
    }

    private fun wrapOperate(raw: OperateMethodRaw<*>): OperateMethodRaw<OperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation ->
            lazyStack = lazy { image.stack.toMutableList() }
            val stack by lazyStack
            val ret = raw(env, image, continuation)

            // common returns
            wrapJSReturn(
                ret,
                listOfNotNull<OperatorSideEffect>(consumeMedia()).toMutableList(),
                continuation,
                { stack.add(it) },
            )?.let {
                if (it.newImage === TODO_IMAGE) return@wrapped it.copy(
                    newImage = image.copy(
                        stack = if (lazyStack.isInitialized()) TreeList.from(stack)
                        else image.stack, opsConsumed = image.opsConsumed + 1
                    )
                )
            }

            // spell: SpellAction
            if (ret is SpellAction.Result) {
                // from https://github.com/FallingColors/HexMod/blob/main/Common/src/main/java/at/petrak/hexcasting/api/casting/castables/SpellAction.kt
                val userDataMut = image.userData.copy()

                val sideEffects = mutableListOf<OperatorSideEffect>()

                if (env.extractMedia(ret.cost, true) > 0) throw MishapNotEnoughMedia(ret.cost)
                if (ret.cost > 0) sideEffects.add(OperatorSideEffect.ConsumeMedia(ret.cost))

                sideEffects.add(OperatorSideEffect.AttemptSpell(ret.effect, hasCastingSound, awardsCastingStat))

                for (spray in ret.particles) sideEffects.add(OperatorSideEffect.Particles(spray))

                val image2 = image.copy(opsConsumed = image.opsConsumed + ret.opCount, userData = userDataMut)

                val sound = if (hasCastingSound) HexEvalSounds.SPELL else HexEvalSounds.MUTE
                return@wrapped OperationResult(image2, sideEffects, continuation, sound.get())
            }

            // else
            ret.asUnsupportedKJS
        }
    }

    private fun wrapOperateInParens(raw: OperateParenMethodRaw<*>): OperateParenMethodRaw<ParenthesizedOperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota ->
            lazyParenList = lazy { image.parenthesized.toMutableList() }
            val stack by lazyParenList
            val ret = raw(env, image, continuation, thisIota)

            wrapJSReturn(
                ret,
                mutableListOf(),
                continuation,
                { stack.add(CastingImage.ParenthesizedIota(it, false)) },
            ) { stack.add(it) }?.let {
                return@wrapped ParenthesizedOperationResult(
                    newImage = if (it.newImage === TODO_IMAGE) image.copy(
                        parenthesized = if (lazyParenList.isInitialized()) TreeList.from(
                            stack
                        ) else image.parenthesized
                    ) else it.newImage,
                    sideEffects = it.sideEffects,
                    newContinuation = it.newContinuation,
                    sound = it.sound,
                    resolutionType = ResolvedPatternType.ESCAPED,
                )
            }

            // else
            ret.asUnsupportedKJS
        }
    }

    private fun <T> wrapTry(action: () -> T): T {
        try {
            return action()
        } catch (e: Throwable) {
            var e = e
            if (e is WrappedException) e = e.wrappedException
            if (e is JavaScriptException) {
                val inner: Any? = e.value.unwrapKJS()
                if (inner is Throwable) e = inner
                else throw MishapCustom("$inner")
            }
            if (e is Mishap) throw e
            throw MishapCustom("$e")
        }
    }
    //#endregion

    //#region
    // ConstMediaAction
    var mediaCost: Long = 0
    fun consumeMedia() = if (mediaCost > 0) OperatorSideEffect.ConsumeMedia(mediaCost) else null
    // SpellAction
    var hasCastingSound = true
    var awardsCastingStat = true
    //#endregion

    init {
        opRaw?.let { _operate = wrapOperate(it) }
        opInParensRaw?.let { _operateInParens = wrapOperateInParens(it) }
    }

    //#region KJS extra hooks
    constructor() : this(null, null)
    constructor(opRaw: OperateMethodRaw<Any>) : this(opRaw, null)

    fun setOperate(newFun: OperateMethodRaw<*>): ActionJS {
        _operate = wrapOperate(newFun)
        return this
    }

    fun setOperateInParens(newFun: OperateParenMethodRaw<*>): ActionJS {
        _operateInParens = wrapOperateInParens(newFun)
        return this
    }

    fun setOperateMutableStack(newFun: MutableStackMethod): ActionJS {
        _operate = wrapOperate { env, image, continuation ->
            val stack = lazyStack.value
            val ret = newFun(stack, env, image, continuation)
            // (image as MutableCastingImage).setStack(TreeList.from(stack))
            ret
        }
        return this
    }
    //#endregion
}
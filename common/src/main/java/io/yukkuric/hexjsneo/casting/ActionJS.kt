package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.castables.Action
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
import dev.latvian.mods.kubejs.typings.Info
import dev.latvian.mods.rhino.Undefined
import dev.latvian.mods.rhino.util.HideFromJS
import io.yukkuric.hexjsneo.ext.*
import io.yukkuric.hexjsneo.mixin_interface.LazyCastingImage
import net.minecraft.network.chat.Component

typealias OperateMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation) -> R
typealias OperateParenMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation, Iota) -> R
typealias OperateMethod = OperateMethodRaw<OperationResult>
typealias OperateParenMethod = OperateParenMethodRaw<ParenthesizedOperationResult>
typealias MutableStackMethod = (MutableList<Iota>, CastingEnvironment, CastingImage, SpellContinuation) -> Any
typealias ArgsSplitMethod = (ArgsJS, CastingEnvironment, CastingImage, SpellContinuation) -> Any

@Info("Base for all KubeJS-registered actions")
open class ActionJS(
    opRaw: OperateMethodRaw<*>? = null, opInParensRaw: OperateParenMethodRaw<*>? = null
) : Action, PipeSelf<ActionJS> {
    companion object {
        @Info("default operate: do nothing")
        val DUMMY_OPERATE: OperateMethod =
            { env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation ->
                env.castingEntity?.sendSystemMessage(Component.literal("hello hexjs"))
                OperationResult(image, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE.get())
            }
        @Info("default paren operate: do nothing but adding self pattern")
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
        @HideFromJS
        val TODO_IMAGE = CastingImage()
    }

    var _operate: OperateMethod = DUMMY_OPERATE
    var _operateInParens: OperateParenMethod = DUMMY_OPERATE_PARENS
    override fun operate(
        env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation
    ): OperationResult {
        ArgsJS.InjectContext(env)
        return wrapTryKJS { _operate(env, image, continuation) }
    }

    override fun operateInParens(
        env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota
    ): ParenthesizedOperationResult {
        ArgsJS.InjectContext(env)
        return wrapTryKJS { _operateInParens(env, image, continuation, thisIota) }
    }

    //#region wrappers
    private fun wrapJSReturn(
        ret: Any?,
        sideEffects: MutableList<OperatorSideEffect>,
        continuation: SpellContinuation,
        addIota: (iota: Iota) -> Unit,
        addParened: ((iota: CastingImage.ParenthesizedIota) -> Unit)? = null,
    ): OperationResult {
        var ret = ret
        // env.castingEntity?.sendSystemMessage(Component.literal("$ret is class ${ret?.javaClass?.simpleName}"))

        // null or undefined = empty list
        if (ret is Undefined || ret == null) ret = listOf<Iota>()

        // list: add stack or sideEffect
        if (ret is Array<*>) ret = ret.asIterable()
        if (ret is Iterable<*>) {
            val overrideSound = OverrideHelper<EvalSound>("sound")
            val overrideCont = OverrideHelper<SpellContinuation>("continuation")
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
        ret.asUnsupportedKJS
    }

    private fun wrapOperate(raw: OperateMethodRaw<*>): OperateMethodRaw<OperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation ->
            val lazyStack = (image as LazyCastingImage).getLazyStack(true)
            val stack by lazyStack
            val ret = raw(env, image, continuation)

            // common returns
            wrapJSReturn(
                ret,
                listOfNotNull<OperatorSideEffect>(consumeMedia()).toMutableList(),
                continuation,
                { stack.add(it) },
            ).let {
                if (it.newImage === TODO_IMAGE) return@wrapped it.copy(
                    newImage = image.copy(
                        stack = if (lazyStack.isInitialized()) TreeList.from(stack)
                        else image.stack, opsConsumed = image.opsConsumed + 1
                    )
                )
                return@wrapped it
            }
        }
    }

    private fun wrapOperateInParens(raw: OperateParenMethodRaw<*>): OperateParenMethodRaw<ParenthesizedOperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota ->
            val lazyParenList = (image as LazyCastingImage).getLazyParenList(true)
            val stack by lazyParenList
            val ret = raw(env, image, continuation, thisIota)

            wrapJSReturn(
                ret,
                mutableListOf(),
                continuation,
                { stack.add(CastingImage.ParenthesizedIota(it, false)) },
            ) { stack.add(it) }.let {
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
        }
    }
    //#endregion

    //#region
    // ConstMediaAction
    @get:Info("set >0 to auto-add a ConsumeMedia into default side effect list")
    var mediaCost: Long = 0
    @Info("returns a ConsumeMedia side-effect if mediaCost > 0")
    fun consumeMedia() = if (mediaCost > 0) OperatorSideEffect.ConsumeMedia(mediaCost) else null
    @Info("mishaps if CastEnv can't afford the amount")
    fun preCheckMedia(env: CastingEnvironment, cost: Long) {
        if (env.extractMedia(cost, true) > 0) throw MishapNotEnoughMedia(cost)
    }
    @Info("mishaps if CastEnv can't afford current set mediaCost")
    fun preCheckMedia(env: CastingEnvironment) = preCheckMedia(env, mediaCost)
    //#endregion

    init {
        opRaw?.let { _operate = wrapOperate(it) }
        opInParensRaw?.let { _operateInParens = wrapOperateInParens(it) }
    }

    //#region KJS extra hooks
    constructor() : this(null, null)
    constructor(opRaw: OperateMethodRaw<*>) : this(opRaw, null)

    @Info("KJS-ish operate method setter")
    fun setOperate(newFun: OperateMethodRaw<*>) = also { _operate = wrapOperate(newFun) }

    @Info("KJS-ish paren operate method setter")
    fun setOperateInParens(newFun: OperateParenMethodRaw<*>) = also { _operateInParens = wrapOperateInParens(newFun) }

    @Info("Special KJS-ish operate method setter: accepts a mutable whole stack argument at first of the method")
    fun setOperateMutableStack(newFun: MutableStackMethod) = also {
        _operate = wrapOperate { env, image, continuation ->
            val stack = (image as LazyCastingImage).getLazyStack(false).value
            val ret = newFun(stack, env, image, continuation)
            // (image as MutableCastingImage).setStack(TreeList.from(stack))
            ret
        }
    }

    @Info("Special KJS-ish operate method setter: accepts how many stack elements to be transformed into initial `ArgsJS` object, and inserts it as the first argument, just like a ConstMediaAction or SpellAction")
    fun setOperateArgsSplit(argCount: Int, newFun: ArgsSplitMethod) = also {
        _operate = wrapOperate { env, image, continuation ->
            val stack = (image as LazyCastingImage).getLazyStack(false).value
            newFun(ArgsJS(stack, argCount), env, image, continuation)
        }
    }
    //#endregion
}
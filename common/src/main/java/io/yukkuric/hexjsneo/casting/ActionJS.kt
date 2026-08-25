package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import io.yukkuric.hexjsneo.ext.unwrapKJS
import io.yukkuric.hexjsneo.mixin.MutableCastingImage
import net.minecraft.network.chat.Component

typealias OperateMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation) -> R
typealias OperateParenMethodRaw<R> = (CastingEnvironment, CastingImage, SpellContinuation, Iota) -> R
typealias OperateMethod = OperateMethodRaw<OperationResult>
typealias OperateParenMethod = OperateParenMethodRaw<ParenthesizedOperationResult>
typealias MutableStackMethod = (MutableList<Iota>, CastingEnvironment, CastingImage, SpellContinuation) -> Any

class ActionJS(
    opRaw: OperateMethodRaw<*>? = null,
    opInParensRaw: OperateParenMethodRaw<*>? = null
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
    }

    var _operate: OperateMethod = DUMMY_OPERATE
    var _operateInParens: OperateParenMethod = DUMMY_OPERATE_PARENS
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ) = _operate(env, image, continuation)

    override fun operateInParens(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation,
        thisIota: Iota
    ) = _operateInParens(env, image, continuation, thisIota)

    //#region wrappers
    fun wrapOperate(raw: OperateMethodRaw<*>): OperateMethodRaw<OperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation ->
            val ret = raw(env, image, continuation)?.unwrapKJS()

            // full result: direct return
            if (ret is OperationResult) return@wrapped ret

            // list: add stack
            if (ret is List<*> || ret is Array<*>) {
                val stack = image.stack.toMutableList()
                val handleSub: (Any?) -> Unit = { subRaw: Any? ->
                    val sub = subRaw?.unwrapKJS()
                    if (sub is Iota) stack.add(sub)
                    else throw MishapCustom("Unsupported Iota: $ret")
                }
                if (ret is List<*>) ret.forEach(handleSub)
                if (ret is Array<*>) ret.forEach(handleSub)
                return@wrapped OperationResult(
                    image.copy(stack = TreeList.from(stack), opsConsumed = image.opsConsumed + 1),
                    listOfNotNull(consumeMedia()),
                    continuation,
                    HexEvalSounds.NORMAL_EXECUTE.get(),
                )
            }

            // spell: SpellAction
            if (ret is SpellAction.Result) {
                // from https://github.com/FallingColors/HexMod/blob/main/Common/src/main/java/at/petrak/hexcasting/api/casting/castables/SpellAction.kt
                val userDataMut = image.userData.copy()

                val sideEffects = mutableListOf<OperatorSideEffect>()

                if (env.extractMedia(ret.cost, true) > 0)
                    throw MishapNotEnoughMedia(ret.cost)
                if (ret.cost > 0)
                    sideEffects.add(OperatorSideEffect.ConsumeMedia(ret.cost))

                sideEffects.add(OperatorSideEffect.AttemptSpell(ret.effect, hasCastingSound, awardsCastingStat))

                for (spray in ret.particles)
                    sideEffects.add(OperatorSideEffect.Particles(spray))

                val image2 = image.copy(opsConsumed = image.opsConsumed + ret.opCount, userData = userDataMut)

                val sound = if (hasCastingSound) HexEvalSounds.SPELL else HexEvalSounds.MUTE
                return@wrapped OperationResult(image2, sideEffects, continuation, sound.get())
            }

            // mishap: help throw
            if (ret is Mishap) throw ret

            // else
            throw MishapCustom("Unsupported: $ret")
        }
    }

    fun wrapOperateInParens(raw: OperateParenMethodRaw<*>): OperateParenMethodRaw<ParenthesizedOperationResult> {
        return wrapped@{ env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota ->
            val ret = raw(env, image, continuation, thisIota)?.unwrapKJS()

            // full result: direct return
            if (ret is ParenthesizedOperationResult) return@wrapped ret

            // list: add stack
            if (ret is List<*> || ret is Array<*>) {
                val stack = image.parenthesized.toMutableList()
                val handleSub: (Any?) -> Unit = { subRaw: Any? ->
                    val sub = subRaw?.unwrapKJS()
                    if (sub is Iota) stack.add(CastingImage.ParenthesizedIota(sub, false))
                    else if (sub is CastingImage.ParenthesizedIota) stack.add(sub)
                    else throw MishapCustom("Unsupported Iota: $ret")
                }
                if (ret is List<*>) ret.forEach(handleSub)
                if (ret is Array<*>) ret.forEach(handleSub)
                return@wrapped ParenthesizedOperationResult(
                    image.copy(parenthesized = TreeList.from(stack)),
                    listOf(),
                    continuation,
                    HexEvalSounds.NORMAL_EXECUTE.get(),
                    ResolvedPatternType.ESCAPED,
                )
            }

            // mishap: help throw
            if (ret is Mishap) throw ret

            // else
            throw MishapCustom("Unsupported: $ret")
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
            val stack = image.stack.toMutableList()
            val ret = newFun(stack, env, image, continuation)
            (image as MutableCastingImage).setStack(TreeList.from(stack))
            ret
        }
        return this
    }
    //#endregion
}
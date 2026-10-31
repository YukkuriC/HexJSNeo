package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import com.google.common.collect.Lists
import dev.latvian.mods.kubejs.typings.Info
import io.yukkuric.hexjsneo.ext.*

class OperatorJS(arity: Int, accepts: IotaMultiPredicate) : Operator(arity, accepts), PipeSelf<OperatorJS> {
    private var delegateAction = ActionJS()
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ) = delegateAction.operate(env, image, continuation)

    //#region KJS
    @Info(value = "KJS-ish operate method setter")
    fun setOperate(newFun: OperateMethodRaw<*>) = also { delegateAction.setOperate(newFun) }
    @Info(
        value = "KJS-ish apply method setter, as a combination of OperatorBasic and ActionJS.setOperateArgsSplit;\n" +
                "return values are handled the same as ActionJS; non-iota values are not recommended but allowed"
    )
    fun setApply(newFun: ArgsSplitMethod) = also {
        delegateAction.setOperateArgsSplit(arity, newFun)
    }
    @Info(value = "reverse-bind self to an ArithmeticJS")
    fun bindArithmetic(root: ArithmeticJS, key: Any?) = also {
        root.setOperator(key, this)
    }
    //#endregion

    class Accepts {
        companion object {
            inline fun _wrapListPredicate(crossinline test: (seq: List<Iota>) -> Boolean) =
                IotaMultiPredicate { seq ->
                    val list = when (seq) {
                        is List<*> -> seq as List<Iota>
                        else -> Lists.newArrayList<Iota>(seq)
                    }
                    test(list)
                }

            inline fun _toType(raw: Any?) = when (raw) {
                is IotaType<*> -> raw
                is String -> raw.toHexRegistry(HexIotaTypes.REGISTRY)
                else -> raw.asUnsupportedKJS
            }

            @JvmStatic
            @Info("if the iota of given position is of given type")
            fun ofTypeAt(idx: Int, raw: Any?): IotaMultiPredicate {
                val type by lazy { _toType(raw) }
                return _wrapListPredicate test@{
                    if (it.size <= idx) return@test false
                    return@test type == it[idx].type
                }
            }
            @JvmStatic
            @Info("if the iotas each matches given types")
            fun ofTypes(vararg raw: Any?): IotaMultiPredicate {
                val types by lazy { raw.map(::_toType) }
                return _wrapListPredicate test@{
                    if (it.size < types.size) return@test false
                    for (i in 0 until types.size) if (types[i] != it[i].type) return@test false
                    return@test true
                }
            }
            @JvmStatic
            @Info("if all the iotas are of given type")
            fun allOfType(raw: Any?): IotaMultiPredicate {
                val type by lazy { _toType(raw) }
                return IotaMultiPredicate test@{
                    for (iota in it) if (type != iota.type) return@test false
                    return@test true
                }
            }
            @JvmStatic
            @Info("iotas match all given predicates")
            fun and(vararg predicates: IotaMultiPredicate) = _wrapListPredicate test@{
                // it's highly likely to accept wrapList inside, so do it first
                for (p in predicates) if (!p.test(it)) return@test false
                return@test true
            }
        }
    }
}
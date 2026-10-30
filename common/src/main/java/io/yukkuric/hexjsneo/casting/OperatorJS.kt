package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import dev.latvian.mods.kubejs.typings.Info
import io.yukkuric.hexjsneo.ext.PipeSelf

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
}
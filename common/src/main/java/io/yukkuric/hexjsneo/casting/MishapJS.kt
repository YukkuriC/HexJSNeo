package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch
import at.petrak.hexcasting.api.pigment.FrozenPigment
import dev.latvian.mods.kubejs.typings.Info
import io.yukkuric.hexjsneo.ext.PipeSelf
import io.yukkuric.hexjsneo.mixin.AccessorMishap
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapJS private constructor(val type: Type, var data: Any?) : Mishap() {
    companion object {
        @JvmStatic
        @Info("create a mishap type holding all custom logics")
        fun type() = Type()

        val DUMMY_ACCENT_COLOR: (MishapJS, CastingEnvironment, Context) -> FrozenPigment =
            { _, _, _ -> FrozenPigment.DEFAULT.get() }
        val DUMMY_EXECUTE: (MishapJS, CastingEnvironment, Context, MutableList<Iota>) -> Unit = { _, _, _, _ -> }
        val DUMMY_ERROR_MESSAGE: (MishapJS, CastingEnvironment, Context) -> Component = { _, _, _ -> Component.empty() }
    }

    class Type : PipeSelf<Type> {
        var handleOnCreate: ((mishap: MishapJS) -> Unit)? = null
        var handleAccentColor = DUMMY_ACCENT_COLOR
        var handleErrorMessage = DUMMY_ERROR_MESSAGE
        var handleExecute = DUMMY_EXECUTE

        fun execute(self: MishapJS, env: CastingEnvironment, context: Context, stack: MutableList<Iota>) =
            handleExecute(self, env, context, stack)

        // factory methods
        @Info("create a mishap of this type, without custom data")
        fun create() = create(null)
        @Info("create a mishap of this type, with custom data")
        fun create(data: Any?) = MishapJS(this, data).also { handleOnCreate?.invoke(it) }

        // helpers forwarded
        private val mishapForHelpers by lazy { MishapEvalTooMuch() as AccessorMishap }
        fun dyeColor(color: DyeColor) = mishapForHelpers.callDyeColor(color)
        fun error(stub: String, vararg args: Any) = mishapForHelpers.callError(stub, *args)
        fun actionName(name: Component?) = mishapForHelpers.callActionName(name)
        fun blockAtPos(env: CastingEnvironment, pos: BlockPos) = mishapForHelpers.callBlockAtPos(env, pos)

        //#region KJS piped setters
        fun setOnCreate(handler: (MishapJS) -> Unit) = also { handleOnCreate = handler }

        fun setAccentColor(handler: (MishapJS, CastingEnvironment, Context) -> FrozenPigment) =
            also { handleAccentColor = handler }

        fun setConstAccentColor(value: FrozenPigment) = also { handleAccentColor = { _, _, _ -> value } }

        fun setErrorMessage(handler: (MishapJS, CastingEnvironment, Context) -> Component) =
            also { handleErrorMessage = handler }

        fun setConstErrorMessage(value: Component) = also { handleErrorMessage = { _, _, _ -> value } }

        fun setExecute(handler: (MishapJS, CastingEnvironment, Context, MutableList<Iota>) -> Unit) =
            also { handleExecute = handler }

        fun setNopExecute() = also { handleExecute = DUMMY_EXECUTE }
        //#endregion
    }

    override fun accentColor(env: CastingEnvironment, errorCtx: Context) = type.handleAccentColor(this, env, errorCtx)
    override fun errorMessage(env: CastingEnvironment, errorCtx: Context) = type.handleErrorMessage(this, env, errorCtx)
    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) =
        type.execute(this, env, errorCtx, stack)
}
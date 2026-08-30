package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect.DoMishap
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.google.common.base.Supplier
import dev.latvian.mods.kubejs.typings.Info
import io.yukkuric.hexjsneo.ext.BoxedContent
import io.yukkuric.hexjsneo.ext.BoxedRegistry
import io.yukkuric.hexjsneo.ext.PipeSelf
import io.yukkuric.hexjsneo.ext.hotSwap
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

class IotaJS(val data: CompoundTag, val typeJSRaw: Type) : Iota(typeJSRaw) {
    companion object {
        val HOLDER = HashMap<ResourceLocation, Type>()

        fun register(regFunc: (ResourceLocation, IotaType<*>) -> Any?) {
            for ((key, value) in HOLDER.entries) {
                val box = BoxedIotaType(value)
                value.box = box
                regFunc(key, box)
            }
        }

        class BoxedIotaType(override var inner: Type) : IotaType<IotaJS>(),
            BoxedRegistry<Type, IotaType<IotaJS>, BoxedIotaType> {
            override fun codec() = inner.codec()
            override fun streamCodec() = inner.streamCodec()
            override fun color() = inner.color()
            override fun validate(iota: IotaJS, level: ServerLevel) = inner.validate(iota, level)
            override fun usesListCommas() = inner.usesListCommas()

            override fun equals(other: Any?): Boolean {
                if (other is Type) return this.inner == other
                return super.equals(other)
            }
        }

        // iota dummy
        val DUMMY_TRUTHY = { _: IotaJS -> true }
        val DUMMY_TOLERATE = tolerate@{ self: IotaJS, other: Iota ->
            if (!typesMatch(self, other) || other !is IotaJS) return@tolerate false
            self.data == other.data
        }
        val DUMMY_DISPLAY = { self: IotaJS -> Component.literal("[${self.typeJS.id} ${self.data}]") }
        val DUMMY_HASHCODE = { self: IotaJS -> self.data.hashCode() }
        val DUMMY_SIZE_DEPTH = { _: IotaJS -> 1 }

        // iota type dummy
        val DUMMY_VALIDATE = { _: IotaJS, _: ServerLevel -> true }
        val DUMMY_COLOR = { 0x00ffff }
        val DUMMY_COMMA = { true }
    }

    val typeJS get() = typeJSRaw.box?.inner ?: typeJSRaw

    override fun isTruthy() = typeJS.handlerTruthy(this)
    override fun toleratesOther(other: Iota) = typeJS.handlerTolerate(this, other)
    override fun display() = typeJS.handlerDisplay(this)
    override fun hashCode() = typeJS.handlerHashCode(this)
    override fun size() = typeJS.handlerSize(this)
    override fun depth() = typeJS.handlerDepth(this)

    // executable iota
    override fun execute(vm: CastingVM, world: ServerLevel, continuation: SpellContinuation) =
        if (typeJS.customExecute) typeJS.handleExecute(this, vm, world, continuation)
        else super.execute(vm, world, continuation)

    override fun executeInParens(vm: CastingVM, world: ServerLevel, continuation: SpellContinuation) =
        if (typeJS.customExecuteInParens) typeJS.handleExecuteInParens(this, vm, world, continuation)
        else super.executeInParens(vm, world, continuation)

    override fun executable() = typeJS.customExecute || typeJS.customExecuteInParens

    /*
    override fun subIotas() = TODO()
    */

    class Type(override val id: ResourceLocation) : IotaType<IotaJS>(), BoxedContent, PipeSelf<Type>,
        Supplier<IotaType<out Iota?>?> {
        var box: BoxedIotaType? = null
        override fun get() = box ?: this
        override fun equals(other: Any?): Boolean {
            if (other is BoxedIotaType) return other.inner == this
            return super.equals(other)
        }

        //#region executable iota
        private var delegateAction = ActionJS()
        var customExecute = false
        var customExecuteInParens = false

        internal fun handleExecute(
            self: IotaJS, vm: CastingVM, world: ServerLevel, continuation: SpellContinuation
        ): CastResult {
            return try {
                val resolvedType = ResolvedPatternType.EVALUATED
                val mid = delegateAction.operate(vm.env, vm.image, continuation)
                CastResult(self, mid.newContinuation, mid.newImage, mid.sideEffects, resolvedType, mid.sound)
            } catch (mishap: Mishap) {
                generalExecuteFail(mishap, self, vm, continuation)
            }
        }

        internal fun handleExecuteInParens(
            self: IotaJS, vm: CastingVM, world: ServerLevel, continuation: SpellContinuation
        ): CastResult {
            return try {
                val mid = delegateAction.operateInParens(vm.env, vm.image, continuation, self)
                CastResult(self, mid.newContinuation, mid.newImage, mid.sideEffects, mid.resolutionType, mid.sound)
            } catch (mishap: Mishap) {
                generalExecuteFail(mishap, self, vm, continuation)
            }
        }

        private fun generalExecuteFail(
            mishap: Mishap, self: IotaJS, vm: CastingVM, continuation: SpellContinuation
        ): CastResult {
            // from https://github.com/FallingColors/HexMod/blob/main/Common/src/main/java/at/petrak/hexcasting/api/casting/iota/PatternIota.java
            val wipeParens =
                ((continuation as? SpellContinuation.NotDone)?.frame as? FrameEvaluate)?.isMetacasting ?: false
            return CastResult(
                self,
                continuation,
                if (wipeParens) vm.image.withResetEscape() else null,
                listOf(DoMishap(mishap, Mishap.Context(HexPattern(HexDir.WEST, mutableListOf()), null))),
                mishap.resolutionType(vm.env),
                HexEvalSounds.MISHAP.get()
            )
        }
        //#endregion

        // iota handlers
        var handlerTruthy = DUMMY_TRUTHY
        var handlerTolerate = DUMMY_TOLERATE
        var handlerDisplay: (IotaJS) -> Component = DUMMY_DISPLAY
        var handlerHashCode = DUMMY_HASHCODE
        var handlerSize = DUMMY_SIZE_DEPTH
        var handlerDepth = DUMMY_SIZE_DEPTH
        // type handlers
        var handlerColor = DUMMY_COLOR
        var handlerValidate = DUMMY_VALIDATE
        var handlerListCommas = DUMMY_COMMA

        //#region registry & codec
        init {
            HOLDER[id] = this
            hotSwap(IXplatAbstractions.INSTANCE?.iotaTypeRegistry) {
                box = it as BoxedIotaType
            }
        }

        private val _consumer = { data: CompoundTag -> IotaJS(data, this) }
        private val _codec = CompoundTag.CODEC.xmap(_consumer, IotaJS::data).fieldOf("data")
        private val _streamCodec =
            ByteBufCodecs.COMPOUND_TAG.map(_consumer, IotaJS::data).mapStream({ b: RegistryFriendlyByteBuf -> b })

        override fun codec() = _codec
        override fun streamCodec() = _streamCodec
        //#endregion

        override fun color() = handlerColor()
        override fun validate(iota: IotaJS, level: ServerLevel) = handlerValidate(iota, level)
        override fun usesListCommas() = handlerListCommas()

        //#region KJS interface executable
        @Info("KJS-ish operate method setter")
        fun setOperate(newFun: OperateMethodRaw<*>?) = modify {
            customExecute = newFun != null
            delegateAction.setOperate(newFun ?: ActionJS.DUMMY_OPERATE)
        }

        @Info("KJS-ish paren operate method setter")
        fun setOperateInParens(newFun: OperateParenMethodRaw<*>?) = modify {
            customExecuteInParens = newFun != null
            delegateAction.setOperateInParens(newFun ?: ActionJS.DUMMY_OPERATE_PARENS)
        }
        //#endregion

        //#region KJS interface simple
        fun setTruthy(handler: (IotaJS) -> Boolean) = modify { handlerTruthy = handler }
        fun setTruthy(value: Boolean) = modify { handlerTruthy = { value } }

        fun setTolerate(handler: (IotaJS, Iota) -> Boolean) = modify { handlerTolerate = handler }

        fun setDisplay(handler: (IotaJS) -> Component) = modify { handlerDisplay = handler }
        fun setDisplay(value: Component) = modify { handlerDisplay = { value } }

        fun setHashCode(handler: (IotaJS) -> Int) = modify { handlerHashCode = handler }
        fun setHashCode(value: Int) = modify { handlerHashCode = { value } }

        fun setSize(handler: (IotaJS) -> Int) = modify { handlerSize = handler }
        fun setSize(value: Int) = modify { handlerSize = { value } }

        fun setDepth(handler: (IotaJS) -> Int) = modify { handlerDepth = handler }
        fun setDepth(value: Int) = modify { handlerDepth = { value } }

        fun setColor(handler: () -> Int) = modify { handlerColor = handler }
        fun setColor(value: Int) = modify { handlerColor = { value } }

        fun setValidate(handler: (IotaJS, ServerLevel) -> Boolean) = modify { handlerValidate = handler }
        fun setValidate(value: Boolean) = modify { handlerValidate = { _, _ -> value } }

        fun setListCommas(handler: () -> Boolean) = modify { handlerListCommas = handler }
        fun setListCommas(value: Boolean) = modify { handlerListCommas = { value } }
        //#endregion
    }
}
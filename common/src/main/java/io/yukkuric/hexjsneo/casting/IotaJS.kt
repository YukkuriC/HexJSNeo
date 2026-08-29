package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.google.common.base.Supplier
import io.yukkuric.hexjsneo.ext.PipeSelf
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

class IotaJS(val data: CompoundTag, val typeJS: Type) : Iota(typeJS) {
    companion object {
        val HOLDER = HashMap<ResourceLocation, Type>()

        fun register(regFunc: (ResourceLocation, IotaType<*>) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value)
        }

        // iota dummy
        val DUMMY_TRUTHY = { _: IotaJS -> true }
        val DUMMY_TOLERATE = tolerate@{ self: IotaJS, other: Iota ->
            if (!typesMatch(self, other) || other !is IotaJS) return@tolerate false
            self.data == other.data
        }
        val DUMMY_DISPLAY = { self: IotaJS -> Component.literal("[${self.typeJS.id} ${self.data}]") }
        val DUMMY_HASHCODE = { self: IotaJS -> self.data.hashCode() }
        val DUMMY_SIZE = { _: IotaJS -> 1 }

        // iota type dummy
        val DUMMY_VALIDATE = { _: IotaJS, _: ServerLevel -> true }
        val DUMMY_COLOR = { 0x00ffff }
        val DUMMY_COMMA = { true }
    }

    override fun isTruthy() = typeJS.handlerTruthy(this)
    override fun toleratesOther(other: Iota) = typeJS.handlerTolerate(this, other)
    override fun display() = typeJS.handlerDisplay(this)
    override fun hashCode() = typeJS.handlerHashCode(this)
    override fun size() = typeJS.handlerSize(this)

    /*
    override fun depth() = TODO()
    override fun execute(vm: CastingVM, world: ServerLevel, continuation: SpellContinuation) = TODO()
    override fun executeInParens(vm: CastingVM, world: ServerLevel, continuation: SpellContinuation) = TODO()
    override fun executable() = TODO()
    override fun subIotas() = TODO()
    */

    class Type(val id: ResourceLocation) : IotaType<IotaJS>(), Supplier<IotaType<out Iota?>?>, PipeSelf<Type> {
        override fun get() = this

        // iota handlers
        var handlerTruthy = DUMMY_TRUTHY
        var handlerTolerate = DUMMY_TOLERATE
        var handlerDisplay: (IotaJS) -> Component = DUMMY_DISPLAY
        var handlerHashCode = DUMMY_HASHCODE
        var handlerSize = DUMMY_SIZE
        // type handlers
        var handlerColor = DUMMY_COLOR
        var handlerValidate = DUMMY_VALIDATE
        var handlerListCommas = DUMMY_COMMA

        //#region registry & codec
        init {
            HOLDER[id] = this
            // hot swap
            IXplatAbstractions.INSTANCE?.iotaTypeRegistry?.let { reg ->
                if (reg.containsKey(id)) {
                    (reg[id] as? Type)?.let {
                        it.handlerTruthy = handlerTruthy
                        it.handlerTolerate = handlerTolerate
                        it.handlerDisplay = handlerDisplay
                        it.handlerHashCode = handlerHashCode
                        it.handlerSize = handlerSize
                        it.handlerColor = handlerColor
                        it.handlerValidate = handlerValidate
                        it.handlerListCommas = handlerListCommas
                    }
                }
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

        //#region KJS interface
        fun setTruthy(handler: (IotaJS) -> Boolean) = modify { handlerTruthy = handler }
        fun setTruthy(value: Boolean) = modify { handlerTruthy = { value } }

        fun setTolerate(handler: (IotaJS, Iota) -> Boolean) = modify { handlerTolerate = handler }

        fun setDisplay(handler: (IotaJS) -> Component) = modify { handlerDisplay = handler }
        fun setDisplay(value: Component) = modify { handlerDisplay = { value } }

        fun setHashCode(handler: (IotaJS) -> Int) = modify { handlerHashCode = handler }
        fun setHashCode(value: Int) = modify { handlerHashCode = { value } }

        fun setSize(handler: (IotaJS) -> Int) = modify { handlerSize = handler }
        fun setSize(value: Int) = modify { handlerSize = { value } }

        fun setColor(handler: () -> Int) = modify { handlerColor = handler }
        fun setColor(value: Int) = modify { handlerColor = { value } }

        fun setValidate(handler: (IotaJS, ServerLevel) -> Boolean) = modify { handlerValidate = handler }
        fun setValidate(value: Boolean) = modify { handlerValidate = { _, _ -> value } }

        fun setListCommas(handler: () -> Boolean) = modify { handlerListCommas = handler }
        fun setListCommas(value: Boolean) = modify { handlerListCommas = { value } }
        //#endregion
    }
}
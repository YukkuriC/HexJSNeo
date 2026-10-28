package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexArithmetics
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.latvian.mods.kubejs.typings.Info
import dev.latvian.mods.rhino.util.HideFromJS
import io.yukkuric.hexjsneo.ext.*
import net.minecraft.resources.ResourceLocation

data class ArithmeticJS(override val id: ResourceLocation) : Arithmetic, BoxedContent, PipeSelf<ArithmeticJS> {
    //#region registry
    companion object {
        val HOLDER = HashMap<ResourceLocation, ArithmeticJS>()

        @HideFromJS
        fun register(regFunc: (ResourceLocation, Arithmetic) -> Any?) {
            for ((key, value) in HOLDER.entries) {
                val box = BoxedArithmetic(value)
                value.box = box
                regFunc(key, box)
            }
        }
        @HideFromJS
        class BoxedArithmetic(override var inner: ArithmeticJS) : Arithmetic,
            BoxedRegistry<ArithmeticJS, Arithmetic, BoxedArithmetic> {
            override fun equals(other: Any?): Boolean {
                if (other is ArithmeticJS) return this.inner == other
                return super.equals(other)
            }

            override fun arithName() = inner.arithName()
            override fun opTypes() = inner.opTypes()
            override fun getOperator(pattern: HexPattern) = inner.getOperator(pattern)
        }

        private val fArithEngine by lazy {
            HexArithmetics::class.java.getDeclaredField("ENGINE").also { it.isAccessible = true }
        }
        @JvmStatic
        @Info("invalidate current arithmetic engine to refresh its contents")
        fun resetEngine() {
            fArithEngine.set(null, null)
        }

        @JvmStatic
        @Info("collection of builtin operator patterns")
        val BUILTIN_OPERATIONS = object : HashMap<String, HexPattern>() {
            override fun get(key: String) = super.get(key.lowercase())
        }.also { self ->
            Arithmetic::class.java.declaredFields
                .filter { it.type == HexPattern::class.java }
                .forEach { field ->
                    val name = field.name
                    self.put(name.lowercase(), field.get(null) as HexPattern)
                }
        }
    }

    private var box: BoxedArithmetic? = null

    init {
        HOLDER[id] = this
        hotSwap(IXplatAbstractions.INSTANCE?.arithmeticRegistry) {
            box = it as BoxedArithmetic
        }
    }
    //#endregion

    override fun arithName() = "ArithmeticJS_${hashCode()}"
    override fun opTypes() = opMap.keys
    override fun getOperator(pattern: HexPattern) = opMap[pattern]
        ?: throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.");

    private val opMap = HashMap<HexPattern, Operator>()

    @Info("link given operator to certain pattern")
    fun setOperator(key: Any?, op: Operator) = also {
        val pattern = when (key) {
            is HexPattern -> key
            is String -> BUILTIN_OPERATIONS[key] ?: throw IllegalArgumentException("unknown builtin operator: $key")
            else -> key.asUnsupportedKJS
        }
        opMap[pattern] = op
    }
}
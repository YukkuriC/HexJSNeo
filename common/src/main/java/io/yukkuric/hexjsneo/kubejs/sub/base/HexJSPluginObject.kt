package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.BaseFunction
import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.ScriptableObject
import dev.latvian.mods.rhino.Scriptable
import dev.latvian.mods.rhino.util.DefaultValueTypeHint

open class HexJSPluginObject : ScriptableObject() {
    val fnToString by lazy { ToStringFn(this) }
    var inited = false

    private fun initContext(cx: Context) {
        if (inited) return
        inited = true
        onInit(cx)
    }

    protected open fun onInit(cx: Context) {}

    protected open fun getCustom(cx: Context, name: String, start: Scriptable): Any? = NOT_FOUND

    override fun get(cx: Context, name: String, start: Scriptable): Any? {
        initContext(cx)
        if (name == "toString") return fnToString
        val v = getCustom(cx, name, start)
        return if (v !== NOT_FOUND) v else super.get(cx, name, start)
    }

    override fun getDefaultValue(cx: Context, typeHint: DefaultValueTypeHint?): Any? =
        if (typeHint == null || typeHint == DefaultValueTypeHint.STRING) toString()
        else super.getDefaultValue(cx, typeHint)

    override fun toString() = "[${javaClass.simpleName}]"
    override fun getClassName() = javaClass.simpleName

    class ToStringFn(val obj: Any) : BaseFunction() {
        override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?) =
            obj.toString()

        override fun getDefaultValue(cx: Context, typeHint: DefaultValueTypeHint?): Any? = when (typeHint) {
            null, DefaultValueTypeHint.STRING -> toString()
            DefaultValueTypeHint.BOOLEAN -> true
            DefaultValueTypeHint.NUMBER -> Double.NaN
            else -> this
        }

        override fun toString() = "function toString() { [native code] }"
    }
}
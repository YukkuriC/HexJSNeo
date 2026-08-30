package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.ContextFactory
import dev.latvian.mods.rhino.LambdaFunction
import dev.latvian.mods.rhino.NativeObject
import dev.latvian.mods.rhino.Scriptable
import dev.latvian.mods.rhino.util.DefaultValueTypeHint

open class HexJSPluginObject(f: ContextFactory) : NativeObject(f) {
    private var inited = false
    private var toStringFn: LambdaFunction? = null

    protected open fun onInit(cx: Context) {}

    private fun initContext(cx: Context) {
        if (inited) return
        inited = true
        onInit(cx)
    }

    protected open fun getCustom(cx: Context, name: String, start: Scriptable): Any? = NOT_FOUND

    override fun get(cx: Context, name: String, start: Scriptable): Any? {
        initContext(cx)
        if (name == "toString") {
            return toStringFn ?: LambdaFunction(
                cx, this, "toString", 0
            ) { _, _, _, _ -> this.toString() }.also { toStringFn = it }
        }
        val v = getCustom(cx, name, start)
        return if (v !== NOT_FOUND) v else super.get(cx, name, start)
    }

    override fun getDefaultValue(cx: Context, typeHint: DefaultValueTypeHint?): Any? =
        if (typeHint == null || typeHint == DefaultValueTypeHint.STRING) toString()
        else super.getDefaultValue(cx, typeHint)

    override fun toString() = "[${javaClass.simpleName}]"
}
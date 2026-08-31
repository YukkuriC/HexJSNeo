package io.yukkuric.hexjsneo.kubejs.sub.api

import dev.latvian.mods.rhino.*
import io.yukkuric.hexjsneo.kubejs.sub.base.ClassWalkResult
import io.yukkuric.hexjsneo.kubejs.sub.base.HexJSPluginObject

class APINested(private val src: ClassWalkResult) : HexJSPluginObject() {
    private val cacheSub = HashMap<String, APINested>()

    override fun getCustom(cx: Context, name: String, start: Scriptable): Any? {
        src.subClasses[name]?.let { return NativeJavaClass(cx, start, it) }
        src.subPackages[name]?.let { p -> return cacheSub.computeIfAbsent(name) { APINested(p) } }
        return NOT_FOUND
    }

    override fun toString() = "[HexCasting API (${src.pathPrefix})]"

    fun keys() = sequence {
        yieldAll(src.subPackages.keys.sorted())
        yieldAll(src.subClasses.keys.sorted())
    }.toList()

    override fun get(cx: Context, key: Symbol?, start: Scriptable?): Any? {
        if (key == SymbolKey.ITERATOR) return keys()
        return super.get(cx, key, start)
    }

    override fun getIds(cx: Context?) = sequence {
        yieldAll(src.subPackages.keys.sorted())
        yieldAll(src.subClasses.keys.sorted())
    }.toList().toTypedArray<Any>()
}

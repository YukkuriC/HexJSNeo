package io.yukkuric.hexjsneo.kubejs.sub.api

import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.NativeJavaClass
import dev.latvian.mods.rhino.Scriptable
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
}

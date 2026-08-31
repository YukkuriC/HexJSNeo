package io.yukkuric.hexjsneo.kubejs.sub

import dev.latvian.mods.rhino.Context
import io.yukkuric.hexjsneo.kubejs.sub.api.APIFlat
import io.yukkuric.hexjsneo.kubejs.sub.api.APINested
import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider

// stub for ProbeJS
class HexJS(classes: Iterable<Class<*>>) : SubClassProvider(classes) {
    override fun getClassKey(cls: Class<*>) = cls.simpleName.let {
        sequenceOf(it, it.substring(0, it.length - 2))
    }

    val APIFlat = APIFlat()
    val API = APINested(HexAPICollector.ClassesNested)

    override fun onInit(cx: Context) {
        defineProperty(cx, "API", API, READONLY or PERMANENT)
        defineProperty(cx, "APIFlat", APIFlat, READONLY or PERMANENT)
    }

    override fun toString() = "[HexJS]"
}
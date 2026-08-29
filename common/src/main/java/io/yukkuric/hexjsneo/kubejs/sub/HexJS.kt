package io.yukkuric.hexjsneo.kubejs.sub

import dev.latvian.mods.rhino.ContextFactory
import io.yukkuric.hexjsneo.casting.ActionJS
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider

// stub for ProbeJS
class HexJS(classes: Iterable<Class<*>>, f: ContextFactory) : SubClassProvider(classes, f) {
    override fun getClassKey(cls: Class<*>) = cls.simpleName.let {
        sequenceOf(it, it.substring(0, it.length - 2))
    }
}
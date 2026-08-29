package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.ContextFactory
import dev.latvian.mods.rhino.NativeJavaClass
import dev.latvian.mods.rhino.Scriptable

open class SubClassProvider(classes: Iterable<Class<*>>, f: ContextFactory) : SingletonClassTracker(f) {
    override fun get(cx: Context, name: String, start: Scriptable): Any? {
        CLASS_MAP[name]?.let {
            return NativeJavaClass(cx, start, it)
        }
        return super.get(cx, name, start)
    }

    private val CLASS_MAP = HashMap<String, Class<*>>()
    open fun getClassKey(cls: Class<*>) = sequenceOf(cls.simpleName)
    fun contents() = CLASS_MAP.entries

    init {
        for (cls in classes) {
            for (key in getClassKey(cls)) {
                CLASS_MAP[key] = cls
            }
        }
    }
}
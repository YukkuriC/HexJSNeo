package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.ContextFactory
import dev.latvian.mods.rhino.NativeJavaClass
import dev.latvian.mods.rhino.Scriptable

open class SubClassProvider(
    classes: Iterable<Class<*>>? = null,
    classesNamed: Map<String, Class<*>>? = null,
    f: ContextFactory
) : SingletonClassTracker(f) {
    companion object {
        val LOADED_CLASSES = HashSet<Class<*>>()
    }


    override fun getCustom(cx: Context, name: String, start: Scriptable) = CLASS_MAP[name]?.let {
        NativeJavaClass(cx, start, it)
    } ?: NOT_FOUND

    private val CLASS_MAP = HashMap<String, Class<*>>()
    open fun getClassKey(cls: Class<*>) = sequenceOf(cls.simpleName)
    open fun getClassKey(cls: Class<*>, name: String) = sequenceOf(name)
    fun contents() = CLASS_MAP.entries.sortedBy { it.key }

    init {
        classes?.let {
            for (cls in it) {
                LOADED_CLASSES.add(cls)
                for (key in getClassKey(cls)) {
                    CLASS_MAP[key] = cls
                }
            }
        }
        classesNamed?.let {
            for ((name, cls) in it) {
                LOADED_CLASSES.add(cls)
                for (key in getClassKey(cls, name)) {
                    CLASS_MAP[key] = cls
                }
            }
        }
    }
}
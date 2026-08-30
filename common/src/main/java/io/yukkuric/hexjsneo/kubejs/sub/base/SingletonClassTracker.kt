package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.ContextFactory

open class SingletonClassTracker(f: ContextFactory) : HexJSPluginObject(f) {
    companion object {
        val HOLDER = HashMap<Class<*>, SingletonClassTracker>()

        @JvmStatic
        fun from(cls: Class<*>) = HOLDER[cls]
    }

    init {
        HOLDER[javaClass] = this
    }
}
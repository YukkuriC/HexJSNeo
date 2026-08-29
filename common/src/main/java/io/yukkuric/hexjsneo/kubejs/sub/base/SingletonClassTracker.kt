package io.yukkuric.hexjsneo.kubejs.sub.base

import dev.latvian.mods.rhino.ContextFactory
import dev.latvian.mods.rhino.NativeObject

open class SingletonClassTracker(f: ContextFactory) : NativeObject(f) {
    companion object {
        val HOLDER = HashMap<Class<*>, SingletonClassTracker>()

        @JvmStatic
        fun from(cls: Class<*>) = HOLDER[cls]
    }

    init {
        HOLDER[javaClass] = this
    }
}
package io.yukkuric.hexjsneo.kubejs.sub.base

open class SingletonClassTracker : HexJSPluginObject() {
    companion object {
        val HOLDER = HashMap<Class<*>, SingletonClassTracker>()

        @JvmStatic
        fun from(cls: Class<*>) = HOLDER[cls]
    }

    init {
        HOLDER[javaClass] = this
    }
}
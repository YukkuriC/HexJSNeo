package io.yukkuric.hexjsneo.ext

import io.yukkuric.hexjsneo.casting.MishapCustom

class OverrideHelper<T>(val type: String) {
    private var cur: T? = null

    fun update(newVal: T) {
        if (cur != null) throw MishapCustom("Only one override $type allowed")
        cur = newVal
    }

    fun get(default: T) = cur ?: default
    fun get(default: () -> T) = cur ?: default()
}
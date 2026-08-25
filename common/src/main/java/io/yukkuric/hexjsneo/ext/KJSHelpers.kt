package io.yukkuric.hexjsneo.ext

import dev.latvian.mods.rhino.NativeJavaObject

fun Any.unwrapKJS(): Any {
    if (this is NativeJavaObject) return this.unwrap()
    return this
}
package io.yukkuric.hexjsneo.ext

import dev.latvian.mods.rhino.Wrapper

fun Any?.unwrapKJS(): Any? {
    if (this is Wrapper) return this.unwrap()
    return this
}
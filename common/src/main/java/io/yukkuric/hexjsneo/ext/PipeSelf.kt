package io.yukkuric.hexjsneo.ext

import dev.latvian.mods.kubejs.typings.Info

interface PipeSelf<SELF : PipeSelf<SELF>> {
    @Suppress("UNCHECKED_CAST")
    @Info("Calling the function using self, and return self")
    fun modify(action: (self: SELF) -> Unit) = also {
        action(this as SELF)
    } as SELF
}
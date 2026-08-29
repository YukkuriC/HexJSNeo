package io.yukkuric.hexjsneo.ext

interface PipeSelf<SELF : PipeSelf<SELF>> {
    @Suppress("UNCHECKED_CAST")
    fun modify(action: (self: SELF) -> Unit) = (this as SELF).let { self ->
        action(self)
        self
    }
}
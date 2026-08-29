package io.yukkuric.hexjsneo.ext

interface PipeSelf<SELF : PipeSelf<SELF>> {
    fun modify(action: (self: PipeSelf<SELF>) -> Unit): PipeSelf<SELF> {
        action(this)
        return this
    }
}
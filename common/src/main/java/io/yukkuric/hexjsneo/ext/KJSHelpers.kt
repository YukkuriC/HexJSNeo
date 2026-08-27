package io.yukkuric.hexjsneo.ext

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import dev.latvian.mods.rhino.Undefined
import dev.latvian.mods.rhino.Wrapper
import io.yukkuric.hexjsneo.casting.MishapCustom
import java.util.IdentityHashMap

fun Any?.unwrapKJS(): Any? {
    if (this is Wrapper) return this.unwrap()
    return this
}

val Any?.asUnsupportedKJS: Mishap
    get() = MishapCustom("Unsupported element: ${this?.javaClass?.simpleName} $this")

fun Iterable<*>.toListIotaKJS(visited: IdentityHashMap<Any?, Any?> = IdentityHashMap()): ListIota = ListIota(map {
    val unwrap = it.unwrapKJS()
    if (visited.containsKey(unwrap)) throw MishapCustom("loop ref detected: $unwrap")
    visited[unwrap] = unwrap
    when (unwrap) {
        is Iota -> unwrap
        null, is Undefined -> NullIota()
        is Iterable<*> -> unwrap.toListIotaKJS(visited)
        else -> throw unwrap.asUnsupportedKJS
    }
})
package io.yukkuric.hexjsneo.ext

import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.*
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import dev.latvian.mods.rhino.JavaScriptException
import dev.latvian.mods.rhino.Undefined
import dev.latvian.mods.rhino.WrappedException
import dev.latvian.mods.rhino.Wrapper
import io.yukkuric.hexjsneo.casting.MishapCustom
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.joml.Vector3f
import java.util.*

fun Any?.unwrapKJS(): Any? {
    if (this is Wrapper) return this.unwrap()
    return this
}

inline fun <T> wrapTryKJS(action: () -> T): T {
    try {
        return action()
    } catch (e: Throwable) {
        var e = e
        if (e is WrappedException) e = e.wrappedException
        if (e is JavaScriptException) {
            val inner: Any? = e.value.unwrapKJS()
            if (inner is Throwable) e = inner
            else throw MishapCustom("$inner")
        }
        if (e is Mishap) throw e
        throw MishapCustom("$e")
    }
}

val Any?.asUnsupportedKJS: Nothing
    get() = throw MishapCustom("Unsupported element: ${this?.javaClass?.simpleName} $this")

fun Any?.toIotaKJSStrict(visited: IdentityHashMap<Any?, Any?>? = null) =
    toIotaKJS(visited) ?: asUnsupportedKJS

fun Any?.toIotaKJS(visited: IdentityHashMap<Any?, Any?>? = null): Iota? = when (this) {
    is Iota -> this

    // consts
    null, is Undefined -> NullIota()
    is Number -> DoubleIota(toDouble())
    is Boolean -> BooleanIota(this)

    // objects
    is Entity -> EntityIota(this)
    is HexPattern -> PatternIota(this)
    is SpellContinuation -> ContinuationIota(this)

    // vec3
    is Vec3 -> Vec3Iota(this)
    is BlockPos -> Vec3Iota(center)
    is Vector3d -> Vec3Iota(Vec3(x, y, z))
    is Vector3f -> Vec3Iota(Vec3(x.toDouble(), y.toDouble(), z.toDouble()))

    // list
    is Array<*> -> asIterable().toIotaKJS(visited)
    is Iterable<*> -> {
        val map = visited ?: IdentityHashMap()
        if (map.containsKey(this)) throw MishapCustom("loop ref detected: $this")
        map[this] = this
        val ret = ListIota(map { it.toIotaKJSStrict(map) })
        map.remove(this)
        ret
    }

    else -> null
}
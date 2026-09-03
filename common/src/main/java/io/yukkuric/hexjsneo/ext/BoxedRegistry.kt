package io.yukkuric.hexjsneo.ext

import io.yukkuric.hexjsneo.HexJSNeo
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

interface BoxedRegistry<ME, BASE, BOX> where BOX : BASE {
    var inner: ME
}

interface BoxedContent {
    val id: ResourceLocation
}

inline fun <reified BOX : BoxedRegistry<BoxedContent, BASE, *>, BASE> BoxedContent.hotSwap(
    registry: Registry<BASE>?,
    noinline onSwap: ((box: BOX) -> Unit)? = null
) {
    registry ?: return
    if (!registry.containsKey(id)) return
    registry[id]?.let {
        if (it is BOX) {
            it.inner = this
            onSwap?.invoke(it)
        } else HexJSNeo.LOGGER.warn("Non-KJS ${it.javaClass.simpleName} overrides not supported: $id")
    }
}
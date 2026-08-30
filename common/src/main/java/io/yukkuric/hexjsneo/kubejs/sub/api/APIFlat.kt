package io.yukkuric.hexjsneo.kubejs.sub.api

import dev.latvian.mods.rhino.ContextFactory
import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider

class APIFlat(f: ContextFactory) : SubClassProvider(null, HexAPICollector.ClassesFlat, f) {
    override fun toString() = "[HexCasting API (Flat)]"
}
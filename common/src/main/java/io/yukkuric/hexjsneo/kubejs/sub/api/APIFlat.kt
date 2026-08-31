package io.yukkuric.hexjsneo.kubejs.sub.api

import io.yukkuric.hexjsneo.kubejs.sub.base.HexAPICollector
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider

class APIFlat : SubClassProvider(null, HexAPICollector.ClassesFlat) {
    override fun toString() = "[HexCasting API (Flat)]"
}
package io.yukkuric.hexjsneo.forge

import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.forge.events.HJSForgeEventsListener
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(HexJSNeo.MOD_ID)
class HexJSNeoForge(modContainer: ModContainer) {
    init {
        HJSForgeEventsListener.load(modContainer)
    }
}

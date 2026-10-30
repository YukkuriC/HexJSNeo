package io.yukkuric.hexjsneo.forge

import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.forge.events.HJSForgeEventsListener
import net.minecraftforge.fml.common.Mod

@Mod(HexJSNeo.MOD_ID)
class HexJSNeoForge() {
    init {
        HJSForgeEventsListener.load()
    }
}

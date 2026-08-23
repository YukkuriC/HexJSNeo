package io.yukkuric.hexjsneo.forge

import io.yukkuric.hexjsneo.HexJSNeo
import io.yukkuric.hexjsneo.HexJSNeoClient
import io.yukkuric.hexjsneo.forge.events.HJSForgeEventsListener
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent

@Mod(HexJSNeo.MOD_ID)
class HexJSNeoForge(modContainer: ModContainer) : HexJSNeo.IAPI() {
    init {
        HJSForgeEventsListener.load(modContainer)
    }

    override fun modLoaded(id: String) = ModList.get().isLoaded(id)
}

object HexJSNeoForgeClient {
    @SubscribeEvent
    fun OnClientInit(e: FMLClientSetupEvent) {
        HexJSNeoClient.load()
    }
}
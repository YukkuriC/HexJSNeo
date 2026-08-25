package io.yukkuric.hexjsneo.fabric

import at.petrak.hexcasting.common.lib.hex.HexActions
import io.yukkuric.hexjsneo.HexJSNeo.IAPI
import io.yukkuric.hexjsneo.HexJSNeo.commonInit
import io.yukkuric.hexjsneo.HexJSNeo.commonLateInit
import io.yukkuric.hexjsneo.HexJSNeoClient
import io.yukkuric.hexjsneo.casting.ActionRegistryJS
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexJSNeoFabric : IAPI(), ModInitializer {
    private fun <T : Any> bindReg(reg: Registry<T>, loader: ((ResourceLocation, T) -> Any?) -> Any?) {
        loader { k, v -> Registry.register(reg, k, v) }
    }

    override fun onInitialize() {
        bindReg(HexActions.REGISTRY, ActionRegistryJS::register)

        commonInit()
        var lateInitOnce = false
        ServerLifecycleEvents.SERVER_STARTING.register {
            if (lateInitOnce) return@register
            lateInitOnce = true
            commonLateInit()
        }
    }

    override fun modLoaded(id: String) = FabricLoader.getInstance().isModLoaded(id)

    companion object {
        init {
        }
    }
}

class HexJSNeoFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexJSNeoClient.load()
    }
}
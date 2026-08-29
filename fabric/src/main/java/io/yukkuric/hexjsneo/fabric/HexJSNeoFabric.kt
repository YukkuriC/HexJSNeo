package io.yukkuric.hexjsneo.fabric

import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.yukkuric.hexjsneo.HexJSNeo.IAPI
import io.yukkuric.hexjsneo.HexJSNeo.commonInit
import io.yukkuric.hexjsneo.HexJSNeo.commonLateInit
import io.yukkuric.hexjsneo.HexJSNeoClient
import io.yukkuric.hexjsneo.casting.ActionRegistryJS
import io.yukkuric.hexjsneo.casting.IotaJS
import io.yukkuric.hexjsneo.casting.SpecialHandlerJS
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
        IXplatAbstractions.INSTANCE?.let {
            bindReg(it.actionRegistry, ActionRegistryJS::register)
            bindReg(it.specialHandlerRegistry, SpecialHandlerJS::register)
            bindReg(it.iotaTypeRegistry, IotaJS::register)
        }

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
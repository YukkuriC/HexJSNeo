package io.yukkuric.hexjsneo.fabric

import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.yukkuric.hexjsneo.casting.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

class HexJSNeoFabric : ModInitializer {
    private fun <T : Any> bindReg(reg: Registry<T>, loader: ((ResourceLocation, T) -> Any?) -> Any?) {
        loader { k, v -> Registry.register(reg, k, v) }
    }

    override fun onInitialize() {
        IXplatAbstractions.INSTANCE?.let {
            bindReg(it.actionRegistry, ActionRegistryJS::register)
            bindReg(it.specialHandlerRegistry, SpecialHandlerJS::register)
            bindReg(it.iotaTypeRegistry, IotaJS::register)
            bindReg(it.arithmeticRegistry, ArithmeticJS::register)
        }
    }

    companion object {
        init {
        }
    }
}

class HexJSNeoFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
    }
}
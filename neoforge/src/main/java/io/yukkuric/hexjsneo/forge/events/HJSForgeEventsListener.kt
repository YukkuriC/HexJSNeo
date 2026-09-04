package io.yukkuric.hexjsneo.forge.events

import at.petrak.hexcasting.common.lib.HexRegistries
import io.yukkuric.hexjsneo.casting.ActionRegistryJS
import io.yukkuric.hexjsneo.casting.IotaJS
import io.yukkuric.hexjsneo.casting.SpecialHandlerJS
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.registries.RegisterEvent


class HJSForgeEventsListener {
    // private object ForgeBus {
    // }

    private object ModBus {
        @SubscribeEvent
        fun OnRegisterAll(e: RegisterEvent) {
            fun <T : Any> bindReg(
                key: ResourceKey<Registry<T>>, regFunc: ((ResourceLocation, T) -> Any?) -> Any?
            ) {
                if (e.registryKey != key) return
                regFunc { id, obj -> e.register(key, id) { obj } }
            }
            bindReg(HexRegistries.ACTION, ActionRegistryJS::register)
            bindReg(HexRegistries.SPECIAL_HANDLER, SpecialHandlerJS::register)
            bindReg(HexRegistries.IOTA_TYPE, IotaJS::register)
        }
    }

    companion object {
        fun load(modContainer: ModContainer) {
            // NeoForge.EVENT_BUS.register(ForgeBus)
            val modBus = modContainer.eventBus!!
            modBus.register(ModBus)
        }
    }
}
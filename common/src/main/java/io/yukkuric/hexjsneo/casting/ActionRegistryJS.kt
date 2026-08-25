package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.yukkuric.hexjsneo.mixin.MutableActionRegistryEntry
import net.minecraft.resources.ResourceLocation

data class ActionRegistryJS(
    val prototype: HexPattern,
    val id: ResourceLocation,
    val action: ActionJS,
    val isGreat: Boolean
) {
    companion object {
        val HOLDER = HashMap<ResourceLocation, ActionRegistryJS>()
        val MAP_NORMAL = HashMap<String, ActionRegistryJS>()
        val MAP_GREAT = HashMap<HexPattern, ActionRegistryJS>()

        fun register(regFunc: (ResourceLocation, ActionRegistryEntry) -> Any?) {
            for (pair in HOLDER.entries) regFunc(pair.key, pair.value.asEntry)
        }
    }

    init {
        HOLDER[id] = this
        if (isGreat) MAP_GREAT[prototype] = this
        else MAP_NORMAL[prototype.anglesSignature()] = this

        IXplatAbstractions.INSTANCE?.actionRegistry?.let { reg ->
            // replace existing registry for hot swap
            if (reg.containsKey(id)) {
                reg[id].let {
                    (it as MutableActionRegistryEntry).let { entry ->
                        entry.setAction(action)
                        entry.setPrototype(prototype)
                    }
                }
            }
        }
    }

    val asEntry by lazy { ActionRegistryEntry(prototype, action) }
}
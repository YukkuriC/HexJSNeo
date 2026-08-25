package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

data class MishapCustom(val text: Component) : Mishap() {
    constructor(text: String) : this(Component.literal(text))

    override fun accentColor(env: CastingEnvironment, errorCtx: Context) = dyeColor(DyeColor.PURPLE)
    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>) = TreeList.empty<Iota>()
    override fun errorMessage(env: CastingEnvironment, errorCtx: Context) = text
}
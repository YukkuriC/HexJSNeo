package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mishap.class)
public interface AccessorMishap {
    @Invoker
    FrozenPigment callDyeColor(DyeColor color);
    @Invoker
    Component callError(String stub, Object... args);
    @Invoker
    Component callActionName(Component name);
    @Invoker
    Component callBlockAtPos(CastingEnvironment env, BlockPos pos);
}

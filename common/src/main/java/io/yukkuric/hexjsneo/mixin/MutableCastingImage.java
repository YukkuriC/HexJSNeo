package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CastingImage.class)
public interface MutableCastingImage {
    @Accessor("stack")
    @Mutable
    void setStack(List<Iota> newStack);
    @Accessor("opsConsumed")
    @Mutable
    void setOpsConsumed(long newCount);
}

package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CastingImage.class)
public interface MutableCastingImage {
    @Accessor("stack")
    void setStack(TreeList<Iota> newStack);
}

package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import io.yukkuric.hexjsneo.mixin_interface.MutableCastingImage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CastingImage.class)
public class MutableCastingImageImpl implements MutableCastingImage {
    @Final
    @Shadow
    @Mutable
    private TreeList<Iota> stack;

    public void setStack(TreeList<Iota> newStack) {
        stack = newStack;
    }
}

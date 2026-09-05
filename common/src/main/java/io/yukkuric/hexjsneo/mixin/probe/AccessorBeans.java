package io.yukkuric.hexjsneo.mixin.probe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "moe.wolfgirl.probejs.plugin.builtins.InjectBeans$Bean")
public interface AccessorBeans {
    @Accessor
    String getName();
}

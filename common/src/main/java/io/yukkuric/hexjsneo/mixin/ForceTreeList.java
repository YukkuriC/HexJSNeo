package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.utils.TreeList;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.type.TypeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Context.class)
public class ForceTreeList {
    @Inject(method = "jsToJava", at = @At("HEAD"), cancellable = true, remap = false)
    void TreeListFirst(Object from, TypeInfo target, CallbackInfoReturnable<Object> cir) {
        if (TreeList.class.isAssignableFrom(target.asClass())) {
            if (from instanceof Wrapper w) from = w.unwrap();
            cir.setReturnValue(from);
        }
    }
}

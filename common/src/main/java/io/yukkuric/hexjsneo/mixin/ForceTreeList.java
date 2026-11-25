package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.utils.TreeList;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.type.TypeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(Context.class)
public class ForceTreeList {
    @Inject(method = "jsToJava", at = @At("HEAD"), cancellable = true, remap = false)
    void TreeListFirst(Object from, TypeInfo target, CallbackInfoReturnable<Object> cir) {
        // prepare
        if (!TreeList.class.isAssignableFrom(target.asClass())) return;
        while (from instanceof Wrapper w) from = w.unwrap();

        // direct treelist
        if (from instanceof TreeList<?>) {
            cir.setReturnValue(from);
            return;
        }

        // array to list first
        if (from instanceof Object[] array) from = Arrays.asList(array);
        // iterables to treelist
        if (from instanceof Iterable<?> list) {
            from = TreeList.from(list);
        }

        // all (maybe) converted
        cir.setReturnValue(from);
    }
}

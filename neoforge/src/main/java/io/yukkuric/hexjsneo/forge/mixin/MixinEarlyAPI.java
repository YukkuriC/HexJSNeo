package io.yukkuric.hexjsneo.forge.mixin;

import io.yukkuric.hexjsneo.HexJSEarlyAPI;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(HexJSEarlyAPI.class)
public class MixinEarlyAPI {
    @Inject(method = "modFilePath", at = @At("HEAD"), cancellable = true)
    private static void hookModFilePath(String id, CallbackInfoReturnable<Path> cir) {
        var file = ModList.get().getModFileById(id);
        if (file == null) return;
        cir.setReturnValue(file.getFile().getFilePath());
    }
}

package io.yukkuric.hexjsneo.fabric.mixin;

import io.yukkuric.hexjsneo.HexJSEarlyAPI;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(HexJSEarlyAPI.class)
public class MixinEarlyAPI {
    @Inject(method = "modFilePath", at = @At("HEAD"), cancellable = true)
    private static void hookModFilePath(String id, CallbackInfoReturnable<Path> cir) {
        var container = FabricLoader.getInstance().getModContainer(id).orElse(null);
        if (container == null) return;
        var paths = container.getOrigin().getPaths();
        cir.setReturnValue(paths.isEmpty() ? null : paths.get(0));
    }
}

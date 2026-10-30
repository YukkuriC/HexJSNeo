package io.yukkuric.hexjsneo.mixin;

import io.yukkuric.hexjsneo.kubejs.probe.PJSPluginHJS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.SharedDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * ProbeJS-Legacy only calls modifyFiles for ScriptDump, so the shared dump
 * keeps raw untransformed classes. Redirect the shared dump's transpile step
 * to apply HexJS's modifyFiles (dynamic API bindings, synthetic nested docs)
 * to the shared dump as well.
 */
@Mixin(SharedDump.class)
public class MixinSharedDump {
    @Shadow
    @Final
    public Set<ClassPath> denied;

    @Redirect(
        method = "open()V",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lzzzank/probejs/lang/transpiler/Transpiler;dump(Ljava/util/Collection;)Ljava/util/Map;",
            remap = false
        )
    )
    private Map<ClassPath, TypeScriptFile> hexjsneo$dumpShared(Transpiler transpiler, Collection<Clazz> classes) {
        Map<ClassPath, TypeScriptFile> files = transpiler.dump(classes);
        RequestAwareFiles requestAwareFiles = new RequestAwareFiles(files, null);
        for (ProbeJSPlugin plugin : ProbeJSPlugins.getAll()) {
            if (plugin instanceof PJSPluginHJS) {
                plugin.modifyFiles(requestAwareFiles);
            }
        }
        // script dumps request the synthetic nested docs first and deny them from the
        // shared dump; re-allow ours so the shared dump stays self-contained
        denied.removeAll(requestAwareFiles.requested());
        return files;
    }
}

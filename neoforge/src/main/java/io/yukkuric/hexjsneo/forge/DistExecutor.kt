package io.yukkuric.hexjsneo.forge

import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import java.util.function.Supplier


object DistExecutor {
    fun unsafeRunWhenOn(dist: Dist, action: Supplier<Runnable>) {
        if (FMLEnvironment.dist == dist) action.get().run()
    }
}
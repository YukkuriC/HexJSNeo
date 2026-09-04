package io.yukkuric.hexjsneo

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

object HexJSNeo {
    const val MOD_ID: String = "hexjsneo"
    val LOGGER: Logger = LogUtils.getLogger()
    @JvmStatic
    fun modLoc(path: String): ResourceLocation {
        return ResourceLocation.tryBuild(MOD_ID, path)!!
    }
}
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

    fun commonInit() {
    }

    fun commonLateInit() {
    }

    fun tryLoadInterop(modId: String, loadFunc: () -> Any) {
        if (!API.modLoaded(modId)) return
        try {
            loadFunc()
        } catch (e: Throwable) {
            LOGGER.error("error trying to load interop of $modId; error: ${e.stackTraceToString()}")
        }
    }

    lateinit var API: IAPI

    abstract class IAPI {
        init {
            API = this
        }

        abstract fun modLoaded(id: String): Boolean
    }
}

// client stuff
object HexJSNeoClient {
    fun load() {
    }
}
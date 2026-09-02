package io.yukkuric.hexjsneo.kubejs.sub.base

import at.petrak.hexcasting.api.HexAPI
import io.yukkuric.hexjsneo.HexJSEarlyAPI
import io.yukkuric.hexjsneo.HexJSNeo
import java.util.zip.ZipFile

object HexAPICollector {
    @JvmStatic
    val ClassesFlat = HashMap<String, Class<*>>()
    val ClassesNested = ClassWalkResult("at.petrak.hexcasting")
    var inited = false

    fun init() {
        if (inited) return
        inited = true

        try {
            val modFile = (HexJSEarlyAPI.modFilePath(HexAPI.MOD_ID) ?: return).toFile()
            ZipFile(modFile).use { jar ->
                for (entry in jar.entries()) {
                    if (entry.isDirectory) continue
                    entry.name.let {
                        if (it.endsWith(".class")) {
                            val name = it.substring(0, it.length - 6).replace("/", ".").replace("\\", ".")
                            ClassesNested.tryAdd(name)?.let { (name, cls) -> ClassesFlat[name] = cls }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            HexJSNeo.LOGGER.error("${e.javaClass.simpleName} ${e.message}\n${e.stackTraceToString()}")
        }
    }
}

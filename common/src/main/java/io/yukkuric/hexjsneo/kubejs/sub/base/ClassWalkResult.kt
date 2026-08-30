package io.yukkuric.hexjsneo.kubejs.sub.base

import io.yukkuric.hexjsneo.HexJSNeo

class ClassWalkResult(val pathPrefix: String) {
    val subPackages = HashMap<String, ClassWalkResult>()
    val subClasses = HashMap<String, Class<*>>()
    val loader by lazy { Thread.currentThread().getContextClassLoader() }

    fun tryAdd(path: String): Pair<String, Class<*>>? {
        if (!path.startsWith(pathPrefix)) return null
        try {
            val cls = Class.forName(path, false, loader)
            if (cls.isAnonymousClass) return null
            path.lastIndexOf('.').let { idx ->
                val packagePath = path.substring(0, idx).removePrefix(pathPrefix).removePrefix(".")
                val clsName = path.substring(idx + 1)
                add(cls, packagePath, clsName)
                return Pair(clsName, cls)
            }
        } catch (e: Throwable) {
            HexJSNeo.LOGGER.warn("Adding class $path failed: ${e.message}")
        }
        return null
    }

    fun add(cls: Class<*>, packagePath: String, clsName: String) {
        if (packagePath.isEmpty()) {
            subClasses[clsName] = cls
            return
        }
        val pathSplit = packagePath.split(".", limit = 2)
        val sub = subPackages.computeIfAbsent(pathSplit[0], ::ClassWalkResult)
        return sub.add(cls, if (pathSplit.size > 1) pathSplit[1] else "", clsName)
    }
}

package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

open class CastingEnvironmentComponentJS(initKey: String) : CastingEnvironmentComponent {
    private var _key: CastingEnvironmentComponent.Key<*> = Key.of(initKey)
    override fun getKey() = _key
    fun setKey(key: String): Any {
        _key = Key.of(key)
        return this
    }

    class Key private constructor(val key: String) : CastingEnvironmentComponent.Key<CastingEnvironmentComponentJS> {
        companion object {
            val CACHE = HashMap<String, Key>()
            @JvmStatic
            fun of(key: String) = CACHE.computeIfAbsent(key, ::Key)
        }
    }

    open class ExtractMedia(initKey: String, var funExtractMedia: (target: Long, simulate: Boolean) -> Long) :
        CastingEnvironmentComponentJS(initKey), CastingEnvironmentComponent.ExtractMedia {
        override fun onExtractMedia(target: Long, simulate: Boolean) = funExtractMedia(target, simulate)

        class Pre(initKey: String, funExtractMedia: (Long, Boolean) -> Long) : ExtractMedia(initKey, funExtractMedia),
            CastingEnvironmentComponent.ExtractMedia.Pre

        class Post(initKey: String, funExtractMedia: (Long, Boolean) -> Long) : ExtractMedia(initKey, funExtractMedia),
            CastingEnvironmentComponent.ExtractMedia.Post
    }

    class PostCast(initKey: String, var funPostCast: (image: CastingImage) -> Any?) :
        CastingEnvironmentComponentJS(initKey), CastingEnvironmentComponent.PostCast {
        override fun onPostCast(image: CastingImage) {
            funPostCast(image)
        }
    }

    class PostExecution(initKey: String, var funPostExecution: (result: CastResult) -> Any?) :
        CastingEnvironmentComponentJS(initKey), CastingEnvironmentComponent.PostExecution {
        override fun onPostExecution(result: CastResult) {
            funPostExecution(result)
        }
    }

    class IsVecInRange(initKey: String, var funVecInRange: (pos: Vec3, oldResult: Boolean) -> Boolean) :
        CastingEnvironmentComponentJS(initKey), CastingEnvironmentComponent.IsVecInRange {
        override fun onIsVecInRange(pos: Vec3, oldResult: Boolean) = funVecInRange(pos, oldResult)
    }

    class HasEditPermissionsAt(
        initKey: String,
        var funEditPermissionsAt: (pos: BlockPos, oldResult: Boolean) -> Boolean
    ) :
        CastingEnvironmentComponentJS(initKey),
        CastingEnvironmentComponent.HasEditPermissionsAt {
        override fun onHasEditPermissionsAt(pos: BlockPos, oldResult: Boolean) = funEditPermissionsAt(pos, oldResult)
    }
}
// 生成于 GLM-5.3
package io.yukkuric.hexjsneo.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.AbstractVillager
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.raid.Raider
import net.minecraft.world.phys.Vec3

/**
 * Helpers for handling stack contents
 */
class ArgsJS(stack: MutableList<Iota>, n: Int, keep: Boolean = false) {
    constructor(stack: MutableList<Iota>, n: Int) : this(stack, n, false)

    companion object {
        lateinit var CTX_LEVEL: ServerLevel

        fun InjectContext(ctx: CastingEnvironment) {
            CTX_LEVEL = ctx.world
        }
    }

    val world: ServerLevel = CTX_LEVEL

    val data: List<Iota>

    init {
        if (stack.size < n) throw MishapNotEnoughArgs(n, stack.size)
        // no more slice/splice
        val taken = ArrayList<Iota>(n)
        for (i in 0 until n) {
            taken.add(if (keep) stack[stack.size - 1 - i] else stack.removeAt(stack.size - 1))
        }
        taken.reverse()
        data = taken
    }

    operator fun get(i: Int): Iota = data[i]

    fun double(i: Int) = data.getDouble(i, data.size)
    fun list(i: Int) = data.getList(i, data.size)
    fun pattern(i: Int) = data.getPattern(i, data.size)
    fun vec3(i: Int) = data.getVec3(i, data.size)
    fun bool(i: Int) = data.getBool(i, data.size)

    fun entity(i: Int): Entity = data.getEntity(world, i, data.size)

    // fun brainmerge_target(i: Int): Entity {
    //     val entity = entity(i)
    //     if (entity is AbstractVillager || entity is Raider) return entity
    //     throw MishapInvalidIota.of(data[i], data.size - i - 1, "entity.brainmerge_target")
    // }

    fun villager(i: Int): Villager {
        val entity = entity(i)
        if (entity is Villager) return entity
        throw MishapInvalidIota.of(data[i], data.size - i - 1, "entity.villager")
    }
}

package gun.raycast

import me.znotchill.blossom.extensions.toSmall
import me.znotchill.blossom.pos.SmallPos
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import kotlin.math.absoluteValue
import kotlin.math.floor

object RaycastEngine {
    fun shootFromPos(
        instance: Instance,
        start: Pos,
        direction: Vec,
        maxDistance: Double = 200.0,
        travelStep: Double = 0.05,
        journeyStep: Int = 2,
        excludeTypes: List<EntityType>? = listOf(), // types to ignore
        includeTypes: List<EntityType>? = null, // if not null, only these types are considered
        excludeEntity: Entity? = null
    ): RaycastResult {
        val dir = direction.normalize()
        val maxSteps = (maxDistance / travelStep).toInt()
        val journey = ArrayList<SmallPos>(maxSteps / journeyStep + 1)

        for (step in 0..maxSteps) {
            val rayPos = start.add(dir.mul(step * travelStep))

            // only store every journeyStep-th position to reduce RAM
            if (step % journeyStep == 0) {
                journey.add(rayPos.toSmall())
            }

            // skip if chunk not loaded
            if (!instance.isChunkLoaded(rayPos)) {
                return RaycastResult(
                    hitBlock = false,
                    hitBlockPosition = Pos(0.0, 0.0, 0.0),
                    hitBlockType = Block.AIR,
                    hitEntity = false,
                    entityHit = null,
                    hitUnloadedChunk = true,
                    hitMaxDistance = false,
                    hitPosition = rayPos,
                    relativeHitPosition = Pos(0.0, 0.0, 0.0),
                    hitFace = BlockFace.TOP,
                    journey = journey
                )
            }

            // Entity detection
            val entities = instance.getNearbyEntities(rayPos, 2.0)
                .filter { e ->
                    if (excludeEntity != null && e == excludeEntity) return@filter false
                    if (includeTypes != null) return@filter e.entityType in includeTypes
                    excludeTypes?.contains(e.entityType) != true
                }

            for (entity in entities) {
                val collided = entity.boundingBox
                    .boundingBoxRayIntersectionCheck(start.asVec(), dir, entity.position)
                if (collided) {
                    return RaycastResult(
                        hitBlock = false,
                        hitBlockPosition = Pos(0.0, 0.0, 0.0),
                        hitBlockType = Block.AIR,
                        hitEntity = true,
                        entityHit = entity,
                        hitUnloadedChunk = false,
                        hitMaxDistance = false,
                        hitPosition = rayPos,
                        relativeHitPosition = Pos(0.0, 0.0, 0.0),
                        hitFace = BlockFace.TOP,
                        journey = journey
                    )
                }
            }

            // Block detection
            val block = instance.getBlock(rayPos)
            if (block.isSolid) {
                val blockPos = Pos(floor(rayPos.x), floor(rayPos.y), floor(rayPos.z))
                val blockCenter = blockPos.add(0.5, 0.5, 0.5)
                val offset = rayPos.sub(blockCenter)
                val epsilon = 0.005

                val hitFace = when {
                    offset.x.absoluteValue + epsilon > offset.y.absoluteValue &&
                            offset.x.absoluteValue + epsilon > offset.z.absoluteValue ->
                                if (offset.x > 0) BlockFace.EAST else BlockFace.WEST
                    offset.y.absoluteValue + epsilon > offset.x.absoluteValue &&
                            offset.y.absoluteValue + epsilon > offset.z.absoluteValue ->
                                if (offset.y > 0) BlockFace.TOP else BlockFace.BOTTOM
                    else ->
                        if (offset.z > 0) BlockFace.SOUTH else BlockFace.NORTH
                }

                return RaycastResult(
                    hitBlock = true,
                    hitBlockPosition = blockPos,
                    hitBlockType = block,
                    hitEntity = false,
                    entityHit = null,
                    hitUnloadedChunk = false,
                    hitMaxDistance = false,
                    hitPosition = rayPos,
                    relativeHitPosition = offset,
                    hitFace = hitFace,
                    journey = journey
                )
            }
        }

        // Max distance reached
        return RaycastResult(
            hitBlock = false,
            hitBlockPosition = Pos(0.0, 0.0, 0.0),
            hitBlockType = Block.AIR,
            hitEntity = false,
            entityHit = null,
            hitUnloadedChunk = false,
            hitMaxDistance = true,
            hitPosition = start.add(dir.mul(maxDistance)),
            relativeHitPosition = Pos(0.0, 0.0, 0.0),
            hitFace = BlockFace.TOP,
            journey = journey
        )
    }
}
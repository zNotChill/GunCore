package me.znotchill.guncore.gun.raycast

import me.znotchill.blossom.pos.SmallPos
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

data class RaycastResult(
    val hitBlock: Boolean,
    val hitBlockPosition: Pos,
    val hitBlockType: Block,
    val hitEntity: Boolean,
    val entityHit: Entity?,
    val hitUnloadedChunk: Boolean,
    val hitMaxDistance: Boolean,
    val hitPosition: Pos,
    val relativeHitPosition: Pos,
    val hitFace: BlockFace,
    var journey: List<SmallPos> = emptyList(),
)
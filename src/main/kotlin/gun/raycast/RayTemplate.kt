package gun.raycast

import net.minestom.server.entity.EntityType

data class RayTemplate(
    val included: List<EntityType>?,
    val excluded: List<EntityType>?,
)
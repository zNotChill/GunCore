package gun.classes

import net.minestom.server.entity.LivingEntity

data class EntityData(
    val entity: LivingEntity,
    val isPlayer: Boolean,
    val isNonPlayer: Boolean,
    val weapons: MutableList<PhysicalGun>,
    val gunInactiveTimer: Int = 0,
    val isShooting: Boolean = false,

    var isPrimaryTriggered: Boolean = false,
    var isSecondaryTriggered: Boolean = false,
)
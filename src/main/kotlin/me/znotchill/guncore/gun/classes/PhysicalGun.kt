package me.znotchill.guncore.gun.classes

import me.znotchill.guncore.gun.Gun
import java.util.UUID

/**
 * Holds instanced specific gun data.
 */
data class PhysicalGun(
    var ammo: Int,
    var ammoReserve: Int = gun.magazineSize,
    var gun: Gun,
    var reloading: Boolean = false,
    var lastShotTick: Int = 0,
    var shootingTicks: Int = 0,
    val itemId: UUID,

    var isPrimaryTriggered: Boolean = false,
    var isSecondaryTriggered: Boolean = false,
)
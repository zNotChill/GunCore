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

    var isScoped: Boolean = false,
    var wasScoped: Boolean = false,
    var scopeTicks: Int = 0,

    var isAds: Boolean = false,
    var wasAds: Boolean = false,
    var adsTicks: Int = 0
)
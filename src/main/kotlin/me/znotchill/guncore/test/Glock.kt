package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunTriggerInput

object GlockGun : Gun(
    id = "glock",
    primaryTrigger = GunTriggerInput.LEFT_CLICK,
    secondaryTrigger = GunTriggerInput.RIGHT_CLICK,
    adsZoom = 20,
    fireRate = 5,
    spread = 0.0
)
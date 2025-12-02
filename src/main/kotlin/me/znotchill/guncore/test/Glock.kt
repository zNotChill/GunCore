package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunTriggerInput

object GlockGun : Gun(
    id = "glock",
    primaryTrigger = GunTriggerInput.LEFT_CLICK,
    secondaryTrigger = GunTriggerInput.RIGHT_CLICK,
    fireRate = 5f,
    hipfireSpread = 0.15,
    adsSpread = 0.15,
    adsZoom = 5,
)
package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunTriggerInput

object SMGGun : Gun(
    id = "smg",
    primaryTrigger = GunTriggerInput.LEFT_CLICK,
    secondaryTrigger = GunTriggerInput.RIGHT_CLICK,
    adsZoom = 20,
    fireRate = 12,
    spread = 0.04
)
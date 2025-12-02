package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunTriggerInput

object SMGGun : Gun(
    id = "smg",
    primaryTrigger = GunTriggerInput.LEFT_CLICK,
    secondaryTrigger = GunTriggerInput.RIGHT_CLICK,
    fireRate = 12f,

    hipfireSpread = 0.06,
    adsSpread = 0.04,
    adsZoom = 20,
)
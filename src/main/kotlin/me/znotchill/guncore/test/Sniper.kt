package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunAction
import me.znotchill.guncore.gun.classes.GunTriggerInput

object SniperGun : Gun(
    id = "sniper",
    primaryTrigger = GunTriggerInput.LEFT_CLICK,
    secondaryTrigger = GunTriggerInput.RIGHT_CLICK,
    secondaryAction = GunAction.SCOPE,
    fireRate = 0.5f,

    hipfireSpread = 0.5,
    scopedSpread = 0.0,
    scopedZoom = 60,
)
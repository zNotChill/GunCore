package me.znotchill.guncore.test

import me.znotchill.guncore.gun.Gun
import me.znotchill.guncore.gun.classes.GunTrigger

object SniperGun : Gun(
    id = "sniper",
    primaryTrigger = GunTrigger.LEFT_CLICK,
    secondaryTrigger = GunTrigger.RIGHT_CLICK,
    adsZoom = 60
)
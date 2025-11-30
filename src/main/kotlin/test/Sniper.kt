package me.znotchill.test

import gun.Gun
import me.znotchill.gun.classes.GunTrigger

object SniperGun : Gun(
    id = "sniper",
    primaryTrigger = GunTrigger.LEFT_CLICK,
    secondaryTrigger = GunTrigger.RIGHT_CLICK,
    adsZoom = 60
)
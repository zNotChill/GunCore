package test

import gun.Gun
import me.znotchill.gun.classes.GunTrigger

object GlockGun : Gun(
    id = "glock",
    primaryTrigger = GunTrigger.LEFT_CLICK,
    secondaryTrigger = GunTrigger.RIGHT_CLICK,
    adsZoom = 20,
    fireRate = 5,
    spread = 0.0
)
package gun

import gun.classes.EntityData
import gun.classes.PhysicalGun
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.PlayerHand
import gun.classes.GunTags
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import java.util.UUID

object LivingEntityUtils {
    fun LivingEntity.getHeldGun(hand: PlayerHand? = PlayerHand.MAIN): PhysicalGun? {
        var finalHand = PlayerHand.MAIN
        if (hand == null) {
            if (this is Player)
                finalHand = this.itemUseHand ?: PlayerHand.MAIN
        }

        val item = this.getItemInHand(finalHand)

        val isGun = item.hasTag(GunTags.IS_GUN)
        if (!isGun) return null

        val itemId = item.getTag(GunTags.ITEM_ID) ?: return null

        return this.gunData.weapons.firstOrNull {
            it.itemId == UUID.fromString(itemId)
        }
    }

    val LivingEntity.gunData: EntityData
        get() = GunCore.getEntityData(this)

    val LivingEntity.actualEyeHeight: Double
        get() {
            val entityBoundingBox = this.boundingBox
            val entityScale = this.getAttribute(Attribute.SCALE)
            val entityTop = (entityBoundingBox.maxY() * entityScale.value)

            return entityTop
        }
}
package me.znotchill.guncore.gun

import me.znotchill.guncore.gun.LivingEntityUtils.getHeldGun
import me.znotchill.guncore.gun.classes.EntityData
import me.znotchill.guncore.gun.classes.GunTags
import me.znotchill.guncore.gun.classes.PhysicalGun
import me.znotchill.blossom.extensions.addListener
import me.znotchill.blossom.extensions.ticks
import me.znotchill.blossom.scheduler.task
import me.znotchill.blossom.server.BlossomServer
import me.znotchill.marmot.common.api.MarmotEvent
import me.znotchill.marmot.minestom.api.MarmotAPI
import net.kyori.adventure.text.Component
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import java.util.*

object GunCore {
    private val registry: MutableMap<String, Gun> = mutableMapOf()
    private val entityData: MutableMap<UUID, EntityData> = mutableMapOf()

    lateinit var server: BlossomServer

    fun getGun(id: String): Gun? {
        return registry.filter { it.key == id }
            .values.firstOrNull()
    }

    fun getEntityData(entity: LivingEntity): EntityData {
        return entityData.filter { it.key == entity.uuid }
            .values.first()
    }

    fun createEntityData(entity: LivingEntity) {
        entityData[entity.uuid] = EntityData(
            entity = entity,
            isPlayer = entity is Player,
            isNonPlayer = entity !is Player,
            weapons = mutableListOf()
        )
    }

    fun register(gun: Gun) {
        registry[gun.id] = gun
    }

    fun announce(component: Component) {
        server.players.forEach {
            it.sendMessage(component)
        }
    }

    fun announce(message: String) {
        server.players.forEach {
            it.sendMessage(message)
        }
    }

    fun init(server: BlossomServer) {
        this@GunCore.server = server
        MarmotAPI.addEvent(MarmotEvent.LEFT_CLICK_BEGIN) { player ->
            val heldGun = processHeldGun(player)
            heldGun?.gun?.onPrimaryTriggerBegin(player as LivingEntity)
        }
        MarmotAPI.addEvent(MarmotEvent.LEFT_CLICK_END) { player ->
            val heldGun = processHeldGun(player)
            heldGun?.gun?.onPrimaryTriggerEnd(player as LivingEntity)
        }
        MarmotAPI.addEvent(MarmotEvent.RIGHT_CLICK_BEGIN) { player ->
            val heldGun = processHeldGun(player)
            heldGun?.gun?.onSecondaryTriggerBegin(player as LivingEntity)
        }
        MarmotAPI.addEvent(MarmotEvent.RIGHT_CLICK_END) { player ->
            val heldGun = processHeldGun(player)
            heldGun?.gun?.onSecondaryTriggerEnd(player as LivingEntity)
        }

        server.eventHandler.addListener<PlayerChangeHeldSlotEvent> { event ->
            val player = event.player

            // run 1 tick later to get the NEW item in the player's hand
            GunCore.server.scheduler.task {
                delay = 1.ticks
                run = { task ->
                    val heldGun = processHeldGun(player)
                    if (heldGun == null) {
                        task.cancel()
                    } else {
                        if (heldGun.isPrimaryTriggered)
                            heldGun.gun.onPrimaryTriggerEnd(player as LivingEntity)
                        if (heldGun.isSecondaryTriggered)
                            heldGun.gun.onSecondaryTriggerEnd(player as LivingEntity)
                    }
                }
            }
        }
    }

    private fun processHeldGun(player: Player): PhysicalGun? {
        val hand = player.itemUseHand ?: PlayerHand.MAIN
        val heldGun = player.getHeldGun(hand)
        val item = player.getItemInHand(hand)

        if (item.hasTag(GunTags.IS_GUN) &&
            item.getTag(GunTags.IS_GUN) != true
        ) return null
        if (heldGun == null) return null

        return heldGun
    }
}
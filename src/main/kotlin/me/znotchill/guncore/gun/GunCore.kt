package me.znotchill.guncore.gun

import me.znotchill.blossom.extensions.addListener
import me.znotchill.blossom.server.BlossomServer
import me.znotchill.guncore.gun.LivingEntityUtils.getPhysicalGun
import me.znotchill.guncore.gun.LivingEntityUtils.gunData
import me.znotchill.guncore.gun.classes.EntityData
import me.znotchill.guncore.gun.classes.GunTags
import me.znotchill.guncore.gun.classes.GunTriggerInput
import me.znotchill.guncore.gun.classes.PhysicalGun
import me.znotchill.guncore.gun.classes.click.ClickPhase
import me.znotchill.guncore.gun.classes.click.ClickSide
import me.znotchill.guncore.gun.classes.click.NormalizedClickEvent
import me.znotchill.marmot.common.api.MarmotEvent
import me.znotchill.marmot.minestom.api.MarmotAPI
import net.kyori.adventure.text.Component
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.event.player.PlayerLoadedEvent
import net.minestom.server.timer.TaskSchedule
import java.util.*

object GunCore {
    private val registry: MutableMap<String, Gun> = mutableMapOf()
    private val entityData: MutableMap<UUID, EntityData> = mutableMapOf()

    lateinit var server: BlossomServer

    private val marmotToNormalized = mapOf(
        MarmotEvent.LEFT_CLICK_BEGIN to NormalizedClickEvent(ClickSide.LEFT, ClickPhase.BEGIN),
        MarmotEvent.LEFT_CLICK_END to NormalizedClickEvent(ClickSide.LEFT, ClickPhase.END),
        MarmotEvent.RIGHT_CLICK_BEGIN to NormalizedClickEvent(ClickSide.RIGHT, ClickPhase.BEGIN),
        MarmotEvent.RIGHT_CLICK_END to NormalizedClickEvent(ClickSide.RIGHT, ClickPhase.END),
    )

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

        marmotToNormalized.forEach { (marmotEvent, normalized) ->
            MarmotAPI.addEvent(marmotEvent) { player ->
                handleClick(player, normalized)
            }
        }

        server.eventHandler.addListener<PlayerChangeHeldSlotEvent> { event ->
            val player = event.player

            val newPhysicalGun = processGunInSlot(player, event.newSlot)
            println(newPhysicalGun)
            val oldPhysicalGun = processGunInSlot(player, event.oldSlot)
            println(oldPhysicalGun)

            // cancel all interactions with the old gun
            // if this is null, don't do anything
            if (oldPhysicalGun != null) {
                oldPhysicalGun.gun.onPrimaryTriggerEnd(player)
                oldPhysicalGun.gun.onSecondaryTriggerEnd(player)
            }

            // immediately trigger interactions with the new gun
            // so you don't have to re-trigger from the client
            if (newPhysicalGun != null) {
                if (player.gunData.isPrimaryTriggered)
                    newPhysicalGun.gun.onPrimaryTriggerBegin(player)
                if (player.gunData.isSecondaryTriggered)
                    newPhysicalGun.gun.onSecondaryTriggerBegin(player)
            }
        }

        server.eventHandler.addListener<PlayerLoadedEvent> { event ->
            addScheduler(event.player)
        }
    }

    private fun handleClick(player: Player, event: NormalizedClickEvent) {
        val gun = processHeldGun(player) ?: return

        // update click state
        when (event.side) {
            ClickSide.LEFT -> player.gunData.isLeftClicking = event.phase == ClickPhase.BEGIN
            ClickSide.RIGHT -> player.gunData.isRightClicking = event.phase == ClickPhase.BEGIN
        }

        // determine which trigger this click should activate
        val isPrimary = when (event.side) {
            ClickSide.LEFT -> gun.gun.primaryTrigger == GunTriggerInput.LEFT_CLICK
            ClickSide.RIGHT -> gun.gun.primaryTrigger == GunTriggerInput.RIGHT_CLICK
        }

        val isSecondary = when (event.side) {
            ClickSide.LEFT -> gun.gun.secondaryTrigger == GunTriggerInput.LEFT_CLICK
            ClickSide.RIGHT -> gun.gun.secondaryTrigger == GunTriggerInput.RIGHT_CLICK
        }

        // run trigger callbacks
        if (isPrimary) {
            if (event.phase == ClickPhase.BEGIN) {
                player.gunData.isPrimaryTriggered = true
                gun.gun.onPrimaryTriggerBegin(player)
            } else {
                player.gunData.isPrimaryTriggered = false
                gun.gun.onPrimaryTriggerEnd(player)
            }
        }

        if (isSecondary) {
            if (event.phase == ClickPhase.BEGIN) {
                player.gunData.isSecondaryTriggered = true
                gun.gun.onSecondaryTriggerBegin(player)
            } else {
                player.gunData.isSecondaryTriggered = false
                gun.gun.onSecondaryTriggerEnd(player)
            }
        }
    }

    private fun processHeldGun(player: Player): PhysicalGun? {
        val hand = player.itemUseHand ?: PlayerHand.MAIN
        val heldGun = player.getPhysicalGun(hand)
        val item = player.getItemInHand(hand)

        if (item.hasTag(GunTags.IS_GUN) &&
            item.getTag(GunTags.IS_GUN) != true
        ) return null
        if (heldGun == null) return null

        return heldGun
    }

    private fun processGunInSlot(player: Player, slot: Int): PhysicalGun? {
        val heldGun = player.getPhysicalGun(slot = slot)
        val item = player.inventory.getItemStack(slot)

        if (item.hasTag(GunTags.IS_GUN) &&
            item.getTag(GunTags.IS_GUN) != true
        ) return null
        if (heldGun == null) return null

        return heldGun
    }

    private fun processGunInSlot(player: Player, slot: Byte): PhysicalGun? {
        return processGunInSlot(player, slot.toInt())
    }

    private fun addScheduler(player: Player) {
        player.scheduler().buildTask {
            val heldGun = player.getPhysicalGun()
                ?: return@buildTask
            if (!heldGun.isPrimaryTriggered || !player.gunData.isPrimaryTriggered)
                return@buildTask

            heldGun.gun.processPrimaryTriggerTick(player)

            if (player.gunData.isSecondaryTriggered) {
                heldGun.scopeTicks++

                // enter scoped mode after holding ADS for 10 ticks (0.5s)
                if (!heldGun.isScoped && heldGun.scopeTicks >= 10) {
                    heldGun.isScoped = true
                }
            } else {
                // reset scoping
                heldGun.wasScoped = heldGun.isScoped
                heldGun.isScoped = false
                heldGun.scopeTicks = 0
            }
        }
            .repeat(TaskSchedule.tick(1))
            .schedule()
    }
}
package me.znotchill.guncore

import me.znotchill.guncore.gun.GunCore
import me.znotchill.guncore.gun.LivingEntityUtils.gunData
import me.znotchill.guncore.gun.classes.GunTags
import me.znotchill.guncore.gun.classes.PhysicalGun
import me.znotchill.blossom.command.command
import me.znotchill.blossom.component.component
import me.znotchill.blossom.extensions.addListener
import me.znotchill.blossom.server.BlossomServer
import me.znotchill.blossom.server.essentials.GamemodeSwitcherConfig
import me.znotchill.blossom.server.essentials.classes.BareEssential
import me.znotchill.blossom.server.essentials.classes.asEntry
import me.znotchill.blossom.server.essentials.classes.withConfig
import me.znotchill.marmot.minestom.api.MarmotAPI
import me.znotchill.marmot.minestom.api.extensions.configureMouse
import me.znotchill.marmot.minestom.api.extensions.lockCamera
import me.znotchill.guncore.test.SniperGun
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerLoadedEvent
import net.minestom.server.instance.InstanceContainer
import me.znotchill.guncore.test.GlockGun
import me.znotchill.guncore.test.SMGGun
import java.util.*

class Server : BlossomServer(
    "GunCoreTest",
    auth = false,
    useBareEssentials = true,
    bareEssentials = listOf(
        BareEssential.GAMEMODE_SWITCHER.withConfig(
            GamemodeSwitcherConfig(
                permissionLevel = 4,
                successMessage = component {
                    text("Your gamemode has been updated to ")
                    text("{GAMEMODE}")
                }
            )
        ),
        BareEssential.BLOCK_PICKER.asEntry()
    )
) {
    lateinit var instanceContainer: InstanceContainer

    override fun preLoad() {
        MarmotAPI.registerEvents(eventHandler)
        MarmotAPI.registerTasks(scheduler)
        GunCore.init(this)
        GunCore.register(GlockGun)
        GunCore.register(SniperGun)
        GunCore.register(SMGGun)

        instanceContainer = BaseInstance().createInstance(instanceManager)

        eventHandler.addListener<AsyncPlayerConfigurationEvent> { event ->
            val player = event.player
            event.spawningInstance = instanceContainer
            player.gameMode = GameMode.ADVENTURE
            player.permissionLevel = 4
        }

//        eventHandler.addListener<PlayerPluginMessageEvent> { event ->
//            logger.info("Received Plugin Message: ${event.identifier}")
//        }

        eventHandler.addListener<PlayerLoadedEvent> { event ->
            GunCore.createEntityData(event.player)
            event.player.configureMouse(locked = true, emitEvents = true)
        }

        registerCommand(
            command("lockcamera") {
                val lock = argument<Boolean>("lock")
                syntax(lock) { lockBool ->
                    this.lockCamera(lockBool)
                }
            }
        )

        registerCommand(
            command("lockmouse") {
                val lock = argument<Boolean>("lock")
                val emit = argument<Boolean>("emit")
                syntax(lock, emit) { lockBool, emitBool ->
                    this.configureMouse(lockBool, emitBool)
                }
            }
        )

        registerCommand(
            command("gun") {
                val id = argument<String>("id")
                syntax(id) { idStr ->
                    val gun = GunCore.getGun(idStr) ?: return@syntax

                    val item = gun.createFinalItem()
                    inventory.addItemStack(item)

                    val physicalGun = PhysicalGun(
                        ammo = gun.magazineSize,
                        ammoReserve = gun.magazineReserveSize,
                        gun = gun,
                        itemId = UUID.fromString(item.getTag(GunTags.ITEM_ID))
                    )

                    gunData.weapons.add(physicalGun)
                }
            }
        )
    }
}

fun main() {
    Server().start()
}
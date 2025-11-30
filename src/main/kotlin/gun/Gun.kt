package gun

import gun.LivingEntityUtils.actualEyeHeight
import gun.LivingEntityUtils.getHeldGun
import gun.classes.GunTags
import gun.classes.GunType
import gun.classes.PhysicalGun
import gun.raycast.RayTemplate
import gun.raycast.RaycastEngine
import gun.raycast.RaycastResult
import kotlinx.serialization.Serializable
import me.znotchill.blossom.component.component
import me.znotchill.blossom.extensions.audience
import me.znotchill.blossom.extensions.playSounds
import me.znotchill.blossom.extensions.ticks
import me.znotchill.blossom.scheduler.task
import me.znotchill.blossom.sound.sound
import me.znotchill.gun.classes.BulletType
import me.znotchill.gun.classes.GunTrigger
import me.znotchill.marmot.common.classes.FovOp
import me.znotchill.marmot.minestom.api.extensions.adjustCamera
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.timer.TaskSchedule
import java.util.UUID
import kotlin.math.pow

@Serializable
open class Gun(
    /** The gun's ID */
    open var id: String,

    /** The gun type (defaults to [GunType.PRIMARY]) */
    open var type: GunType = GunType.PRIMARY,

    open var primaryTrigger: GunTrigger = GunTrigger.LEFT_CLICK,

    open var secondaryTrigger: GunTrigger = GunTrigger.RIGHT_CLICK,

    /** The type of bullet to shoot (defaults to [me.znotchill.gun.classes.BulletType.HITSCAN]) */
    open var bulletType: BulletType = BulletType.HITSCAN,

    /** The amount of health taken from a hit (defaults to 0) */
    open var damagePerShot: Int = 0,

    /** How many times the gun shoots per second (defaults to 0) */
    open var fireRate: Int = 5,

    /** The number of bullets in the gun's magazine (defaults to 0) */
    open var magazineSize: Int = 0,

    /** The number of bullets in the gun's magazine reserve (defaults to 0) */
    open var magazineReserveSize: Int = 0,

    /** The time it takes to reload the gun, in seconds (defaults to 2.0) */
    open var reloadTime: Double = 2.0,

    /** Maximum effective distance before damage drops off (defaults to 20.0 blocks) */
    open var effectiveRange: Double = 20.0,

    /**
     * The multiplier of damage to remove for every block after
     * a bullet travels outside the maximum effective range
     * (defaults to 0.1)
     */
    open var damageDropOffMultiplier: Double = 0.1,

    /** Deviation from the intended aim spot in blocks (defaults to 0.2) */
    open var spread: Double = 0.2,

    /**
     * The speed at which projectiles travel (only applies to projectile-based weapons).
     * Defaults to 10.0 blocks per second.
     */
    open var projectileSpeed: Double = 10.0,

    /** The percentage chance (1–100) to deal a critical hit (defaults to 20.0%) */
    open var criticalHitChance: Double = 20.0,

    /** The knockback applied to the shooter, in blocks (defaults to 0.1) */
    open var knockback: Double = 0.1,

    /** The trail color of the bullet dust particle (defaults to white) */
    open var trailParticleColor: Color = Color(255, 255, 255),

    /** How many pellets this gun fires per shot (1 for normal guns, 8–12 for shotguns, etc.) */
    open var pelletsPerShot: Int = 1,

    /**
     * The sounds to play at the shooter once a bullet is fired.
     *
     * Scope: **plays for all nearby players**
     */
    open var shootSound: List<Sound> = listOf(sound("block.anvil.fall")),

    /**
     * The sounds to play at the shooter once a reload is triggered.
     *
     * Scope: **plays for all nearby players**
     */
    open var reloadSound: List<Sound> = listOf(sound("block.anvil.fall")),

    /**
     * The sounds to play at the shooter once a reload is triggered,
     * but the shooter has no ammo in the magazine or in reserve.
     *
     * Scope: **plays for all nearby players**
     */
    open var emptyAmmoReloadSound: List<Sound> = listOf(sound("block.stone_button.click_on") {
        pitch = 1.5
    }),

    /**
     * The sounds to play at the shooter once a bullet is attempted to fire,
     * but the shooter has no ammo in the magazine or in reserve.
     *
     * Scope: **plays for all nearby players**
     */
    open var emptyAmmoShootSound: List<Sound> = listOf(sound("block.stone_button.click_on") {
        pitch = 2.0
    }),

    /**
     * The sounds to play at the shooter once an enemy is hit, and
     * it was a critical hit.
     *
     * Scope: **plays for the shooter (player only)**
     */
    open var critSound: List<Sound> = listOf(sound("block.anvil.fall")),

    /**
     * The sounds to play at the impact position once a bullet hits an object.
     *
     * Scope: **plays for the all nearby players**
     */
    open var impactHitSound: List<Sound> = listOf(sound("block.anvil.fall")),

    /**
     * The sounds to play at the shooter once a bullet hits an enemy.
     *
     * Scope: **plays for the shooter (player only)**
     */
    open var hitSound: List<Sound> = listOf(sound("block.anvil.fall")),

    /**
     * The physical item to use when the gun is spawned in the game world.
     *
     * The item should not have a custom name or lore since this is overridden
     * once the gun is spawned.
     * **Disable this functionality by setting [overrideItemData] to false.**
     */
    open var item: ItemStack = ItemStack.of(Material.IRON_HOE),

    /**
     * Whether the custom name and lore of the [item] should be overridden
     * upon spawning the item in the game world.
     */
    open var overrideItemData: Boolean = false,

    /**
     * The list of keyframes to use when animating the gun upon shooting.
     *
     * Takes in a list of [Any] but should correspond to a custom model data field.
     *
     * Supports [Float], [String].
     */
    open var shootKeyframes: List<Any> = listOf(),

    /**
     * The amount of ticks to spend per keyframe.
     */
    open var shootAnimationRate: Int = 1,

    /**
     * The list of keyframes to use when animating the gun upon reloading.
     *
     * Takes in a list of [Any] but should correspond to a custom model data field.
     *
     * Supports [Float], [String].
     */
    open var reloadKeyframes: List<Any> = listOf(),

    /**
     * The amount of ticks to spend per keyframe.
     */
    open var reloadAnimationRate: Int = 1,

    /**
     * The amount of FOV to zoom in when the player ADS'.
     */
    open var adsZoom: Int = 5,
) {
    fun createFinalItem(): ItemStack {
        var item = this.item

        if (overrideItemData) {
            item = item.with { meta ->
                meta.customName(
                    component {
                        text(id)
                    }
                )
            }
        }

        item = item
            .withTag(GunTags.IS_GUN, true)
            .withTag(GunTags.ITEM_TYPE, id)
            .withTag(GunTags.ITEM_ID, UUID.randomUUID().toString())

        return item
    }


    /**
     * Attempt to reload the [LivingEntity]'s held [PhysicalGun].
     * If their total ammo reserve is <= 0, we cannot reload.
     * Only removes the appropriate amount of ammo, based on the amount of bullets in your magazine.
     *
     * If their gun is full of ammo, we cannot reload.
     */
    open fun tryReload(entity: LivingEntity, physicalGun: PhysicalGun) {
        if (physicalGun.ammoReserve <= 0) {
            if (entity is Player)
                entity.playSounds(emptyAmmoReloadSound)
            return
        }

        if (physicalGun.ammo >= physicalGun.gun.magazineSize) {
            return
        }

        physicalGun.reloading = true
        MinecraftServer.getSchedulerManager().scheduleTask(
            {
                // Check if the player is still holding the item
                // and if they aren't, stop reloading
                // Technically this approach allows the player to
                // swap to and from the reload and then swap back on the tick
                // that this finishes, but it shouldn't be an issue for now lol
                if (entity.getHeldGun() != physicalGun) {
                    physicalGun.reloading = false
                    return@scheduleTask
                }

                val needed = physicalGun.gun.magazineSize - physicalGun.ammo
                val taken = minOf(needed, physicalGun.ammoReserve)

                physicalGun.ammo += taken
                physicalGun.ammoReserve -= taken
                physicalGun.reloading = false

                if (entity is Player)
                    entity.playSounds(reloadSound)
            },
            TaskSchedule.seconds(physicalGun.gun.reloadTime.toLong()),
            TaskSchedule.stop()
        )
    }

    fun onPrimaryTriggerBegin(entity: LivingEntity) {
        GunCore.announce("begin primary trigger")
        val heldGun = entity.getHeldGun() ?: return
        heldGun.isPrimaryTriggered = true
        heldGun.lastShotTick = 0
        heldGun.shootingTicks = 0

        GunCore.server.scheduler.task {
            repeat = 1.ticks
            run = { task ->
                if (!heldGun.isPrimaryTriggered)
                    task.cancel()
                else {
                    processPrimaryTriggerTick(entity)
                }
            }
        }
    }

    fun processPrimaryTriggerTick(entity: LivingEntity) {
        val heldGun = entity.getHeldGun() ?: return
        val ticksBetweenShots = 20 / fireRate

        heldGun.shootingTicks += 1
        if ((heldGun.shootingTicks % ticksBetweenShots) == 0 ||
            heldGun.shootingTicks == 0) {
            heldGun.lastShotTick = heldGun.shootingTicks
            GunCore.announce("shot at ${heldGun.shootingTicks}")

            tryShoot(
                entity.instance.players.audience,
                entity
            )
        }
    }

    fun onPrimaryTriggerEnd(entity: LivingEntity) {
        GunCore.announce("end primary trigger")
        val heldGun = entity.getHeldGun() ?: return
        heldGun.isPrimaryTriggered = false
        heldGun.lastShotTick = 0
        heldGun.shootingTicks = 0
    }

    fun onSecondaryTriggerBegin(entity: LivingEntity) {
        println("begin secondary trigger")

        val heldGun = entity.getHeldGun() ?: return
        heldGun.isSecondaryTriggered = true

        if (entity is Player) {
            entity.adjustCamera(
                fovOp = FovOp.SUB,
                fov = adsZoom.toFloat(),
                fovAnimTicks = 15,
                lockFov = true,
                animateFov = true
            )
        }
    }

    fun onSecondaryTriggerEnd(entity: LivingEntity) {
        println("end secondary trigger")

        val heldGun = entity.getHeldGun() ?: return
        heldGun.isSecondaryTriggered = false

        if (entity is Player) {
            entity.adjustCamera(
                fovOp = FovOp.ADD,
                fov = adsZoom.toFloat(),
                fovAnimTicks = 15,
                lockFov = true,
                animateFov = true
            )
        }
    }

    /**
     * Gets a list of raycast included/excluded entities for the sake of clean raycast shooting.
     */
    fun getRayTemplate(): RayTemplate {
        val genericExcludes = listOf(
            EntityType.ITEM_DISPLAY,
            EntityType.TEXT_DISPLAY,
            EntityType.BLOCK_DISPLAY,
        )

        return RayTemplate(
            included = null,
            excluded = genericExcludes
        )
    }

    /**
     * Simple function for shooting a ray from the entity's eyes and returning the data.
     * Includes the ray templates.
     */
    open fun shootRay(
        entity: LivingEntity,
        maxDistance: Double = 50.0,
        direction: Vec? = null
    ): RaycastResult {
        val startPos = entity.position.add(0.0, entity.actualEyeHeight, 0.0)
        val rayTemplate = getRayTemplate()
        return RaycastEngine.shootFromPos(
            entity.instance,
            startPos,
            direction ?: entity.position.direction(),
            excludeTypes = rayTemplate.excluded,
            includeTypes = rayTemplate.included,
            excludeEntity = entity,
            maxDistance = maxDistance
        )
    }

    /**
     * Simple function for shooting a ray from a position and instance and returning the data.
     * Includes the ray templates.
     */
    open fun shootRayFromPos(
        pos: Pos,
        instance: Instance,
        maxDistance: Double = 50.0,
        direction: Vec
    ): RaycastResult {
        val rayTemplate = getRayTemplate()
        return RaycastEngine.shootFromPos(
            instance,
            pos,
            direction,
            excludeTypes = rayTemplate.excluded,
            includeTypes = rayTemplate.included,
            maxDistance = maxDistance
        )
    }


    open fun tryShoot(
        audience: Audience? = null,
        shooter: LivingEntity
    ) {
        val heldGun = shooter.getHeldGun() ?: return
        val pos = shooter.position
        val instance = shooter.instance

        when (bulletType) {
            BulletType.HITSCAN -> {
                audience?.playSounds(shootSound, pos)

                val baseDir = pos.direction().normalize()
                repeat(pelletsPerShot) {
                    val spreadAmount = spread
                    val offsetX = (Math.random() - 0.5) * 2 * spreadAmount
                    val offsetY = (Math.random() - 0.5) * 2 * spreadAmount
                    val offsetZ = (Math.random() - 0.5) * 2 * spreadAmount
                    val spreadDir = baseDir.add(offsetX, offsetY, offsetZ).normalize()

                    val raycastResult: RaycastResult = shootRay(
                        shooter, direction = spreadDir
                    )

                    displayTrail(pos, instance, raycastResult)

//                    if (raycastResult.hitEntity &&
//                        raycastResult.entityHit is LivingEntity &&
//                        (
//                                (entity is Player && raycastResult.entityHit !is Player) ||
//                                        (entity !is Player && raycastResult.entityHit is Player)
//                                )
//                    )

                    val entityHit = raycastResult.entityHit
                    if (
                        raycastResult.hitEntity &&
                        entityHit is LivingEntity
                    ) {
                        val distance = pos.distance(raycastResult.hitPosition)
                        val damage = calculateDamage(distance).toInt()

                        if (shooter is Player) {
                            shooter.playSounds(hitSound, raycastResult.hitPosition)
                        }

                        println("hit: pellet dealt $damage dmg at ${"%.1f".format(distance)} blocks")
                    }
                }

                heldGun.ammo -= 1
            }
            else -> {}
        }
    }

    open fun displayTrail(entity: LivingEntity, raycastResult: RaycastResult) {
        val eyePos = entity.position.add(0.0, entity.actualEyeHeight, 0.0)
        displayTrail(
            eyePos,
            entity.instance,
            raycastResult
        )
    }



    open fun displayTrail(
        pos: Pos,
        instance: Instance,
        raycastResult: RaycastResult
    ) {
        raycastResult.journey.forEachIndexed { index, journeyPos ->
            // only display every 3rd particle
            if (index % 10 != 0) return@forEachIndexed
            val actualPos = journeyPos.toPos()

            // skip particles too close to player's eyes to avoid flash banging
            // and poor visibility
            val distance = pos.distance(actualPos)
            if (distance < 1) return@forEachIndexed

            val particle = ParticlePacket(
                Particle.DUST.withProperties(
                    trailParticleColor,
                    0.2f
                ),
                actualPos.x,
                actualPos.y,
                actualPos.z,
                0f, 0f, 0f, 1f, 10
            )

            instance.players
                .filter { it.position.distance(actualPos) < 50 }
                .forEach { it.sendPacket(particle) }
        }
    }


    fun calculateDamage(distance: Double): Double {
        val range = effectiveRange
        val basePerPellet = damagePerShot.toDouble() / pelletsPerShot

        return if (distance <= range) {
            basePerPellet
        } else {
            val extraDistance = distance - range
            val multiplier = (1.0 - damageDropOffMultiplier).pow(extraDistance)
            (basePerPellet * multiplier).coerceAtLeast(1.0)
        }
    }
}
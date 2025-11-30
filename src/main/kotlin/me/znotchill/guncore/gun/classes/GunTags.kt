package me.znotchill.guncore.gun.classes

import net.minestom.server.tag.Tag

object GunTags {
    val IS_GUN: Tag<Boolean?>? = Tag.Boolean("is_gun")
    val ITEM_TYPE: Tag<String?>? = Tag.String("item_type")
    val ITEM_ID: Tag<String?>? = Tag.String("item_id")
}
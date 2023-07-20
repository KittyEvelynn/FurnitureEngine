package com.mira.furnitureengine.utils

import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.furniture.core.Furniture.RotSides
import com.mira.furnitureengine.furniture.core.SubModel
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.util.Vector
import kotlin.math.round

object Utils {
    const val furnitureFormatVersion = 3
    fun itemsMatch(item1: ItemStack?, item2: ItemStack?): Boolean {
        if (item1 == null || item2 == null) return false
        if (item1.type != item2.type) return false
        if (item1.hasItemMeta() && item2.hasItemMeta()) {
            if (item1.itemMeta!!.hasDisplayName() && item2.itemMeta!!.hasDisplayName()) {
                if (item1.itemMeta!!.displayName != item2.itemMeta!!.displayName) return false
            }
            if (item1.itemMeta!!.hasLore() && item2.itemMeta!!.hasLore()) {
                if (item1.itemMeta!!.lore != item2.itemMeta!!.lore) return false
            }
            if (item1.itemMeta!!.hasCustomModelData() && item2.itemMeta!!.hasCustomModelData()) {
                if (item1.itemMeta!!.customModelData != item2.itemMeta!!.customModelData) return false
            }
        }
        return true
    }

    fun calculatePlacingLocation(clickedBlock: Block?, clickedFace: BlockFace?): Location {
        val location = clickedBlock!!.location
        if (!isSolid(clickedBlock)) return location
        when (clickedFace) {
            BlockFace.UP -> {
                location.add(0.0, 1.0, 0.0)
            }

            BlockFace.DOWN -> {
                location.add(0.0, -1.0, 0.0)
            }

            BlockFace.NORTH -> {
                location.add(0.0, 0.0, -1.0)
            }

            BlockFace.SOUTH -> {
                location.add(0.0, 0.0, 1.0)
            }

            BlockFace.WEST -> {
                location.add(-1.0, 0.0, 0.0)
            }

            BlockFace.EAST -> {
                location.add(1.0, 0.0, 0.0)
            }

            else -> {}
        }
        return location
    }

    /**
     * Used to check if furniture (presumably with submodels) can space to be placed at a specific location
     * @param location The location to check
     * @param rotation The rotation of the furniture
     * @param furniture The furniture to check
     * @return Whether the furniture can be placed or not
     */
    fun hasSpace(location: Location?, rotation: Rotation?, furniture: Furniture): Boolean {
        if (isSolid(location!!.block)) return false
        if (furniture.subModels.size == 0) return true
        for (subModel in furniture.subModels) {
            val subModelLocation = getRelativeLocation(location, subModel.offset, rotation)
            if (isSolid(subModelLocation.block)) return false
        }
        return true
    }

    /**
     * Used to get the location of a submodel from the origin
     * @param input The origin
     * @param offset The offset of the submodel
     * @param rotation The rotation of the item
     * @return The relative location
     */
    fun getRelativeLocation(input: Location?, offset: Vector?, rotation: Rotation?): Location {
        return when (rotation) {
            Rotation.CLOCKWISE -> {
                input!!.clone().add(-offset!!.z, offset.y, offset.x)
            }

            Rotation.FLIPPED -> {
                input!!.clone().add(-offset!!.x, offset.y, -offset.z)
            }

            Rotation.COUNTER_CLOCKWISE -> {
                input!!.clone().add(offset!!.z, offset.y, -offset.x)
            }

            else -> {
                input!!.clone().add(offset!!)
            }
        }
    }

    /**
     * Used to get the location of the origin from a submodel
     * @param input The current location
     * @param furniture The furniture
     * @return The relative location
     */
    fun getOriginLocation(input: Location?, furniture: Furniture): Location? {
        // Check for item displays
        val entities = input!!.world!!
            .getNearbyEntities(input.clone().add(0.5, 0.5, 0.5), 0.1, 0.1, 0.1)
        for (entity in entities) {
            if (entity is ItemDisplay) {
                // Get the item and compare it to the furniture
                if (itemsMatch(entity.itemStack, furniture.blockItem)) {
                    return input.clone()
                } else {
                    for (subModel in furniture.subModels) {
                        if (itemsMatch(entity.itemStack, furniture.generateSubModelItem(subModel))) {
                            val rotation: Rotation = angleToRotation(entity.location.yaw)
                            val offset = subModel.offset.clone()
                            return when (rotation) {
                                Rotation.CLOCKWISE -> {
                                    input.clone().subtract(-offset.z, offset.y, offset.x)
                                }

                                Rotation.FLIPPED -> {
                                    input.clone().subtract(-offset.x, offset.y, -offset.z)
                                }

                                Rotation.COUNTER_CLOCKWISE -> {
                                    input.clone().subtract(offset.z, offset.y, -offset.x)
                                }

                                else -> {
                                    input.clone().subtract(offset)
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Used to get the rotation of an entity
     * @param entity Entity to get it from
     * @param rotSides How many sides the entity has
     * @return The rotation according to the rotation enum
     */
    fun getRotation(entity: Entity, rotSides: RotSides?): Rotation {
        var y = entity.location.yaw
        if (y < 0) y += 360f
        y %= 360f
        val i = ((y + 8) / 22.5).toInt()
        when (rotSides) {
            RotSides.FOUR_SIDED -> {
                when (i) {
                    14, 15, 16, 0, 1 -> {
                        return Rotation.FLIPPED
                    }

                    2, 3, 4, 5 -> {
                        return Rotation.COUNTER_CLOCKWISE
                    }

                    6, 7, 8, 9 -> {
                        return Rotation.NONE
                    }

                    10, 11, 12, 13 -> {
                        return Rotation.CLOCKWISE
                    }
                }
            }
            RotSides.EIGHT_SIDED -> {
                when (i) {
                    15, 16, 0, 1 -> {
                        return Rotation.FLIPPED
                    }

                    2 -> {
                        return Rotation.FLIPPED_45
                    }

                    3, 4, 5 -> {
                        return Rotation.COUNTER_CLOCKWISE
                    }

                    6 -> {
                        return Rotation.COUNTER_CLOCKWISE_45
                    }

                    7, 8, 9 -> {
                        return Rotation.NONE
                    }

                    10 -> {
                        return Rotation.CLOCKWISE_45
                    }

                    11, 12, 13 -> {
                        return Rotation.CLOCKWISE
                    }

                    14 -> {
                        return Rotation.CLOCKWISE_135
                    }
                }
            }
            else -> {
                return Rotation.NONE
            }
        }
        return Rotation.NONE
    }

    fun entityObstructing(location: Location?): Boolean {
        // Check if there is an entity obstructing the location (but item displays get ignored)
        for (entity in location!!.world!!
            .getNearbyEntities(location.add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)) {
            if (entity.type.isAlive && entity.type != EntityType.ITEM_DISPLAY) {
                return true
            }
        }
        return false
    }

    /*
        Apparently the Material.isSolid() method is not exactly what I need... For example flowers are considered non-solid,
        but you shouldn't be able to place furniture on them. So I made my own method.
     */
    fun isSolid(block: Block?): Boolean {
        return if (block!!.type.isSolid) true else {
            if (block.type.blastResistance > 0.2) return true
            if (block.type.name.contains("SAPLING")) return true
            if (block.type.name.contains("FLOWER") || block.type.name.contains("TULIP")) return true
            if (block.type.name.contains("CARPET")) return true
            if (block.type.name.contains("MUSHROOM") || block.type.name.contains("FUNGUS")) return true
            if (block.type.name.contains("BANNER")) return true
            when (block.type) {
                Material.TORCH, Material.SOUL_TORCH, Material.LANTERN, Material.SOUL_LANTERN, Material.REDSTONE_WIRE, Material.REDSTONE, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.NETHER_PORTAL, Material.END_PORTAL, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.SWEET_BERRY_BUSH, Material.SCAFFOLDING, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.NETHER_WART, Material.FLOWER_POT, Material.END_ROD, Material.KELP, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.COBWEB -> {
                    return true
                }
                else -> {}
            }
            false
        }
    }

    /**
     * It's the same case as the isSolid() method, but for interactable blocks.
     * Because for SOME REASON STAIRS ARE INTERACTABLE??? WHAT THE HELL MOJANG
     * @param block The block to check
     * @return Whether the block is interactable or not
     */
    fun isInteractable(block: Block?): Boolean {
        return if (!block!!.type.isInteractable) false else {
            if (block.type.name.contains("STAIRS")) return false
            if (block.type.name.contains("TNT")) return false
            if (block.type.name.contains("FENCE")) return false
            !block.type.name.contains("IRON") // iron door, iron trapdoor
        }
    }

    /**
     * Gets the color of furniture, if it is colorable
     * @param location The location of the furniture
     * @return The color of the block, or null if it is not colorable
     */
    fun getColor(location: Location?): Color? {
        val entities = location!!.world!!
            .getNearbyEntities(location, 0.2, 0.2, 0.2)
        for (entity in entities) {
            if (entity.type != EntityType.ITEM_DISPLAY) continue
            val itemDisplay = entity as ItemDisplay
            if (itemDisplay.itemStack!!.type == Material.TIPPED_ARROW) {
                val potionMeta = itemDisplay.itemStack!!.itemMeta as PotionMeta?
                return if (potionMeta!!.hasColor()) {
                    potionMeta.color
                } else null
            }
        }
        return null
    }

    /**
     * Checks if the furniture is only vertical (no sideways submodels)
     * @param subModels The submodels to check
     * @return Whether the furniture is only vertical or not
     */
    fun onlyVertical(subModels: List<SubModel>): Boolean {
        for (subModel in subModels) {
            if (subModel.offset.x != 0.0 || subModel.offset.z != 0.0) {
                return false
            }
        }
        return true
    }

    fun rotationToAngle(rotation: Rotation): Float{
        return when(rotation){
            Rotation.NONE -> -180f
            Rotation.CLOCKWISE_45 -> -135f
            Rotation.CLOCKWISE -> -90f
            Rotation.CLOCKWISE_135 -> -45f
            Rotation.FLIPPED -> 0f
            Rotation.FLIPPED_45 -> 45f
            Rotation.COUNTER_CLOCKWISE -> 90f
            Rotation.COUNTER_CLOCKWISE_45 -> 135f
        }
    }

    fun angleToRotation(angle: Float): Rotation{
        val i = (round((angle+180)/45)).toInt() % 8
        return when(if (i<0) i + 45 else i){
            0 -> Rotation.NONE
            1 -> Rotation.CLOCKWISE_45
            2 -> Rotation.CLOCKWISE
            3 -> Rotation.CLOCKWISE_135
            4 -> Rotation.FLIPPED
            5 -> Rotation.FLIPPED_45
            6 -> Rotation.COUNTER_CLOCKWISE
            7 -> Rotation.COUNTER_CLOCKWISE_45
            else -> Rotation.NONE // this should never happen
        }
    }
}

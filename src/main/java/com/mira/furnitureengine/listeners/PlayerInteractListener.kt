package com.mira.furnitureengine.listeners

import com.mira.furnitureengine.furniture.FurnitureManager
import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.furniture.functions.FunctionType
import com.mira.furnitureengine.utils.Utils
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PlayerInteractListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Ignore on offhand
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val player = event.player

        // 1. Placing
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            // Ignore if it interacts with a block (eg. opening a chest, crafting table, door, etc.)
            if (!player.isSneaking) {
                // Check if the block is a furniture block
                val furniture: Furniture? =
                    FurnitureManager.instance!!.isFurniture(event.clickedBlock!!.location)
                if (furniture != null) {
                    if (furniture.callFunction(
                            FunctionType.RIGHT_CLICK,
                            event.clickedBlock!!.location,
                            player,
                            Utils.getOriginLocation(event.clickedBlock!!.location, furniture)
                        )
                    ) return
                }
                if (Utils.isInteractable(event.clickedBlock)) return
            } else {
                val furniture: Furniture? = FurnitureManager.instance!!.isFurniture(
                    event.clickedBlock!!.location
                )
                furniture?.callFunction(
                    FunctionType.SHIFT_RIGHT_CLICK,
                    event.clickedBlock!!.location,
                    player,
                    Utils.getOriginLocation(event.clickedBlock!!.location, furniture)
                )
            }

            // Check if the item in the player's hand is a furniture item
            var hand = EquipmentSlot.HAND
            var item = player.inventory.itemInMainHand
            if (item.type == Material.AIR) {
                item = player.inventory.itemInOffHand
                hand = EquipmentSlot.OFF_HAND
            }
            if (item.type == Material.AIR) return
            if (player.gameMode == GameMode.ADVENTURE) return

            // Check if the item is a furniture item
            if (item.hasItemMeta() && item.itemMeta!!.hasCustomModelData()) {
                for (furniture in FurnitureManager.instance!!.getFurniture()) {
                    if (Utils.itemsMatch(item, furniture.generatedItem)) {
                        furniture.place(
                            player,
                            hand,
                            Utils.calculatePlacingLocation(event.clickedBlock, event.blockFace)
                        )
                        return
                    }
                }
            }
        }

        // 2. Breaking
        if (event.action == Action.LEFT_CLICK_BLOCK) {
            val furniture: Furniture? =
                FurnitureManager.instance!!.isFurniture(event.clickedBlock!!.location)
            furniture?.let {
                if (player.isSneaking) {
                    furniture.callFunction(
                        FunctionType.SHIFT_LEFT_CLICK,
                        event.clickedBlock!!.location,
                        player,
                        Utils.getOriginLocation(event.clickedBlock!!.location, furniture)
                    )
                } else {
                    furniture.callFunction(
                        FunctionType.LEFT_CLICK,
                        event.clickedBlock!!.location,
                        player,
                        Utils.getOriginLocation(event.clickedBlock!!.location, furniture)
                    )
                }
                val origin = Utils.getOriginLocation(
                    event.clickedBlock!!.location, furniture
                )
                if (origin != null) {
                    furniture.breakFurniture(player, origin)
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityShoot(event: EntityShootBowEvent) {
        for (furniture in FurnitureManager.instance!!.getFurniture()) {
            if (Utils.itemsMatch(event.consumable, furniture.generatedItem)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        // Scenario - Block in off hand, furniture in main hand
        // In this case cancel the event and place the furniture
        if (event.hand == EquipmentSlot.OFF_HAND) {
            val item = event.player.inventory.itemInMainHand
            if (item.type.isAir) return
            if (item.hasItemMeta() && item.itemMeta!!.hasCustomModelData()) {
                for (furniture in FurnitureManager.instance!!.getFurniture()) {
                    if (Utils.itemsMatch(item, furniture.generatedItem)) {
                        event.isCancelled = true
                        return
                    }
                }
            }
        }
    }
}

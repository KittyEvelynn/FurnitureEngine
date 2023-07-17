package com.mira.furnitureengine.events

import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.block.Action

class FurnitureInteractEvent(
    val player: Player,
    val action: Action,
    val furnitureLocation: Location,
    val blockFace: BlockFace
) : Event() {
    var isCancelled = false

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }

    companion object {
        private val handlers = HandlerList()
    }
}
package com.mira.furnitureengine.events

import com.mira.furnitureengine.furniture.core.Furniture
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class FurnitureBreakEvent(val furniture: Furniture, val player: Player?, val furnitureLocation: Location) : Event() {
    var isCancelled = false
    var isDroppingItems = true

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }

    companion object {
        private val handlers = HandlerList()
    }
}
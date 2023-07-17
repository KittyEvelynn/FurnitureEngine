package com.mira.furnitureengine.furniture.functions.internal.fmanip

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.furniture.FurnitureManager
import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.furniture.functions.Function
import com.mira.furnitureengine.furniture.functions.FunctionType
import com.mira.furnitureengine.utils.Utils
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player

class ReplaceFunction : Function {
    override val type: String
        get() = "REPLACE"

    @Throws(IllegalArgumentException::class)
    override fun execute(args: HashMap<String, Any?>) {
        val origin = args["origin"] as Location?
        val furnitureOverride = args["new"] as String?

        // Get the furniture at the origin location, get the rotation, and remove it. Then, spawn the new furniture with the same rotation.
        val furniture: Furniture = FurnitureManager.instance!!.isFurniture(origin)
            ?: throw UnknownError("No furniture found at origin location. How did this happen?")

        // rotation
        val entities = origin!!.world!!
            .getNearbyEntities(origin.clone().add(0.5, 0.0, 0.5), 0.2, 0.2, 0.2)
        var rot: Rotation? = null
        for (entity in entities) {
            if (entity.type != EntityType.ITEM_FRAME) continue
            val itemFrame = entity as ItemFrame
            if (Utils.itemsMatch(itemFrame.item, furniture.blockItem)) {
                rot = itemFrame.rotation
                break
            }
        }
        if (rot == null) {
            throw UnknownError("No item frame found at origin location. How did this happen?")
        }
        val newFurniture: Furniture? = FurnitureManager.instance!!.getFurniture(furnitureOverride)
        if (newFurniture == null) {
            FurnitureEngine.instance!!.logger
                .warning("Failed to replace furniture: $furnitureOverride does not exist.")
            return
        }
        val color = Utils.getColor(origin)
        furniture.breakFurniture(null, origin)
        if (!Utils.hasSpace(origin, rot, newFurniture)) {
            // Respawn the old furniture
            furniture.spawn(origin, rot, color)
            return
        }
        newFurniture.spawn(origin, rot, color)
        newFurniture.callFunction(
            FunctionType.REPLACE,
            args["location"] as Location?,
            args["player"] as Player?,
            Utils.getOriginLocation(args["location"] as Location?, newFurniture)
        )
    }
}

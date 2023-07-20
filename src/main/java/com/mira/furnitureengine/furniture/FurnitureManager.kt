package com.mira.furnitureengine.furniture

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.utils.Utils
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay

class FurnitureManager private constructor() {
    private val furniture: MutableList<Furniture> = ArrayList()

    init {
        loadFurniture(true)
    }

    private fun loadFurniture(log: Boolean) {
        furniture.clear()
        for (furnitureName in plugin!!.config.getConfigurationSection("Furniture")!!
            .getKeys(false)) {
            try {
                val furniture = Furniture(furnitureName)
                this.furniture.add(furniture)
                if (log) plugin.logger.info("Loaded furniture: $furniture")
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Failed to load furniture: $furnitureName")
            }
        }
    }

    fun reloadFurniture() {
        loadFurniture(true)
    }

    /**
     * Returns a specific furniture asset by its id
     * @param id The id of the furniture
     * @return The furniture asset
     */
    fun getFurniture(id: String?): Furniture? {
        for (furniture in furniture) {
            if (furniture.id.equals(id, ignoreCase = true)) return furniture
        }
        return null
    }

    /**
     * Returns a list of all furniture assets
     * @return The list of furniture assets
     */
    fun getFurniture(): List<Furniture> {
        return furniture
    }

    val ids: MutableList<String>
        /**
         * Returns a list of all furniture ids
         * @return The list of furniture ids
         */
        get() {
            val ids: MutableList<String> = ArrayList()
            for (furniture in furniture) {
                ids.add(furniture.id)
            }
            return ids
        }

    /**
     * Checks if a block at a specific location is a furniture block. This also checks for submodels.
     * @param location The location of the block
     * @return The furniture asset, or null if it's not a furniture block
     */
    fun isFurniture(location: Location?): Furniture? {
        // Check if there is a barrier block at the location
        var location = location
        if (location!!.block.type != Material.BARRIER) return null
        // Get the middle of the block, so that it's as accurate as possible
        location = location.clone().add(0.5, 0.5, 0.5)

        // get all entities at the location, and check if one of them is an item display

        val entities = location.world!!
            .getNearbyEntities(location, 0.1, 0.1, 0.1)
        for (entity in entities) {
            if (entity.type != EntityType.ITEM_DISPLAY) continue
            val itemDisplay = entity as ItemDisplay

            // Check if the item is a furniture item
            for (furniture in furniture) {
                if (Utils.itemsMatch(itemDisplay.itemStack, furniture.blockItem)) return furniture

                // Additionally, check if any of the submodels match
                for (subModel in furniture.subModels) {
                    if (Utils.itemsMatch(itemDisplay.itemStack, furniture.generateSubModelItem(subModel))) return furniture
                }
            }
        }
        return null
    }

    companion object {
        var instance: FurnitureManager? = null
            get() {
                if (field == null) {
                    field = FurnitureManager()
                }
                return field
            }
            private set
        private val plugin: FurnitureEngine? = FurnitureEngine.Companion.instance
    }
}

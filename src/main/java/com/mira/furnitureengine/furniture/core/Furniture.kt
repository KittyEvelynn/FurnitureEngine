package com.mira.furnitureengine.furniture.core

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.events.FurnitureBreakEvent
import com.mira.furnitureengine.events.FurniturePlaceEvent
import com.mira.furnitureengine.furniture.FurnitureManager
import com.mira.furnitureengine.furniture.functions.FunctionManager
import com.mira.furnitureengine.furniture.functions.FunctionType
import com.mira.furnitureengine.utils.FormatUtils
import com.mira.furnitureengine.utils.Utils
import com.mira.furnitureengine.utils.Utils.angleToRotation
import com.mira.furnitureengine.utils.Utils.rotationToAngle
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.round

@OptIn(ExperimentalStdlibApi::class)
class Furniture(/* Basic Furniture Data */
                val id: String
) {
    enum class RotSides {
        FOUR_SIDED,
        EIGHT_SIDED,
        UNLIMITED;

        companion object {
            fun valueOf(rotation: Int): RotSides? {
                return when (rotation) {
                    4 -> FOUR_SIDED
                    8 -> EIGHT_SIDED
                    16 -> UNLIMITED
                    else -> null
                }
            }
        }
        fun getSnappedAngle(angle: Float): Float{
            return when(this){
                FOUR_SIDED -> round(angle/90)*90
                EIGHT_SIDED -> round(angle/45)*45
                UNLIMITED -> angle
            }
        }
    }

    /* Generated itemstacks */
    var generatedItem: ItemStack? = null
        private set
    var blockItem: ItemStack? = null
        private set
    private var generatedDropItem: ItemStack? = null
    private val material: Material
    var displayName: String? = null
    var lore: List<String?>? = null
    val modelData: Int
    val rotationSides: RotSides?

    /* Advanced Furniture Data */
    val subModels: MutableList<SubModel> = ArrayList()
    private val functions = HashMap<FunctionType, MutableList<HashMap<String, Any?>>>()

    // I am aware that the thing above looks awful, but if you take a look at the code below, you'll see why I did it this way
    init {

        // Manually get all other values from config
        val tempDisplayName = plugin!!.config.getString("Furniture.$id.display")
        displayName = if (tempDisplayName != null) {
            FormatUtils.format(tempDisplayName)
        } else {
            null
        }
        val tempLore = plugin!!.config.getStringList("Furniture.$id.lore")
        lore = if (tempLore.isNotEmpty()) {
            FormatUtils.format(tempLore)
        } else {
            null
        }
        material = Material.valueOf(plugin!!.config.getString("Furniture.$id.item")!!)
        modelData = plugin!!.config.getInt("Furniture.$id.model_data")
        // If modelData is 0, throw an error
        if (modelData == 0) {
            plugin!!.logger.warning("Model data for furniture $id is 0. This is not allowed.")
            throw IllegalArgumentException("Model data for furniture $id is 0. This is not allowed.")
        }

        rotationSides = RotSides.valueOf(plugin!!.config.getInt("Furniture.$id.rotation"))

        // Get all submodels (object list)
        try {
            for (obj in plugin!!.config.getList("Furniture.$id.submodels", ArrayList<Any>())!!) {

                // Example format: {offset={x=1, y=0, z=0}, model_data=2}
                if (obj is Map<*, *>) {
                    var offset: Vector? = null
                    if (obj["offset"] is Map<*, *>) {
                        val offsetMap = obj["offset"] as Map<*, *>
                        offset = Vector(
                            if (offsetMap["x"] is Number) offsetMap["x"] as Int else 0,
                            if (offsetMap["y"] is Number) offsetMap["y"] as Int else 0,
                            if (offsetMap["z"] is Number) offsetMap["z"] as Int else 0
                        )
                    }
                    val modelData = if (obj["model_data"] is Number) obj["model_data"] as Int else 0
                    require(!(offset == null || offset.lengthSquared() == 0.0)) { "Offset for a submodel of furniture $id is null or 0. This is not allowed." }
                    require(modelData != 0) { "Model data for a submodel of furniture $id is 0. This is not allowed." }
                    subModels.add(SubModel(offset, modelData))
                }
            }
        } catch (e: Exception) {
            plugin!!.logger.warning("Failed to load submodels for furniture " + id + ". Error: " + e.message)
            throw IllegalArgumentException("Failed to load submodels for furniture " + id + ". Error: " + e.message)
        }
        if (subModels.isNotEmpty()) {
            if (!Utils.onlyVertical(subModels)) {
                require(rotationSides == RotSides.FOUR_SIDED) { "Furniture $id has 8 sided rotation, but has horizontal submodels. This is not allowed." }
            }
        }

        // And now get all functions
        try {
            for (type in FunctionType.entries) {
                for (obj in plugin!!.config.getList(
                    "Furniture." + id + ".functions." + type.name.uppercase(Locale.getDefault()),
                    ArrayList<Any>()
                )!!) {
                    if (obj is Map<*, *>) {
                        // it contains a type ("type") and multiple arguments, which are stored in a map ("args")
                        val functionType: String
                        if (obj["type"] is String) {
                            functionType = obj["type"] as String
                        }
                        else {
                            throw NullPointerException("Function type for furniture $id is null. This is not allowed.")
                        }

                        val args = HashMap<String, Any?>()
                        args["type"] =
                            functionType // Why not put it in the for below? well for error handling, of course
                        for ((key, value) in obj) {
                            if (key == "type") continue
                            args[key.toString()] = value
                        }
                        if (!functions.containsKey(type)) {
                            val list = ArrayList<HashMap<String, Any?>>()
                            list.add(args)
                            functions[type] = list
                        } else {
                            functions[type]!!.add(args)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            plugin!!.logger.warning("Failed to load functions for furniture " + id + ". Error: " + e.message)
            throw IllegalArgumentException("Failed to load functions for furniture " + id + ". Error: " + e.message)
        }
        init()
    }

    override fun toString(): String {
        return "Furniture{" +
                "id='" + id + '\'' +
                ", modelData=" + modelData +
                '}'
    }

    val dropItem: ItemStack
        get() = generatedDropItem!!.clone()

    @Throws(IllegalArgumentException::class)
    private fun init() {
        // Generate itemstack
        generatedItem = ItemStack(material)
        generatedItem!!.amount = 1
        val meta = generatedItem!!.itemMeta
            ?: throw IllegalArgumentException("Failed to generate item for furniture $id. ItemMeta is null. (Material: $material)")
        if (displayName != null) {
            meta.setDisplayName(displayName)
        }
        if (lore != null) {
            meta.lore = lore
        }
        meta.setCustomModelData(modelData)

        // If the item is tipped arrow, hide the potion effect
        if (material == Material.TIPPED_ARROW) meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        generatedItem!!.setItemMeta(meta)


        // Load overrides (drop-item, block-item)
        if (plugin!!.config.contains("Furniture.$id.overrides")) {
            // Drop item
            if (plugin!!.config.contains("Furniture.$id.overrides.drop-item")) {
                // Priority: other furniture > custom override > default
                val dropItemId = plugin!!.config.getString("Furniture.$id.overrides.drop-item.furniture")
                if (dropItemId == null) {

                    // Get fields (item, amount, model_data, display, lore)
                    val dropItem = Material.getMaterial(
                        plugin!!.config.getString(
                            "Furniture.$id.overrides.drop-item.item",
                            material.name
                        )!!
                    )
                    val dropAmount = plugin!!.config.getInt("Furniture.$id.overrides.drop-item.amount", 1)
                    val dropModelData =
                        plugin!!.config.getInt("Furniture.$id.overrides.drop-item.model_data", modelData)
                    val dropDisplayName =
                        plugin!!.config.getString("Furniture.$id.overrides.drop-item.display", displayName)
                    val dropLore = plugin!!.config.getStringList("Furniture.$id.overrides.drop-item.lore")
                    requireNotNull(dropItem) { "Failed to generate drop item for furniture $id. Material is null." }

                    // Generate item
                    val drop = ItemStack(dropItem)
                    drop.amount = dropAmount
                    val dropMeta = drop.itemMeta
                        ?: throw IllegalArgumentException("Failed to generate drop item for furniture $id. ItemMeta is null. (Material: $dropItem)")
                    if (dropDisplayName != null) {
                        dropMeta.setDisplayName(FormatUtils.format(dropDisplayName))
                    }
                    if (dropLore.isNotEmpty()) {
                        dropMeta.lore = FormatUtils.format(dropLore)
                    }
                    if (dropModelData != 0) {
                        dropMeta.setCustomModelData(dropModelData)
                    }
                    drop.setItemMeta(dropMeta)

                    // Now set it
                    generatedDropItem = drop
                } else {
                    try {
                        generatedDropItem =
                            FurnitureManager.instance!!.getFurniture(dropItemId)!!.dropItem
                    } catch (e: Exception) {
                        plugin!!.logger.warning("Furniture with id $dropItemId does not exist. Please load it before loading $id.")
                    }
                }
            }
            if (plugin!!.config.contains("Furniture.$id.overrides.block-item")) {
                // Get fields (item, model_data, display, lore)
                val blockItem = Material.getMaterial(
                    plugin!!.config.getString(
                        "Furniture.$id.overrides.block-item.item",
                        material.name
                    )!!
                )
                val blockModelData = plugin!!.config.getInt("Furniture.$id.overrides.block-item.model_data", modelData)
                val blockDisplayName =
                    plugin!!.config.getString("Furniture.$id.overrides.block-item.display", displayName)
                val blockLore = plugin!!.config.getStringList("Furniture.$id.overrides.block-item.lore")
                requireNotNull(blockItem) { "Failed to generate block item for furniture $id. Material is null." }

                // Generate item
                val block = ItemStack(blockItem)
                block.amount = 1
                val blockMeta = block.itemMeta
                    ?: throw IllegalArgumentException("Failed to generate block item for furniture $id. ItemMeta is null. (Material: $blockItem)")
                if (blockDisplayName != null) {
                    blockMeta.setDisplayName(FormatUtils.format(blockDisplayName))
                }
                if (blockLore.isNotEmpty()) {
                    blockMeta.lore = FormatUtils.format(blockLore)
                }
                if (blockModelData != 0) {
                    blockMeta.setCustomModelData(blockModelData)
                }
                block.setItemMeta(blockMeta)

                // Now set it
                this.blockItem = block
            }
        }

        // if there is no drop item, use the generated item (and same for block item)
        if (generatedDropItem == null) {
            val drop = generatedItem!!.clone()
            drop.amount = 1
            generatedDropItem = drop
        }
        if (blockItem == null) {
            blockItem = generatedItem
        }
    }

    fun generateSubModelItem(subModel: SubModel?): ItemStack {
        val item = blockItem!!.clone()
        val meta = item.itemMeta!!
        meta.setCustomModelData(subModel?.customModelData)
        item.setItemMeta(meta)
        return item
    }

    fun place(player: Player, hand: EquipmentSlot, location: Location): Boolean {
        // Go thru all submodels and check if there is space for them
        val rotationSide = Utils.getRotation(player, rotationSides)

        // Check if the item is a tipped arrow
        val inheritColor: Boolean
        val color: Color?
        if (generatedItem!!.type == Material.TIPPED_ARROW) {
            var itemInHand = player.inventory.itemInMainHand
            if (itemInHand.type != Material.TIPPED_ARROW) {
                itemInHand = player.inventory.itemInOffHand
            }
            val potionMeta = itemInHand.itemMeta as PotionMeta?
            if (potionMeta != null) {
                if (potionMeta.hasColor()) {
                    inheritColor = true
                    color = potionMeta.color
                } else {
                    color = null
                    inheritColor = false
                }
            } else {
                color = null
                inheritColor = false
            }
        } else {
            color = null
            inheritColor = false
        }
        for (subModel in subModels) {
            val subModelLocation = Utils.getRelativeLocation(location, subModel.offset, rotationSide)
            if (Utils.isSolid(subModelLocation.block) || Utils.entityObstructing(subModelLocation)) {
                return false
            }
        }
        if (Utils.isSolid(location.block) || Utils.entityObstructing(location)) {
            return false
        }
        val blockPlaceEvent = BlockPlaceEvent(
            location.block,
            location.block.state,
            location.block.getRelative(BlockFace.UP),
            blockItem!!,
            player,
            true,
            hand
        )
        plugin!!.server.pluginManager.callEvent(blockPlaceEvent)
        if (blockPlaceEvent.isCancelled) {
            return false
        }
        val event = FurniturePlaceEvent(this, player, location)
        plugin!!.server.pluginManager.callEvent(event)
        if (event.isCancelled) {
            return false
        }

        // Set a barrier block at the location
        location.block.type = Material.AIR
        // Spawn an item display at the location
        val itemDisplay = location.world!!.spawn(location, ItemDisplay::class.java) { display: ItemDisplay ->
            // Set the item display's item to the generated item
            if (!inheritColor) {
                display.itemStack = blockItem!!
            } else {
                val item = blockItem!!.clone()
                val potionMeta = item.itemMeta as PotionMeta?
                potionMeta?.let{
                    potionMeta.color = color
                    item.setItemMeta(potionMeta)
                }
                display.itemStack = item
            }

            display.setRotation(rotationSides!!.getSnappedAngle(player.location.yaw), 0f)

            display.persistentDataContainer.set(
                NamespacedKey(
                    JavaPlugin.getPlugin(
                        FurnitureEngine::class.java
                    ), "format"
                ), PersistentDataType.INTEGER, Utils.furnitureFormatVersion
            )
        }
        location.block.type = Material.BARRIER

        // Now go thru all submodels and place them
        for (subModel in subModels) {
            val subModelLocation = Utils.getRelativeLocation(location, subModel.offset, rotationSide)
            subModelLocation.block.type = Material.AIR
            val subModelItemDisplay = subModelLocation.world!!
                .spawn(subModelLocation, ItemDisplay::class.java) { display: ItemDisplay ->
                    if (!inheritColor) display.itemStack = generateSubModelItem(subModel) else {
                        val item = generateSubModelItem(subModel).clone()
                        val potionMeta = item.itemMeta as PotionMeta?
                        potionMeta?.let{
                            potionMeta.color = color
                            item.setItemMeta(potionMeta)
                        }
                        display.itemStack = item
                    }

                    display.setRotation(rotationSides!!.getSnappedAngle(player.location.yaw), 0f)

                    display.persistentDataContainer.set(
                        NamespacedKey(
                            JavaPlugin.getPlugin(
                                FurnitureEngine::class.java
                            ), "format"
                        ), PersistentDataType.INTEGER, Utils.furnitureFormatVersion
                    )
                }
            // TODO the code above is practicaly copy pasted lets look at if we can simplify it later
            subModelLocation.block.type = Material.BARRIER
        }

        // play placing animation & remove item from hand (if not in creative)
        if (hand == EquipmentSlot.HAND) {
            player.swingMainHand()
            if (player.gameMode != GameMode.CREATIVE) {
                player.inventory.itemInMainHand.amount = player.inventory.itemInMainHand.amount - 1
            }
        } else {
            player.swingOffHand()
            if (player.gameMode != GameMode.CREATIVE) {
                player.inventory.itemInOffHand.amount = player.inventory.itemInOffHand.amount - 1
            }
        }
        callFunction(
            FunctionType.PLACE,
            location,
            player,
            location
        )
        return true
    }

    fun spawn(location: Location, rotationSide: Rotation, color: Color?): Boolean {
        val inheritColor: Boolean = generatedItem!!.type == Material.TIPPED_ARROW && color != null
        for (subModel in subModels) {
            val subModelLocation = Utils.getRelativeLocation(location, subModel.offset, rotationSide)
            if (Utils.isSolid(subModelLocation.block)) {
                return false
            }
        }
        if (Utils.isSolid(location.block)) {
            return false
        }
        val event = FurniturePlaceEvent(this, null, location)
        plugin!!.server.pluginManager.callEvent(event)
        if (event.isCancelled) {
            return false
        }

        // Set a barrier block at the location
        location.block.type = Material.AIR
        // Spawn an item display at the location
        val itemDisplay = location.world!!.spawn(location, ItemDisplay::class.java) { display: ItemDisplay ->
            // Set the item display's item to the generated item
            if (!inheritColor) {
                display.itemStack = blockItem
            } else {
                val item = blockItem!!.clone()
                val potionMeta = item.itemMeta as PotionMeta?
                potionMeta?.let {
                    potionMeta.color = color
                    item.setItemMeta(potionMeta)
                }
                display.itemStack = blockItem
            }

            display.location.yaw = rotationSides!!.getSnappedAngle(rotationToAngle(rotationSide))
            // TODO everything to do with spawning models need to be put in a function
            display.persistentDataContainer.set(
                NamespacedKey(
                    JavaPlugin.getPlugin(
                        FurnitureEngine::class.java
                    ), "format"
                ), PersistentDataType.INTEGER, Utils.furnitureFormatVersion
            )
        }
        location.block.type = Material.BARRIER

        // Now go thru all submodels and place them
        for (subModel in subModels) {
            val subModelLocation = Utils.getRelativeLocation(location, subModel.offset, rotationSide)
            subModelLocation.block.type = Material.AIR
            val subModelItemDisplay = subModelLocation.world!!
                .spawn(subModelLocation, ItemDisplay::class.java) { display: ItemDisplay ->
                    if (!inheritColor) display.itemStack = generateSubModelItem(subModel) else {
                        val item = generateSubModelItem(subModel).clone()
                        val potionMeta = item.itemMeta as PotionMeta?
                        if (potionMeta != null) {
                            potionMeta.color = color
                            item.setItemMeta(potionMeta)
                        }
                        display.itemStack = item
                    }
                    display.persistentDataContainer.set(
                        NamespacedKey(
                            JavaPlugin.getPlugin(
                                FurnitureEngine::class.java
                            ), "format"
                        ), PersistentDataType.INTEGER, Utils.furnitureFormatVersion
                    )
                }
            subModelLocation.block.type = Material.BARRIER
        }
        return true
    }

    fun breakFurniture(player: Player?, location: Location): Boolean {
        player?.let {
            val blockBreakEvent = BlockBreakEvent(location.block, player)
            plugin!!.server.pluginManager.callEvent(blockBreakEvent)
            if (blockBreakEvent.isCancelled) {
                return false
            }
        }
        val event = FurnitureBreakEvent(this, player, location)
        plugin!!.server.pluginManager.callEvent(event)
        if (event.isCancelled) {
            return false
        }
        var rot: Rotation? = null
        var inheritColor = false
        var color = Color.WHITE
        // Destroy the initial item display + block
        for (entity in location.world!!
            .getNearbyEntities(location.add(0.5, 0.5, 0.5), 0.1, 0.1, 0.1)) {
            if (entity is ItemDisplay) {
                if (entity.getPersistentDataContainer().has(
                        NamespacedKey(
                            JavaPlugin.getPlugin<FurnitureEngine>(
                                FurnitureEngine::class.java
                            ), "format"
                        ), PersistentDataType.INTEGER
                    )
                ) {
                    if (entity.itemStack!!.type == Material.TIPPED_ARROW) {
                        val potionMeta = entity.itemStack!!.itemMeta as PotionMeta?
                        if (potionMeta != null) {
                            color = potionMeta.color
                            inheritColor = true
                        }
                    }
                    rot = angleToRotation(entity.location.yaw)
                    entity.remove()
                    location.block.type = Material.AIR
                    break
                }
            }
        }
        if(rot==null){
            return false
        }

        // Now time to destroy all submodels
        for (subModel in subModels) {
            val subModelLocation = Utils.getRelativeLocation(location, subModel.offset, rot)
            for (entity in subModelLocation.world!!
                .getNearbyEntities(subModelLocation, 0.2, 0.2, 0.2)) {
                if (entity is ItemDisplay) {
                    if (entity.getPersistentDataContainer().has(
                            NamespacedKey(
                                JavaPlugin.getPlugin(
                                    FurnitureEngine::class.java
                                ), "format"
                            ), PersistentDataType.INTEGER
                        )
                    ) {
                        entity.remove()
                        subModelLocation.block.type = Material.AIR
                        break
                    }
                }
            }
        }
        player?.let {
            callFunction(
                FunctionType.BREAK,
                location,
                player,
                location
            )

            // If the player isn't in creative, drop the item
            if (player.gameMode != GameMode.CREATIVE && event.isDroppingItems) {
                if (!inheritColor) location.world!!.dropItemNaturally(location, dropItem) else {
                    val item = dropItem
                    val potionMeta = item.itemMeta as PotionMeta?
                    if (potionMeta != null) {
                        potionMeta.color = color
                        item.setItemMeta(potionMeta)
                    }
                    location.world!!.dropItemNaturally(location, item)
                }
            }
        }
        return true
    }

    fun callFunction(
        type: FunctionType,
        clickedLocation: Location?,
        interactingPlayer: Player?,
        originLocation: Location?
    ): Boolean {
        if (!functions.containsKey(type)) return false
        val funList: List<HashMap<String, Any?>> = functions[type]!!
        for (args in funList) {
            FunctionManager.instance!!.call(
                args["type"].toString(),
                args,
                interactingPlayer,
                this,
                clickedLocation,
                originLocation
            )
        }
        return true
    }

    companion object {
        var plugin: FurnitureEngine? = FurnitureEngine.instance
    }
}

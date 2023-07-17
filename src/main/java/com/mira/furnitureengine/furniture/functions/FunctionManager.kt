package com.mira.furnitureengine.furniture.functions

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.furniture.functions.internal.CommandFunction
import com.mira.furnitureengine.furniture.functions.internal.SitFunction
import com.mira.furnitureengine.furniture.functions.internal.SoundFunction
import com.mira.furnitureengine.furniture.functions.internal.SwingFunction
import com.mira.furnitureengine.furniture.functions.internal.fmanip.ReplaceFunction
import org.bukkit.*
import org.bukkit.entity.Player

class FunctionManager private constructor() {
    var functions = HashMap<String?, Function>()

    init {
        register(CommandFunction())
        register(SoundFunction())
        register(SitFunction())
        register(SwingFunction())
        register(ReplaceFunction())
    }

    fun register(function: Function) {
        functions[function.type] = function
        FurnitureEngine.instance!!.logger.info("Registered function: " + function.type)
    }

    fun call(
        type: String?,
        args: HashMap<String, Any?>?,
        player: Player?,
        furniture: Furniture?,
        interactLocation: Location?,
        originLocation: Location?
    ) {
        val argsCopy = HashMap(args)
        argsCopy["player"] = player
        argsCopy["furniture"] = furniture
        argsCopy["location"] = interactLocation
        argsCopy["origin"] = originLocation
        try {
            functions[type]!!.execute(argsCopy)
        } catch (e: IllegalArgumentException) {
            FurnitureEngine.instance!!.logger.warning("Failed to execute function: " + e.message)
        }
    }

    companion object {
        var instance: FunctionManager? = null
            get() {
                if (field == null) {
                    field = FunctionManager()
                }
                return field
            }
            private set
    }
}

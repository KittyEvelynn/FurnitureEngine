package com.mira.furnitureengine.furniture.functions.internal.fmanip

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.furniture.functions.Function
import java.util.*

class MoveFunction : Function {
    override val type: String
        get() = "MOVE"

    @Throws(IllegalArgumentException::class)
    override fun execute(args: HashMap<String, Any?>) {
        // get position
        var pos = args["position"] as String?
        if (pos == null) pos = "LOCAL"
        val position: Position = try {
            Position.valueOf(pos.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            FurnitureEngine.instance!!.logger.warning("Invalid position in MOVE function: $pos")
            return
        }
    }
}

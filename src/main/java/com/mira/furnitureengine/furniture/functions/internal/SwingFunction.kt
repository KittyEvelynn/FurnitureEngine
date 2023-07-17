package com.mira.furnitureengine.furniture.functions.internal

import com.mira.furnitureengine.furniture.functions.Function
import org.bukkit.entity.Player

class SwingFunction : Function {
    override val type: String
        get() = "SWING_ARM"

    @Throws(IllegalArgumentException::class)
    override fun execute(args: HashMap<String, Any?>) {
        val player = args["player"] as Player?
        require(args.containsKey("arm")) { "Missing argument: arm" }
        val arm = args["arm"] as String?
        if (arm.equals("right", ignoreCase = true)) {
            player!!.swingMainHand()
        } else if (arm.equals("left", ignoreCase = true)) {
            player!!.swingOffHand()
        } else {
            throw IllegalArgumentException("Invalid argument: arm")
        }
    }
}

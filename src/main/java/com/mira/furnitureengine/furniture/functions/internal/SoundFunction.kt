package com.mira.furnitureengine.furniture.functions.internal

import com.mira.furnitureengine.furniture.functions.Function
import org.bukkit.Location

class SoundFunction : Function {
    override val type: String
        get() = "SOUND"

    override fun execute(args: HashMap<String, Any?>) {
        val location = args["location"] as Location?
        require(args.containsKey("sound")) { "Missing argument: sound" }
        val sound = args["sound"] as String?
        val volume: Float = if (args["volume"] == null) 1f else (args["volume"] as Double?)!!.toFloat()
        val pitch: Float = if (args["pitch"] == null) 1f else (args["pitch"] as Double?)!!.toFloat()
        try {
            location!!.world!!.playSound(location, sound!!, volume, pitch)
        } catch (ignored: Exception) {
        }
    }
}

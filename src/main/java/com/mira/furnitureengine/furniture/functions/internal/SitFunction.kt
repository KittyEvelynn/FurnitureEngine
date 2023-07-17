package com.mira.furnitureengine.furniture.functions.internal

import com.mira.furnitureengine.furniture.functions.Function
import com.ranull.sittable.Sittable
import dev.geco.gsit.api.GSitAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class SitFunction : Function {
    override val type: String
        get() = "CHAIR"

    override fun execute(args: HashMap<String, Any?>) {
        val player = args["player"] as Player?
        val location = args["location"] as Location?
        val yOffset: Float = if (args["y-offset"] == null) 0f else (args["y-offset"] as Double?)!!.toFloat()
        val pm = Bukkit.getServer().pluginManager
        if (pm.getPlugin("GSit") != null) {
            if (GSitAPI.getSeats(location!!.block).size == 0) {
                GSitAPI.createSeat(location.block, player!!, true, 0.0, yOffset.toDouble(), 0.0, 0f, true)
            }
        } else if (pm.getPlugin("Sittable") != null) {
            if (!Sittable.isBlockOccupied(location!!.block)) {
                Sittable.sitOnBlock(player, location.block, 0.0, yOffset.toDouble(), 0.0, player!!.facing.oppositeFace)
            }
        } else {
            throw IllegalArgumentException("Missing sit plugin. Please install either GSit or Sittable.")
        }
    }
}

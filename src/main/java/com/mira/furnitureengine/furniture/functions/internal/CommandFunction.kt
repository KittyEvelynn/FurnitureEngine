package com.mira.furnitureengine.furniture.functions.internal

import com.mira.furnitureengine.furniture.core.Furniture
import com.mira.furnitureengine.furniture.functions.Function
import org.bukkit.*
import org.bukkit.entity.Player

class CommandFunction : Function {
    override val type: String
        get() = "COMMAND"

    @Throws(IllegalArgumentException::class)
    override fun execute(args: HashMap<String, Any?>) {
        require(args.containsKey("command")) { "Missing argument: command" }
        var command = args["command"] as String?

        // placeholders
        command = command!!
            .replace("%player%", (args["player"] as Player?)!!.name)
            .replace("%furniture%", (args["furniture"] as Furniture?)!!.id)
            .replace(
                "%location%",
                (args["location"] as Location?)!!.x.toString() + " " + (args["location"] as Location?)!!.y + " " + (args["location"] as Location?)!!.z
            )
            .replace("%world%", (args["location"] as Location?)!!.world!!.name)
            .replace("%location_x%", (args["location"] as Location?)!!.x.toString() + "")
            .replace("%location_y%", (args["location"] as Location?)!!.y.toString() + "")
            .replace("%location_z%", (args["location"] as Location?)!!.z.toString() + "")
            .replace(
                "%origin%",
                (args["origin"] as Location?)!!.x.toString() + " " + (args["origin"] as Location?)!!.y + " " + (args["origin"] as Location?)!!.z
            )
            .replace("%origin_x%", (args["origin"] as Location?)!!.x.toString() + "")
            .replace("%origin_y%", (args["origin"] as Location?)!!.y.toString() + "")
            .replace("%origin_z%", (args["origin"] as Location?)!!.z.toString() + "")
        val player = args["player"] as Player?
        val isOperator = player!!.isOp
        if (command.startsWith("[op]")) {
            try {
                player.isOp = true
                player.performCommand(command.substring(4))
            } finally {
                player.isOp = isOperator
            }
        } else if (command.startsWith("[console]")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().consoleSender, command.substring(9))
        } else {
            player.performCommand(command)
        }
    }
}
package com.mira.furnitureengine.commands

import com.mira.furnitureengine.furniture.FurnitureManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CoreCommandTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            val autoCompletion: MutableList<String> = ArrayList()
            autoCompletion.add("reload")
            autoCompletion.add("give")
            autoCompletion.add("get")
            autoCompletion.add("execute")
            autoCompletion.add("reload")
            autoCompletion.add("paint")
            return autoCompletion
        }
        if (args[0] == "give") {
            var autoCompletion: MutableList<String> = ArrayList()
            if (args.size == 2) {
                for (p in Bukkit.getOnlinePlayers()) {
                    autoCompletion.add(p.name)
                }
                return autoCompletion
            } else if (args.size == 3) {
                autoCompletion = FurnitureManager.instance!!.ids
                return autoCompletion
            } else if (args.size == 4) {
                autoCompletion.add("1")
                return autoCompletion
            }
            return autoCompletion
        }
        if (args[0] == "get") {
            var autoCompletion: MutableList<String> = ArrayList()
            if (args.size == 2) {
                autoCompletion = FurnitureManager.instance!!.ids
                return autoCompletion
            } else if (args.size == 3) {
                autoCompletion.add("1")
                return autoCompletion
            }
            return autoCompletion
        }
        if (args[0] == "paint") {
            val autoCompletion: MutableList<String> = ArrayList()
            autoCompletion.add("#")
            return autoCompletion
        }
        if (args[0] == "execute") {
            val autoCompletion: MutableList<String> = ArrayList()
            autoCompletion.add("set")
            autoCompletion.add("remove")
            autoCompletion.add("replace")
            autoCompletion.add("rotate")
            autoCompletion.add("move")
            return autoCompletion
        }
        if (args[0] == "reload") {
            val autoCompletion: MutableList<String> = ArrayList()
            autoCompletion.add("furniture")
            autoCompletion.add("config")
            autoCompletion.add("all")
            return autoCompletion
        }
        return null
    }
}

package com.mira.furnitureengine.commands

import com.mira.furnitureengine.FurnitureEngine
import com.mira.furnitureengine.furniture.FurnitureManager
import com.mira.furnitureengine.furniture.core.Furniture
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.PotionMeta

class CoreCommand : CommandExecutor {
    var plugin: FurnitureEngine? = FurnitureEngine.instance!!
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (args.size == 0) {
            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Incorrect Command usage!")
            return true
        } else {
            /* Reload */
            if (args[0] == "reload") {
                reload(sender, if (args.size >= 2) args[1] else "")
                return true
            }
            /* Give */if (args[0] == "give") {
                if (args.size == 1) {
                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Incorrect Command usage!")
                    return false
                } else {
                    if (args.size == 2) {
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Incorrect Command usage!")
                        return false
                    } else if (args.size == 3) {
                        val target = plugin!!.server.getPlayer(args[1])
                        if (target == null) {
                            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Player not found!")
                            return false
                        } else {
                            val furniture: Furniture? = FurnitureManager.instance!!.getFurniture(args[2])
                            if (furniture != null) {
                                val item = furniture.generatedItem
                                if (item != null) {
                                    if (item.type == Material.TIPPED_ARROW) {
                                        val meta = item.itemMeta as PotionMeta?
                                        meta!!.color = Color.WHITE
                                        item.setItemMeta(meta)
                                    }
                                    target.inventory.addItem(item)
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Given " + ChatColor.YELLOW + furniture.id + ChatColor.GREEN + " to " + ChatColor.YELLOW + target.name + ChatColor.GREEN + "!")
                                } else {
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Failed to generate item!")
                                }
                            } else {
                                sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Furniture not found!")
                            }
                        }
                    } else if (args.size == 4) {
                        val target = plugin!!.server.getPlayer(args[1])
                        if (target == null) {
                            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Player not found!")
                            return false
                        } else {
                            val furniture: Furniture? = FurnitureManager.instance!!.getFurniture(args[2])
                            if (furniture != null) {
                                val item = furniture.generatedItem
                                if (item != null) {
                                    if (item.type == Material.TIPPED_ARROW) {
                                        val meta = item.itemMeta as PotionMeta?
                                        meta!!.color = Color.WHITE
                                        item.setItemMeta(meta)
                                    }
                                    val amount = args[3].toInt()
                                    if (amount > 0) {
                                        item.amount = amount
                                        target.inventory.addItem(item)
                                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Given " + ChatColor.YELLOW + furniture.id + ChatColor.GREEN + " to " + ChatColor.YELLOW + target.name + ChatColor.GREEN + "!")
                                    } else {
                                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid amount!")
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Failed to generate item!")
                                }
                            } else {
                                sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Furniture not found!")
                            }
                        }
                    }
                }
                return true
            }
            if (args[0] == "get") {
                if (args.size == 1) {
                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Incorrect Command usage!")
                    return false
                } else {
                    if (sender is Player) {
                        if (args.size == 2) {
                            val furniture: Furniture? = FurnitureManager.instance!!.getFurniture(args[1])
                            if (furniture != null) {
                                val item = furniture.generatedItem
                                if (item != null) {
                                    if (item.type == Material.TIPPED_ARROW) {
                                        val meta = item.itemMeta as PotionMeta?
                                        meta!!.color = Color.WHITE
                                        item.setItemMeta(meta)
                                    }
                                    sender.inventory.addItem(item)
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Given " + ChatColor.YELLOW + furniture.id + ChatColor.GREEN + " to " + ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + "!")
                                } else {
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Failed to generate item!")
                                }
                            } else {
                                sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid furniture!")
                            }
                        } else if (args.size == 3) {
                            val furniture: Furniture? = FurnitureManager.instance!!.getFurniture(args[1])
                            if (furniture != null) {
                                val item = furniture.generatedItem
                                if (item != null) {
                                    if (item.type == Material.TIPPED_ARROW) {
                                        val meta = item.itemMeta as PotionMeta?
                                        meta!!.color = Color.WHITE
                                        item.setItemMeta(meta)
                                    }
                                    val amount = args[2].toInt()
                                    if (amount > 0) {
                                        item.amount = amount
                                        sender.inventory.addItem(item)
                                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Given " + ChatColor.YELLOW + furniture.id + ChatColor.GREEN + " to " + ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + "!")
                                    } else {
                                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid amount!")
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Failed to generate item!")
                                }
                            } else {
                                sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid furniture!")
                            }
                        }
                    }
                }
                return true
            } else if (args[0] == "paint") {
                if (args.size == 1) {
                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Incorrect Command usage!")
                    return false
                }
                var item = (sender as Player).inventory.itemInMainHand
                if (!item.hasItemMeta()) {
                    item = sender.inventory.itemInOffHand
                    if (!item.hasItemMeta()) {
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "You must hold an item!")
                        return true
                    }
                }
                if (item.type == Material.TIPPED_ARROW) {
                    // Get hex color from the second argument
                    var hex = args[1]
                    val meta = item.itemMeta as PotionMeta?
                    hex = if (hex.startsWith("#")) {
                        hex.substring(1)
                    } else {
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid hex color!")
                        return true
                    }
                    if (hex.length != 6) {
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid hex color!")
                        return true
                    }
                    try {
                        val r = hex.substring(0, 2).toInt(16)
                        val g = hex.substring(2, 4).toInt(16)
                        val b = hex.substring(4, 6).toInt(16)
                        meta!!.color = Color.fromRGB(r, g, b)
                        item.setItemMeta(meta)
                        sender.playSound(sender.location, Sound.ENTITY_DOLPHIN_SPLASH, 1f, 1f)
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Painted item!")
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid hex color!")
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "This item is not paintable! (Tipped Arrow)")
                }
            }
        }
        return false
    }

    fun reload(sender: CommandSender, arg: String) {
        // No label - Reload all
        // furniture - Reload furniture
        // config - Reload config
        // all - Reload all
        if (arg.isEmpty() || arg.equals("all", ignoreCase = true)) {
            plugin!!.reloadConfig()
            FurnitureManager.instance!!.reloadFurniture()
            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Reloaded furniture + config!")
        } else if (arg.equals("furniture", ignoreCase = true)) {
            FurnitureManager.instance!!.reloadFurniture()
            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Reloaded furniture!")
        } else if (arg.equals("config", ignoreCase = true)) {
            plugin!!.reloadConfig()
            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.GREEN + "Reloaded config!")
        } else {
            sender.sendMessage(ChatColor.GOLD.toString() + "Furniture" + ChatColor.YELLOW + "Engine" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + "Invalid argument!")
        }
    }
}

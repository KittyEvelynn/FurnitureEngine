package com.mira.furnitureengine

import com.mira.furnitureengine.commands.CoreCommand
import com.mira.furnitureengine.commands.CoreCommandTabCompleter
import com.mira.furnitureengine.furniture.FurnitureManager
import com.mira.furnitureengine.furniture.functions.FunctionManager
import com.mira.furnitureengine.listeners.PlayerInteractListener
import com.mira.furnitureengine.utils.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class FurnitureEngine : JavaPlugin() {
    override fun onEnable() {
        instance = this
        logger.info("FurnitureEngine has been enabled!")

        // Load config
        loadConfig()

        // Static access... doing this so that it instantiates the classes
        FurnitureManager.instance
        FunctionManager.instance
        if (this.config.getBoolean("Options.checkForUpdates")) {
            UpdateChecker(97134).getVersion { version: String ->
                if (description.version.equals(version, ignoreCase = true)) {
                    logger.info("You are running the latest version of FurnitureEngine!")
                } else {
                    logger.info("There is a new update available for FurnitureEngine!" + " (Current version: " + description.version + " New version: " + version + ")")
                }
            }
        }

        // Register commands
        getCommand("furnitureengine")!!.setExecutor(CoreCommand())
        getCommand("furnitureengine")!!.tabCompleter = CoreCommandTabCompleter()

        // Register events
        server.pluginManager.registerEvents(PlayerInteractListener(), this)

        // Register metrics
        // Metrics(this, 13146) TODO fix this when i feel like it
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun loadConfig() {
        config.options().copyDefaults(true)
        saveConfig()
    }

    companion object {
        var instance: FurnitureEngine? = null
            private set
    }
}

package com.mira.furnitureengine.utils

import com.mira.furnitureengine.FurnitureEngine
import org.bukkit.Bukkit
import org.bukkit.util.Consumer
import java.io.IOException
import java.net.URL
import java.util.*

//@JvmRecord
data class UpdateChecker(val resourceId: Int) {
    fun getVersion(consumer: Consumer<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(FurnitureEngine.instance!!, Runnable {
            try {
                URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId").openStream()
                    .use { inputStream ->
                        Scanner(inputStream).use { scanner ->
                            if (scanner.hasNext()) {
                                consumer.accept(scanner.next())
                            }
                        }
                    }
            } catch (exception: IOException) {
                FurnitureEngine.instance!!.logger
                    .info("Unable to check for updates: " + exception.message)
            }
        })
    }
}
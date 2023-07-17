package com.mira.furnitureengine.utils

import org.bukkit.ChatColor
import java.util.regex.Pattern

object FormatUtils {
    /**
     * This file defines colors,
     * and gradients for item names.
     */
    const val COLOR_CHAR = ChatColor.COLOR_CHAR
    fun format(input: String?): String {
        return translateHexColorCodes("&#", ChatColor.translateAlternateColorCodes('&', input!!))
    }

    fun format(input: List<String?>): List<String> {
        val output: MutableList<String> = ArrayList()
        for (string in input) {
            output.add(format(string))
        }
        return output
    }

    // Custom Hex Colors
    fun translateHexColorCodes(startTag: String, message: String): String {
        val hexPattern = Pattern.compile("$startTag([A-Fa-f0-9]{6})")
        val matcher = hexPattern.matcher(message)
        val buffer = StringBuilder(message.length + 4 * 8)
        while (matcher.find()) {
            val group = matcher.group(1)
            matcher.appendReplacement(
                buffer, COLOR_CHAR.toString() + "x"
                        + COLOR_CHAR + group[0] + COLOR_CHAR + group[1]
                        + COLOR_CHAR + group[2] + COLOR_CHAR + group[3]
                        + COLOR_CHAR + group[4] + COLOR_CHAR + group[5]
            )
        }
        return matcher.appendTail(buffer).toString()
    }
}

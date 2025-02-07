package com.mira.furnitureengine.furniture.core

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.util.Vector

// Submodel contains vector (offset) and custom model data (int)
class SubModel(val offset: Vector, val customModelData: Int, val rotation: Int) : ConfigurationSerializable {

    override fun serialize(): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["offset"] = offset
        map["model_data"] = customModelData
        return map
    }

    override fun toString(): String {
        return "SubModel{" +
                "offset=" + offset +
                ", model_data=" + customModelData +
                '}'
    }

    companion object {
        fun deserialize(map: Map<String?, Any?>): SubModel {
            return SubModel(map["offset"] as Vector, map["model_data"] as Int, map["rotation"] as Int)
        }
    }
}
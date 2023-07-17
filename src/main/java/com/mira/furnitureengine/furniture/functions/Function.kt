package com.mira.furnitureengine.furniture.functions

interface Function {
    val type: String

    @Throws(IllegalArgumentException::class)
    fun execute(args: HashMap<String, Any?>)
}

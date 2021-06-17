package dev.moru3.minepie.map.interfaces

import org.bukkit.entity.Player

interface CustomMap {
    val mapRenderer: CustomMapRenderer

    fun addPlayer(player: Player)

    fun removePlayer(player: Player)
}
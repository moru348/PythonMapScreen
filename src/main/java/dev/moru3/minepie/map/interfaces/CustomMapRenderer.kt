package dev.moru3.minepie.map.interfaces

import dev.moru3.pythonmapscreen.map.MapCursor

interface CustomMapRenderer {
    fun renderer(canvas: CustomMapCanvas, cursors: Set<MapCursor>)
}
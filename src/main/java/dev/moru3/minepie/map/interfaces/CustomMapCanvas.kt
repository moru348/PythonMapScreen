package dev.moru3.minepie.map.interfaces

import java.awt.Color
import java.awt.image.BufferedImage

interface CustomMapCanvas {
    fun setPixel(x: Int, y: Int, color: Color)

    fun setPixel(x: Int, y: Int, color: Byte)

    fun setPixel(x: Int, y: Int, color: Int)

    fun getPixel(x: Int, y: Int): Byte

    fun getPixelByColor(x: Int, y: Int): Color

    fun getPixelByArgb(x: Int, y: Int): Int

    fun asImage(): BufferedImage

    fun asByteArray(): ByteArray

    fun clone(): CustomMapCanvas
}
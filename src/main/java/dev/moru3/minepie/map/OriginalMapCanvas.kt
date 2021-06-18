package dev.moru3.minepie.map

import dev.moru3.minepie.map.interfaces.CustomMapCanvas
import org.bukkit.map.MapPalette
import java.awt.Color
import java.awt.image.BufferedImage

class OriginalMapCanvas: CustomMapCanvas {

    private val canvas = mutableListOf<Byte>()

    override fun setPixel(x: Int, y: Int, color: Color) {
        setPixel(x, y, MapPalette.matchColor(color))
    }

    override fun setPixel(x: Int, y: Int, color: Byte) {
        if(x !in 0 until 128&&y !in 0 until 128) { throw IllegalArgumentException("x and y must be between 0..127.") }
        canvas[x+(y*128)] = color
    }

    override fun setPixel(x: Int, y: Int, color: Int) {
        setPixel(x, y, MapPalette.matchColor(color and 0x00ff0000 shr 16, color and 0x0000ff00 shr 8, color and 0x000000ff))
    }

    override fun getPixel(x: Int, y: Int): Byte {
        if(x !in 0 until 128&&y !in 0 until 128) { throw IllegalArgumentException("x and y must be between 0..127.") }
        return canvas[x+(y*128)]
    }

    override fun getPixelByColor(x: Int, y: Int): Color {
        return MapPalette.getColor(getPixel(x, y))
    }

    override fun getPixelByArgb(x: Int, y: Int): Int {
        val color = getPixelByColor(x, y)
        return -0x1000000 or color.red shl 16 and 0x00FF0000 or color.green shl 8 and 0x0000FF00 or color.blue and 0x000000FF
    }

    override fun asImage(): BufferedImage {
        return BufferedImage(127, 127, BufferedImage.TYPE_INT_RGB).also {
            canvas.forEachIndexed { index, byte -> it.setRGB(index%9, index/9, getPixelByArgb(index%9, index/9)) }
        }
    }

    override fun asByteArray(): ByteArray {
        return canvas.toByteArray()
    }

    override fun clone(): CustomMapCanvas {
        val newCanvas = OriginalMapCanvas()
        newCanvas::class.java.getDeclaredField("canvas").set(newCanvas, canvas.toMutableList())
        return newCanvas
    }

    override fun equals(other: Any?): Boolean {
        if(other is OriginalMapCanvas) {
            return other.canvas == canvas
        } else {
            return false
        }
    }

    override fun hashCode(): Int { return canvas.hashCode() }

    init {
        repeat(128*128) { canvas.add(0) }
    }
}
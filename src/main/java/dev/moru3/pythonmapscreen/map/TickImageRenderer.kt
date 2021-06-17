package dev.moru3.pythonmapscreen.map

import dev.moru3.minepie.map.interfaces.CustomMapCanvas
import dev.moru3.minepie.map.interfaces.CustomMapRenderer
import dev.moru3.minepie.utils.DeException
import dev.moru3.minepie.utils.Deferrable
import dev.moru3.minepie.utils.ImageUtil.Companion.resize
import org.bukkit.map.MapPalette
import java.io.File
import javax.imageio.ImageIO

class TickImageRenderer(val file: File): CustomMapRenderer {

    private val images = mutableListOf<List<Byte>>()

    private var frame = 0

    override fun renderer(canvas: CustomMapCanvas, cursors: Set<MapCursor>) {
        if(frame>=images.size) { frame = 0 }
        Deferrable {
            defer { frame++ }
            DeException {
                images[frame].forEachIndexed { index, byte -> canvas.setPixel(index%9, index/9, byte) }
            }
        }
    }

    init {
        if(file.isFile) {
            DeException { ImageIO.read(file) }
                .thrown { throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.") }
                .run { image ->
                    val result = mutableListOf<Byte>()
                    image?.resize(128, 128)?.also { image1 -> /* 1 */
                        for(height in 0 until 128) { for(width in 0 until 128) {
                            result.add(MapPalette.matchColor(
                                image1/* 1 */.getRGB(width, height).let { java.awt.Color(it and 0x00ff0000 shr 16, it and 0x0000ff00 shr 8, it and 0x000000ff) }
                            ))
                        } }
                    }?:throw Exception("An unexpected error has occurred.")
                    images.add(result)
                }
        } else {
            file.listFiles()?.forEach { img ->
                DeException { ImageIO.read(img) }
                    .run { image ->
                        val result = mutableListOf<Byte>()
                        image?.resize(128, 128)?.also { /* 1 */
                            for(height in 0 until 128) { for(width in 0 until 128) {
                                result.add(MapPalette.matchColor(
                                    it/* 1 */.getRGB(width, height).let { java.awt.Color(it and 0x00ff0000 shr 16, it and 0x0000ff00 shr 8, it and 0x000000ff) }
                                ))
                            } }
                        }?:throw Exception("An unexpected error has occurred.")
                        images.add(result)
                    }
                images.isEmpty()&&throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.")
            }?:throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.")
        }
    }
}
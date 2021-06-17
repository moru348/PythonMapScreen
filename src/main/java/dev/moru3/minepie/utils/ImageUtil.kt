package dev.moru3.minepie.utils

import java.awt.RenderingHints
import java.awt.image.BufferedImage

class ImageUtil {
    companion object {
        fun BufferedImage.resize(newWidth: Int, newHeight: Int): BufferedImage {
            return BufferedImage(newWidth, newHeight, this.type).apply {
                createGraphics().also {
                    it.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                    it.drawImage(this, 0, 0, newWidth, newHeight, 0, 0, this.width, this.height, null)
                    it.dispose()
                } }
        }
    }
}
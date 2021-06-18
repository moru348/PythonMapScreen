package dev.moru3.pythonmapscreen.map

import dev.moru3.minepie.utils.DeException
import org.bukkit.Bukkit
import org.bukkit.Location
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

class ScreenManager(pos1: Location, pos2: Location, val file: File, val frameRate: Int = 10, val direction: Direction) {

    val world = pos1.world?:throw NullPointerException("pos1.world is null!")

    val pos1: Location = pos1.clone()
    val pos2: Location = pos2.clone()

    val blockHeight: Int
    val blockWidth: Int

    val frames = mutableListOf<List<Byte>>()

    init {
        //PrimaryThreadじゃない場合はthrow
        if(!Bukkit.isPrimaryThread()) { throw Exception("RUn it from Bukkit's Primary Thread.") }
        //フレームレートは1以上かつ20以下でない場合はthrow
        if(frameRate !in 1..20) { throw IllegalArgumentException("The frame rate must be greater than or equal to 1 and less than or equal to 20.") }
        //pos1とpos2をソート
        this.pos1.also { it.x=min(pos1.x, pos2.x);it.y=min(pos1.y, pos2.y);it.z=min(pos1.z, pos2.z) }
        this.pos2.also { it.x=max(pos1.x, pos2.x);it.y=max(pos1.y, pos2.y);it.z=max(pos1.z, pos2.z) }
        //pos1とpos2のワールドが違う場合はthrow
        if(this.pos1.world!=this.pos2.world) { throw IllegalArgumentException("pos1 and pos2 must be in the same world.") }
        //x,y,zのどれか一つのみ0ではないばあいはthrow
        if(listOf(this.pos1.blockX-this.pos2.blockX,this.pos1.blockZ-this.pos2.blockZ,this.pos1.blockY-this.pos2.blockY)
                .map(0::equals).filter(true::equals).size!=1) { throw IllegalArgumentException("For pos1 and pos2, any of x, y, or z must be 0!") }

        //directionの情報が正しいか、確かめた上、heightとwidthを設定。
        when {
            this.pos2.blockZ-this.pos1.blockZ==0 -> {
                if(direction !in listOf(Direction.NORTH, Direction.SOUTH)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = this.pos2.blockX - this.pos1.blockX
                blockWidth = this.pos2.blockY - this.pos1.blockY
            }
            this.pos2.blockX-this.pos1.blockX==0 -> {
                if(direction !in listOf(Direction.EAST, Direction.WEST)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = this.pos2.blockZ - this.pos1.blockZ
                blockWidth = this.pos2.blockY - this.pos1.blockY
            }
            else -> {
                if(direction !in listOf(Direction.DOWN, Direction.UP)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = this.pos2.blockX - this.pos1.blockX
                blockWidth = this.pos2.blockZ - this.pos1.blockZ
            }
        }

        val images = mutableListOf<BufferedImage>()
        if(file.isFile) {
            DeException { ImageIO.read(file) }
                .thrown { throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.") }
                .run(images::add)
        } else {
            file.listFiles()?.forEach { img ->
                DeException { ImageIO.read(img) }
                    .run(images::add)
            }?:throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.")
        }
        if(images.isEmpty()) { throw IllegalArgumentException("The variable file must be the directory where the image is stored, or the image.") }

        //一枚目の画像と解像度が違う場合はimagesから削除
        images.filter{ images.first().height!=it.height }
            .filter { images.first().width!=it.width }
            .forEach(images::remove)

        
    }
}

enum class Direction(val type: Byte) { EAST(1), WEST(1), NORTH(2), SOUTH(2), UP(3), DOWN(3) }
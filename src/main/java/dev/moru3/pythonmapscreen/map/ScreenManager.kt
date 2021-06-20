package dev.moru3.pythonmapscreen.map

import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.DeException
import dev.moru3.pythonmapscreen.map.Direction.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.map.MapPalette
import org.bukkit.plugin.Plugin
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

class ScreenManager(plugin: Plugin ,pos1: Location, pos2: Location, val file: File, val frameRate: Int = 10, val direction: Direction) {

    val world = pos1.world?:throw NullPointerException("pos1.world is null!")
    val itemFrames = mutableMapOf<ItemFrame, Int>()

    val pos1: Location = pos1.clone()
    val pos2: Location = pos2.clone()

    var height: Int = 0
    var width: Int = 0

    val blockHeight: Int
    val blockWidth: Int

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
                if(direction !in listOf(NORTH, SOUTH)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = (this.pos2.blockY - this.pos1.blockY)+1
                blockWidth = (this.pos2.blockX - this.pos1.blockX)+1
            }
            this.pos2.blockX-this.pos1.blockX==0 -> {
                if(direction !in listOf(EAST, WEST)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = (this.pos2.blockY - this.pos1.blockY)+1
                blockWidth = (this.pos2.blockZ - this.pos1.blockZ)+1
            }
            else -> {
                if(direction !in listOf(DOWN, UP)) {
                    throw IllegalArgumentException("pos1 pos2 and direction do not match.")
                }
                blockHeight = (this.pos2.blockX - this.pos1.blockX)+1
                blockWidth = (this.pos2.blockZ - this.pos1.blockZ)+1
            }
        }

        MultiThreadRunner {
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

            val imageHeight = images.first().height
            val imageWidth = images.first().width

            val mag: Double = ((blockHeight*128.0)/imageHeight).takeIf { imageWidth*it<=blockWidth*128.0 }?:(blockWidth*128.0)/imageWidth

            height = (imageHeight*mag).toInt()
            width = (imageWidth*mag).toInt()

            val marginTop = ((blockHeight*128)-height)/2
            val marginLeft = ((blockWidth*128)-width)/2

            val frames = mutableMapOf<Int, Image>()

            images.forEachIndexed { index, it ->
                val temp = BufferedImage(blockWidth*128, blockHeight*128, BufferedImage.TYPE_INT_ARGB).also { image ->
                    image.createGraphics().also { graphics2D ->
                        graphics2D.paint = Color.WHITE
                        graphics2D.fillRect(0, 0, image.width, image.height)
                        graphics2D.drawImage(it.getScaledInstance(width, height, Image.SCALE_DEFAULT), marginLeft, marginTop, null)
                        graphics2D.dispose()
                    } }
                frames[index] = temp
            }

            var byteFrames = mutableMapOf<Int, List<Byte>>()

            frames.keys.parallelStream().forEach {
                byteFrames[it] = MapPalette.imageToBytes(frames[it]?:return@forEach).toList()
            }

            byteFrames = byteFrames.toSortedMap()

            (0 until blockHeight).toList().parallelStream().forEach {h -> (0 until blockWidth).toList().parallelStream().forEach { w ->

                val videoFrames = mutableListOf<List<Byte>>()
                byteFrames.values.forEach {
                    val result = mutableListOf<Byte>()

                    for(wi in 0 until 128) { for(he in 0 until 128) {
                        result.add(it[w+wi+((h+he)*blockWidth*128)])//tODO
                    } }

                    videoFrames.add(result)
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    val itemFrame = world.spawnEntity(this.pos1.clone()
                        .also { when(direction) {
                            EAST -> {//TODO

                            }
                            WEST -> {

                            }
                            NORTH -> {

                            }
                            SOUTH -> {

                            }
                            UP -> TODO()
                            DOWN -> TODO()
                        } }, EntityType.ITEM_FRAME) as ItemFrame
                    itemFrame.isInvulnerable = true
                    itemFrame.itemDropChance = 0F
                    itemFrame.setFacingDirection(BlockFace.valueOf(direction.toString()))
                    val mapScreen = MapScreen(videoFrames, world, frameRate)
                    itemFrame.setItem(mapScreen.itemStack)
                    itemFrames[itemFrame] = mapScreen.mapId
                })
            } }
        }
    }
}

enum class Direction(val type: Byte) { EAST(1), WEST(1), NORTH(2), SOUTH(2), UP(3), DOWN(3) }
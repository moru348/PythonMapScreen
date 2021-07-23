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
    }
}

enum class Direction(val type: Byte) { EAST(1), WEST(1), NORTH(2), SOUTH(2), UP(3), DOWN(3) }
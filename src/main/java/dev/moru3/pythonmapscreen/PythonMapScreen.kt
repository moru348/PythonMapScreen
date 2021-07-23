package dev.moru3.pythonmapscreen

import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.thread.MultiThreadScheduler
import dev.moru3.minepie.utils.DeException
import dev.moru3.pythonmapscreen.map.Direction
import dev.moru3.pythonmapscreen.map.ScreenManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.map.MapPalette
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

class PythonMapScreen : JavaPlugin(), Listener {

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if(event.player.inventory.itemInMainHand.type!=Material.DIAMOND_HORSE_ARMOR) { return }
        when (event.action) {
            LEFT_CLICK_BLOCK -> {
                event.player.sendMessage("pos1を設定しました。")
                pos1[event.player] = event.clickedBlock?.location ?: return
            }
            RIGHT_CLICK_BLOCK -> {
                event.player.sendMessage("pos2を設定しました。")
                pos2[event.player] = event.clickedBlock?.location ?: return
            }
            else -> {
                return
            }
        }
    }

    private val pos1 = mutableMapOf<Player, Location>()
    private val pos2 = mutableMapOf<Player, Location>()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        MultiThreadScheduler.timers.forEach(MultiThreadScheduler::stop)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            if(sender !is Player) { throw IllegalArgumentException("このコマンドはプレイヤーからのみ実行できます。") }
            when(args.getOrNull(0)?:return true) {
                "start" -> {
                    ScreenManager(this, pos1[sender]?:return true, pos2[sender]?:return true, dataFolder.resolve("rts_logo_10fps"), 10, Direction.EAST)
                }
                "convert" -> {

                    val file: File = dataFolder.resolve(args.getOrNull(1)?:throw IllegalArgumentException("file pathをargs1に設定してください。"))
                    MultiThreadRunner {
                        val blockHeight = 9
                        val blockWidth = 16
                        var height: Int = 0
                        var width: Int = 0
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

                        frames.keys.parallelStream().forEach { byteFrames[it] = MapPalette.imageToBytes(frames[it]?:return@forEach).toList() }

                        byteFrames = byteFrames.toSortedMap()
                        // TODO この辺めんどくなってめっちゃコードかぶってるけど無視してね
                        dataFolder.resolve("${args.getOrNull(1)?:throw IllegalArgumentException("file pathをargs1に設定してください。")}.yml").createNewFile()
                        val config = YamlConfiguration.loadConfiguration(dataFolder.resolve("${args.getOrNull(1)?:throw IllegalArgumentException("file pathをargs1に設定してください。")}.yml"))
                        println(byteFrames.values.toList())
                        config.set("code", byteFrames.values.toList())
                        config.save(dataFolder.resolve("${args.getOrNull(1)?:throw IllegalArgumentException("file pathをargs1に設定してください。")}.yml"))
                    }
                }
            } } catch (e: IllegalArgumentException) {
            e.message?.also(sender::sendMessage)
        }
        return super.onCommand(sender, command, label, args)
    }
}
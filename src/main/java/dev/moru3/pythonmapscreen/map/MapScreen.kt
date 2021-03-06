package dev.moru3.pythonmapscreen.map

import dev.moru3.minepie.map.OriginalMapCanvas
import dev.moru3.minepie.map.interfaces.CustomMap
import dev.moru3.minepie.map.interfaces.CustomMapCanvas
import dev.moru3.minepie.map.interfaces.CustomMapRenderer
import dev.moru3.minepie.nms.NmsUtils.Companion.getNmsClass
import dev.moru3.minepie.nms.NmsUtils.Companion.sendPacket
import dev.moru3.minepie.utils.DeException
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import java.util.*

class MapScreen(val images: List<List<Byte>> ,val world: World,val frameRate: Int = 10): CustomMap {

    private val players = mutableSetOf<Player>()

    val mapId: Int

    private var frame = 0

    val itemStack = ItemStack(Material.FILLED_MAP).apply {
        itemMeta = (itemMeta as MapMeta).also { mapMeta ->
            mapMeta.mapView = Bukkit.createMap(world).also { mapView ->
                mapView.isLocked = true
                mapView.isTrackingPosition = false
                mapView.isUnlimitedTracking = false
                mapView.renderers.forEach(mapView::removeRenderer)
                mapId = mapView.id
            } }
    }
        get() = field.clone()

    private val timer = Timer()

    val canvas = OriginalMapCanvas()

    override val mapRenderer: CustomMapRenderer = object: CustomMapRenderer {
        override fun renderer(canvas: CustomMapCanvas, cursors: Set<MapCursor>) {
            if(images.isEmpty()) { return }
            if(images.size<=frame) { frame = 0 }
            images[frame].forEachIndexed { index, byte -> canvas.setPixel(index%128, index/128, byte) }
        }
    }

    override fun addPlayer(player: Player) { players.add(player) }

    override fun removePlayer(player: Player) { players.remove(player) }

    private var preCanvas = canvas.clone()

    init {
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                DeException {

                    if(images.isEmpty()) { return@DeException }

                    /*canvas?????????*/
                    mapRenderer.renderer(canvas, mutableSetOf())
                    /*????????????*/

                    val result = mutableListOf<Byte>()
                    var startX = 127
                    var startY = 127
                    var endX = 0
                    var endY = 0
                    startX = 0
                    startY = 0
                    endX = 128
                    endY = 128
                    result.addAll(canvas.asByteArray().toList())
                    /**if(preCanvas!=canvas) {
                        canvas.asByteArray().forEachIndexed { index, byte ->
                            if(preCanvas.asByteArray()[index]!=byte) {
                                result.add(byte)
                                (index%128).apply {
                                    takeIf{ startX>it }?.also{ startX=it }
                                    takeIf{ endX<it }?.also{ endX=it }
                                }
                                (index/128).apply {
                                    takeIf{ startY>it }?.also{ startY=it }
                                    takeIf{ endY<it }?.also{ endY=it }
                                }
                            }
                        }
                    } else { return@DeException }**/
                    preCanvas = canvas
                    val packet = getNmsClass("PacketPlayOutMap")
                        .getConstructor(Int::class.java, Byte::class.java, Boolean::class.java, Boolean::class.java, Collection::class.java, ByteArray::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java)
                        .newInstance(mapId, 0.toByte(), true, false, listOf<Any>(), result.toByteArray(), startX, startY, endX, endY)
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendPacket(packet)
                    }
                }.thrown {
                    it.printStackTrace()
                    Thread.currentThread().interrupt()
                }
            }
        }, 0, ((20/frameRate).toLong())*50)
    }
}
package dev.moru3.pythonmapscreen

import dev.moru3.minepie.thread.MultiThreadScheduler
import dev.moru3.pythonmapscreen.map.Direction
import dev.moru3.pythonmapscreen.map.ScreenManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

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
        saveResource("rts_logo_10fps", false)
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        MultiThreadScheduler.timers.forEach(MultiThreadScheduler::stop)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            if(sender !is Player) { throw IllegalArgumentException("このコマンドはプレイヤーからのみ実行できます。") }
            ScreenManager(this, pos1[sender]?:return true, pos2[sender]?:return true, dataFolder.resolve("rts_logo_10fps"), 10, Direction.EAST)
        } catch (e: IllegalArgumentException) {
            e.message?.also(sender::sendMessage)
        }
        return super.onCommand(sender, command, label, args)
    }
}
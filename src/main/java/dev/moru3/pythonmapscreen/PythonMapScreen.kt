package dev.moru3.pythonmapscreen

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class PythonMapScreen : JavaPlugin() {

    inline fun <reified E: Event> Plugin.executeEvent(crossinline runnable: (event: E) -> Unit) {
        val listener = object: Listener {
            @EventHandler
            fun event(event: E) { runnable.invoke(event) }
        }
        Bukkit.getPluginManager().registerEvents(listener, this)
    }

    private val pos1 = mutableMapOf<Player, Location>()
    private val pos2 = mutableMapOf<Player, Location>()

    override fun onEnable() {
        this.executeEvent<PlayerInteractEvent> {
            when(it.action) {
                LEFT_CLICK_BLOCK -> {
                    it.player.sendMessage("pos1を設定しました。")
                    pos1[it.player] = it.clickedBlock?.location?:return@executeEvent
                }
                RIGHT_CLICK_BLOCK -> {
                    it.player.sendMessage("pos2を設定しました。")
                    pos2[it.player] = it.clickedBlock?.location?:return@executeEvent
                }
                else -> { return@executeEvent }
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            if(sender !is Player) { throw IllegalArgumentException("このコマンドはプレイヤーからのみ実行できます。") }
            
        } catch (e: IllegalArgumentException) {
            e.message?.also(sender::sendMessage)
        }
        return super.onCommand(sender, command, label, args)
    }
}
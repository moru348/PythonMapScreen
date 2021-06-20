package dev.moru3.minepie.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NmsUtils {
    companion object {
        private val version = Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(",")[3]

        fun getNmsClass(className: String): Class<*> {
            return Class.forName("net.minecraft.server.${version}.${className}")
        }

        fun getCraftBukkitClass(className: String): Class<*> {
            return Class.forName("org.bukkit.craftbukkit.${version}.${className}")
        }

        fun Player.asNmsPlayer(): Any {
            return this::class.java.getMethod("getHandle").invoke(this)
        }
        fun Any.asBukkitItemStack(): ItemStack {
            return getCraftBukkitClass("inventory.CraftItemStack")
                .getMethod("asBukkitCopy", getNmsClass("ItemStack"))
                .invoke(null, this) as ItemStack
        }
        fun ItemStack.asNmsItemStack(): Any {
            return getCraftBukkitClass("inventory.CraftItemStack")
                .getMethod("asNMSCopy", ItemStack::class.java)
                .invoke(null, this)
        }
        fun Player.sendPacket(packet: Any) {
            val nmsPlayer = this.asNmsPlayer()
            val con = nmsPlayer.javaClass.getField("playerConnection").get(nmsPlayer)
            val sendPacket = getNmsClass("PlayerConnection").getMethod("sendPacket", getNmsClass("Packet"))
            sendPacket.invoke(con, packet)
        }
    }
}
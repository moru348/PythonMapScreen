package dev.moru3.pythonmapscreen.map

import org.bukkit.Bukkit

class MapCursor(x: Byte,
                y: Byte,
                direction: Byte,
                type: Type,
                var caption: String? = null) {
    private val version = Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(",")[3]

    private fun getNmsClass(className: String): Class<*> { return Class.forName("net.minecraft.server.${version}.${className}") }
    val asNmsCursor: Any = getNmsClass("MapIcon")
        .getConstructor(getNmsClass("MapIcon\$Type"), Byte::class.java, Byte::class.java, Byte::class.java, getNmsClass("IChatBaseComponent"))
        .newInstance(getNmsClass("MapIcon\$Type").enumConstants[type.id], x, y, direction, caption)

    var x: Byte = x
        set(value) {
            asNmsCursor.javaClass.getDeclaredField("x").also { it.isAccessible = true }.set(asNmsCursor, value)
            field = value
        }
    var y: Byte = y
        set(value) {
            asNmsCursor.javaClass.getDeclaredField("y").also { it.isAccessible = true }.set(asNmsCursor, value)
            field = value
        }
    var direction: Byte = direction
        set(value) {
            asNmsCursor.javaClass.getDeclaredField("rotation").also { it.isAccessible = true }.set(asNmsCursor, value)
            field = value
        }
    var type: Type = type
        set(value) {
            val enum = getNmsClass("MapIcon\$Type").enumConstants[value.id]
            asNmsCursor.javaClass.getDeclaredField("type").also { it.isAccessible = true }.set(asNmsCursor, enum)
            field = value
        }

    enum class Type(val id: Int) {
        PLAYER(0),
        FRAME(1),
        RED_MARKER(2),
        BLUE_MARKER(3),
        TARGET_X(4),
        TARGET_POINT(5),
        PLAYER_OFF_MAP(6),
        PLAYER_OFF_LIMITS(7),
        MANSION(8),
        MONUMENT(9),
        BANNER_WHITE(10),
        BANNER_ORANGE(11),
        BANNER_MAGENTA(12),
        BANNER_LIGHT_BLUE(13),
        BANNER_YELLOW(14),
        BANNER_LIME(15),
        BANNER_PINK(16),
        BANNER_GRAY(17),
        BANNER_LIGHT_GRAY(18),
        BANNER_CYAN(19),
        BANNER_PURPLE(20),
        BANNER_BLUE(21),
        BANNER_BROWN(22),
        BANNER_GREEN(23),
        BANNER_RED(24),
        BANNER_BLACK(25),
        RED_X(26)
    }
}
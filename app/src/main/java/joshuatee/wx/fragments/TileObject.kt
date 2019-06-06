package joshuatee.wx.fragments

internal class TileObject(
    val photo: Int,
    val activity: Class<*>,
    val target: String,
    val argsArr: Array<String>,
    val helpStr: String,
    val objectTagStr: String,
    val description: String
)

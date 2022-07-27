/*

Made by ELY M. 

*/
//modded by ELY M. 

package joshuatee.wx.radar

class Hail internal constructor(hailIcon: String, hailSize: String, hailSizeNumber: Double, lat: Double, lon: Double) {
    var hailIcon: String = ""
    var hailSize: String = ""
    var hailSizeNumber = 0.0
    var lat: Double = 0.0
    var lon: Double = 0.0

    init {
        this.hailIcon = hailIcon
        this.hailSize = hailSize
        this.hailSizeNumber = hailSizeNumber
        this.lat = lat
        this.lon = lon
    }
}



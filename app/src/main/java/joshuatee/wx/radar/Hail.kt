/*

Made by ELY M. 

*/
//modded by ELY M. 

package joshuatee.wx.radar

class Hail internal constructor(hailIcon: String, hailSize: String, hailSizeNumber: Double, latD: Double, lonD: Double) {
    var hailIcon: String = ""
    var hailSize: String = ""
    var hailSizeNumber = 0.0
    var latD: Double = 0.0
    var lonD: Double = 0.0

    init {
        this.hailIcon = hailIcon
        this.hailSize = hailSize
        this.hailSizeNumber = hailSizeNumber
        this.latD = latD
        this.lonD = lonD
    }
}



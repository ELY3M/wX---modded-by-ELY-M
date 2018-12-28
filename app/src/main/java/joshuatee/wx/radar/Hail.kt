/*

Made by ELY M. 

*/
//modded by ELY M. 

package joshuatee.wx.radar

class Hail internal constructor(hailicon: String, hailsize: String, lat: Double, lon: Double) {
    var hailicon: String = ""
    var hailsize: String = ""
    var lat: Double = 0.0
    var lon: Double = 0.0


    init {
        this.hailicon = hailicon
        this.hailsize = hailsize
        this.lat = lat
        this.lon = lon
    }
}



/*
*
* This is part of getting mcd and wat texts within radar
* ELY M.
*
* */

package joshuatee.wx.radar



class SPC internal constructor(mcdno: String, mcdtext: String, lat: String, lon: String) {
    var mcdno: String = ""
    var mcdtext: String = ""
    var lat: String = ""
    var lon: String = ""
    var latD: Double = 0.0
    var lonD: Double = 0.0

    init {
        this.mcdno = mcdno
        this.mcdtext = mcdtext
        this.lat = lat
        this.lon = lon
        latD = lat.toDoubleOrNull() ?: 0.0
        lonD = -1.0 * (lon.toDoubleOrNull() ?: 0.0)
    }
}
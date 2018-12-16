/* Geodesy by Mike Gavaghan
 * 
 * http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula/
 * 
 * This code may be freely used and modified on any personal or professional
 * project.  It comes with no warranty.
 *
 * BitCoin tips graciously accepted at 1FB63FYQMy7hpC2ANVhZ5mSgAZEtY1aVLf
 */
package joshuatee.wx.external

import java.io.Serializable

/**
 * This is the outcome of a geodetic calculation. It represents the path and
 * ellipsoidal distance between two GlobalCoordinates for a specified reference
 * ellipsoid.
 *
 * @author Mike Gavaghan
 */
internal open class ExternalGeodeticCurve
/**
 * Create a new GeodeticCurve.
 * @param ellipsoidalDistance ellipsoidal distance in meters
 * @param azimuth azimuth in degrees
 * @param reverseAzimuth reverse azimuth in degrees
 */
    (
    /** Ellipsoidal distance (in meters).  */
    /**
     * Get the ellipsoidal distance.
     * @return ellipsoidal distance in meters
     */
    val ellipsoidalDistance: Double,
    /** Azimuth (degrees from north).  */
    /**
     * Get the azimuth.
     * @return azimuth in degrees
     */
    val azimuth: Double,
    /** Reverse azimuth (degrees from north).  */
    /**
     * Get the reverse azimuth.
     * @return reverse azimuth in degrees
     */
    val reverseAzimuth: Double
) : Serializable {

    /**
     * Get curve as a string.
     * @return
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("s=")
        buffer.append(ellipsoidalDistance)
        buffer.append(";a12=")
        buffer.append(azimuth)
        buffer.append(";a21=")
        buffer.append(reverseAzimuth)
        buffer.append(";")

        return buffer.toString()
    }
}

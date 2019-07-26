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

import joshuatee.wx.radar.LatLon
import java.io.Serializable
import kotlin.math.abs

/**
 *
 *
 * Encapsulation of latitude and longitude coordinates on a globe. Negative
 * latitude is southern hemisphere. Negative longitude is western hemisphere.
 *
 *
 *
 * Any angle may be specified for longtiude and latitude, but all angles will be
 * canonicalized such that:
 *
 *
 * <pre>
 * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
</pre> *
 *
 * @author Mike Gavaghan
 */
open class ExternalGlobalCoordinates
/**
 * Construct a new GlobalCoordinates. Angles will be canonicalized.
 *
 * @param mLatitude latitude in degrees
 * @param mLongitude longitude in degrees
 */
    (
    /** Latitude in degrees. Negative latitude is southern hemisphere.  */
    private var mLatitude: Double,
    /** Longitude in degrees. Negative longitude is western hemisphere.  */
    private var mLongitude: Double
) : Comparable<ExternalGlobalCoordinates>, Serializable {

    /**
     * Get latitude.
     *
     * @return latitude in degrees
     */
    /**
     * Set latitude. The latitude value will be canonicalized (which might result
     * in a change to the longitude). Negative latitude is southern hemisphere.
     *
     */

    constructor(location: LatLon) : this(location.lat, location.lon)

    constructor(ec: ExternalGlobalCoordinates) : this(ec.latitude, ec.longitude)

    var latitude: Double
        get() = mLatitude
        set(latitude) {
            mLatitude = latitude
            canonicalize()
        }

    /**
     * Get longitude.
     *
     * @return longitude in degrees
     */
    /**
     * Set longitude. The longitude value will be canonicalized. Negative
     * longitude is western hemisphere.
     *
     */
    var longitude: Double
        get() = mLongitude
        set(longitude) {
            mLongitude = longitude
            canonicalize()
        }

    /**
     * Canonicalize the current latitude and longitude values such that:
     *
     * <pre>
     * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
    </pre> *
     */
    private fun canonicalize() {
        mLatitude = (mLatitude + 180) % 360
        if (mLatitude < 0) mLatitude += 360.0
        mLatitude -= 180.0

        if (mLatitude > 90) {
            mLatitude = 180 - mLatitude
            mLongitude += 180.0
        } else if (mLatitude < -90) {
            mLatitude = -180 - mLatitude
            mLongitude += 180.0
        }

        mLongitude = (mLongitude + 180) % 360
        if (mLongitude <= 0) mLongitude += 360.0
        mLongitude -= 180.0
    }

    init {
        canonicalize()
    }

    /**
     * Compare these coordinates to another set of coordiates. Western longitudes
     * are less than eastern logitudes. If longitudes are equal, then southern
     * latitudes are less than northern latitudes.
     *
     * @param other instance to compare to
     * @return -1, 0, or +1 as per Comparable contract
     */
    override fun compareTo(other: ExternalGlobalCoordinates): Int {
        return when {
            mLongitude < other.mLongitude -> -1
            mLongitude > other.mLongitude -> +1
            mLatitude < other.mLatitude -> -1
            mLatitude > other.mLatitude -> +1
            else -> 0
        }
    }

    /**
     * Get a hash code for these coordinates.
     *
     * @return
     */
    override fun hashCode(): Int {
        return (mLongitude * mLatitude * 1000000.0 + 1021).toInt() * 1000033
    }

    /**
     * Compare these coordinates to another object for equality.
     *
     * @param
     * @return
     */
    override fun equals(obj: Any?): Boolean {
        if (obj !is ExternalGlobalCoordinates) return false
        val other = obj as ExternalGlobalCoordinates?
        return mLongitude == other!!.mLongitude && mLatitude == other.mLatitude
    }

    /**
     * Get coordinates as a string.
     */
    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(abs(mLatitude))
        buffer.append(if (mLatitude >= 0) 'N' else 'S')
        buffer.append(';')
        buffer.append(abs(mLongitude))
        buffer.append(if (mLongitude >= 0) 'E' else 'W')
        buffer.append(';')
        return buffer.toString()
    }
}

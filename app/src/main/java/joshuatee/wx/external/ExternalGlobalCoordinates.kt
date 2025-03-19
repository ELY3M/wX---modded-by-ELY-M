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

import joshuatee.wx.objects.LatLon

/**
 *
 *
 * Encapsulation of latitude and longitude coordinates on a globe. Negative
 * latitude is southern hemisphere. Negative longitude is western hemisphere.
 *
 *
 *
 * Any angle may be specified for longitude and latitude, but all angles will be
 * canonicalized such that:
 *
 *
 * <pre>
 * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
</pre> *
 *
 * @author Mike Gavaghan
 */
/**
 * Construct a new GlobalCoordinates. Angles will be canonicalized.
 *
 * mLatitude latitude in degrees
 * mLongitude longitude in degrees
 */
/** Latitude in degrees. Negative latitude is southern hemisphere.  */
/** Longitude in degrees. Negative longitude is western hemisphere.  */
open class ExternalGlobalCoordinates(
    private var mLatitude: Double,
    private var mLongitude: Double
) { // : Comparable<ExternalGlobalCoordinates>, Serializable
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
        if (mLongitude <= 0) {
            mLongitude += 360.0
        }
        mLongitude -= 180.0
    }

    init {
        canonicalize()
    }
}

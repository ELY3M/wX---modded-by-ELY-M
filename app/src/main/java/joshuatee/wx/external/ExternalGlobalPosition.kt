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

/**
 *
 *
 * Encapsulates a three dimensional location on a globe (GlobalCoordinates
 * combined with an elevation in meters above a reference ellipsoid).
 *
 *
 *
 * See documentation for GlobalCoordinates for details on how latitude and
 * longitude measurements are canonicalized.
 *
 *
 * @author Mike Gavaghan
 */
class ExternalGlobalPosition
/**
 * Creates a new instance of GlobalPosition.
 *
 * @param latitude latitude in degrees
 * @param longitude longitude in degrees
 * @param elevation elevation, in meters, above the reference ellipsoid
 */
private constructor(latitude: Double, longitude: Double,
        /** Elevation, in meters, above the surface of the ellipsoid.  */
        /**
         * Get elevation.
         *
         * @return elevation about the ellipsoid in meters.
         */
                    /**
                     * Set the elevation.
                     *
                     * @param elevation elevation about the ellipsoid in meters.
                     */
                    var elevation: Double) : ExternalGlobalCoordinates(latitude, longitude) {

    /**
     * Creates a new instance of GlobalPosition.
     *
     * @param coords coordinates of the position
     * @param elevation elevation, in meters, above the reference ellipsoid
     */
    constructor(coords: ExternalGlobalCoordinates, elevation: Double) : this(coords.latitude, coords.longitude, elevation) {}

    /**
     * Compare this position to another. Western longitudes are less than eastern
     * logitudes. If longitudes are equal, then southern latitudes are less than
     * northern latitudes. If coordinates are equal, lower elevations are less
     * than higher elevations
     *
     * @param other instance to compare to
     * @return -1, 0, or +1 as per Comparable contract
     */
    operator fun compareTo(other: ExternalGlobalPosition): Int {
        var retval = super.compareTo(other)

        if (retval == 0) {
            if (elevation < other.elevation)
                retval = -1
            else if (elevation > other.elevation) retval = +1
        }

        return retval
    }

    /**
     * Get a hash code for this position.
     *
     * @return
     */
    override fun hashCode(): Int {
        var hash = super.hashCode()

        if (elevation != 0.0) hash *= elevation.toInt()

        return hash
    }

    /**
     * Compare this position to another object for equality.
     *
     * @param
     * @return
     */
    override fun equals(obj: Any?): Boolean {
        if (obj !is ExternalGlobalPosition) return false

        val other = obj as ExternalGlobalPosition?

        return elevation == other!!.elevation && super.equals(other)
    }

    /**
     * Get position as a string.
     */
    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(super.toString())
        buffer.append("elevation=")
        buffer.append(elevation.toString())
        buffer.append("m")
        return buffer.toString()
    }
}

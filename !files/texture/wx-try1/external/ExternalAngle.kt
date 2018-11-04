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
 * Utility methods for dealing with angles.
 *
 * @author Mike Gavaghan
 */
internal object ExternalAngle {
    /** Degrees/Radians conversion constant.  */
    private const val PiOver180 = Math.PI / 180.0

    /**
     * Convert degrees to radians.
     * @param degrees
     * @return
     */
    fun toRadians(degrees: Double): Double {
        return degrees * PiOver180
    }

    /**
     * Convert radians to degrees.
     * @param radians
     * @return
     */
    fun toDegrees(radians: Double): Double {
        return radians / PiOver180
    }
}
/**
 * Disallow instantiation.
 */

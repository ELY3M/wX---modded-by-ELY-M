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

import kotlin.math.*

/**
 * Utility methods for dealing with angles.
 *
 * @author Mike Gavaghan
 */
internal object ExternalAngle {

    private const val PI_OVER_180 = PI / 180.0

    fun toRadians(degrees: Double) = degrees * PI_OVER_180

    fun toDegrees(radians: Double) = radians / PI_OVER_180
}

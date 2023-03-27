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
 * Encapsulation of an ellipsoid, and declaration of common reference ellipsoids.
 * @author Mike Gavaghan
 */
class ExternalEllipsoid
/**
 * Construct a new Ellipsoid.  This is private to ensure the values are
 * consistent (flattening = 1.0 / inverseFlattening).  Use the methods
 * fromAAndInverseF() and fromAAndF() to create new instances.
 * @param semiMajorAxis
 * @param semiMinorAxis
 * @param flattening
 * @param inverseFlattening
 */
private constructor(val semiMajorAxis: Double, val semiMinorAxis: Double, val flattening: Double, private val inverseFlattening: Double) : Serializable {
    companion object {

        /** The WGS84 ellipsoid.  */
        val WGS84: ExternalEllipsoid = fromAAndInverseF(6378137.0, 298.257223563)

        /**
         * Build an Ellipsoid from the semi major axis measurement and the inverse flattening.
         * @param semiMajor semi major axis (meters)
         * @param inverseFlattening
         * @return
         */
        private fun fromAAndInverseF(semiMajor: Double, inverseFlattening: Double): ExternalEllipsoid {
            val f = 1.0 / inverseFlattening
            val b = (1.0 - f) * semiMajor
            return ExternalEllipsoid(semiMajor, b, f, inverseFlattening)
        }
    }
}

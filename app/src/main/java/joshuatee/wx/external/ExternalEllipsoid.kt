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
 * @param semiMajor
 * @param semiMinor
 * @param flattening
 * @param inverseFlattening
 */
private constructor(
        /** Semi major axis (meters).  */
        /**
         * Get semi-major axis.
         * @return semi-major axis (in meters).
         */
        val semiMajorAxis: Double,
        /** Semi minor axis (meters).  */
        /**
         * Get semi-minor axis.
         * @return semi-minor axis (in meters).
         */
        val semiMinorAxis: Double,
        /** Flattening.  */
        /**
         * Get flattening
         * @return
         */
        val flattening: Double,
        /** Inverse flattening.  */
        /**
         * Get inverse flattening.
         * @return
         */
        val inverseFlattening: Double) : Serializable {
    companion object {

        /** The WGS84 ellipsoid.  */
        val WGS84 = fromAAndInverseF(6378137.0, 298.257223563)

        /** The GRS80 ellipsoid.  */
        val GRS80 = fromAAndInverseF(6378137.0, 298.257222101)

        /** The GRS67 ellipsoid.  */
        val GRS67 = fromAAndInverseF(6378160.0, 298.25)

        /** The ANS ellipsoid.  */
        val ANS = fromAAndInverseF(6378160.0, 298.25)

        /** The WGS72 ellipsoid.  */
        val WGS72 = fromAAndInverseF(6378135.0, 298.26)

        /** The Clarke1858 ellipsoid.  */
        val Clarke1858 = fromAAndInverseF(6378293.645, 294.26)

        /** The Clarke1880 ellipsoid.  */
        val Clarke1880 = fromAAndInverseF(6378249.145, 293.465)

        /** A spherical "ellipsoid".  */
        val Sphere = fromAAndF(6371000.0, 0.0)

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

        /**
         * Build an Ellipsoid from the semi major axis measurement and the flattening.
         * @param semiMajor semi major axis (meters)
         * @param flattening
         * @return
         */
        fun fromAAndF(semiMajor: Double, flattening: Double): ExternalEllipsoid {
            val inverseF = 1.0 / flattening
            val b = (1.0 - flattening) * semiMajor

            return ExternalEllipsoid(semiMajor, b, flattening, inverseF)
        }
    }
}

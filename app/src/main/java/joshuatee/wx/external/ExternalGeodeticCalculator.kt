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

import joshuatee.wx.external.ExternalAngle.toDegrees
import joshuatee.wx.external.ExternalAngle.toRadians

import kotlin.math.*

/**
 *
 *
 * Implementation of Thaddeus Vincenty's algorithms to solve the direct and
 * inverse geodetic problems. For more information, see Vincent's original
 * publication on the NOAA website:
 *
 * See http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
 *
 * @author Mike Gavaghan
 */
class ExternalGeodeticCalculator {

    //private final double TwoPi = 2.0 * Math.PI;
    /**
     * Calculate the destination and final bearing after traveling a specified
     * distance, and a specified starting bearing, for an initial location. This
     * is the solution to the direct geodetic problem.
     *
     *  ellipsoid reference ellipsoid to use
     *  start starting location
     *  startBearing starting bearing (degrees)
     *  distance distance to travel (meters)
     *  endBearing bearing at destination (degrees) element at index 0 will
     * be populated with the result
     * return
     */

    fun calculateEndingGlobalCoordinates(start: ExternalGlobalCoordinates, startBearing: Double, distance: Double): ExternalGlobalCoordinates {
        return calculateEndingGlobalCoordinatesLocal(ExternalEllipsoid.WGS84, start, startBearing, distance, DoubleArray(2))
    }

    private fun calculateEndingGlobalCoordinatesLocal(ellipsoid: ExternalEllipsoid, start: ExternalGlobalCoordinates, startBearing: Double, distance: Double,
                                                      endBearing: DoubleArray?): ExternalGlobalCoordinates {
        val a = ellipsoid.semiMajorAxis
        val b = ellipsoid.semiMinorAxis
        val aSquared = a * a
        val bSquared = b * b
        val f = ellipsoid.flattening
        val phi1 = toRadians(start.latitude)
        val alpha1 = toRadians(startBearing)
        val cosAlpha1 = cos(alpha1)
        val sinAlpha1 = sin(alpha1)
        val tanU1 = (1.0 - f) * tan(phi1)
        val cosU1 = 1.0 / sqrt(1.0 + tanU1 * tanU1)
        val sinU1 = tanU1 * cosU1
        // eq. 1
        val sigma1 = atan2(tanU1, cosAlpha1)
        // eq. 2
        val sinAlpha = cosU1 * sinAlpha1
        val sin2Alpha = sinAlpha * sinAlpha
        val cos2Alpha = 1 - sin2Alpha
        val uSquared = cos2Alpha * (aSquared - bSquared) / bSquared
        // eq. 3
        val bigA = 1 + uSquared / 16384 * (4096 + uSquared * (-768 + uSquared * (320 - 175 * uSquared)))
        // eq. 4
        val bigB = uSquared / 1024 * (256 + uSquared * (-128 + uSquared * (74 - 47 * uSquared)))
        // iterate until there is a negligible change in sigma
        var deltaSigma: Double
        val sOverbA = distance / (b * bigA)
        var sigma = sOverbA
        var sinSigma: Double
        var prevSigma = sOverbA
        var sigmaM2: Double
        var cosSigmaM2: Double
        var cos2SigmaM2: Double
        while (true) {
            // eq. 5
            sigmaM2 = 2.0 * sigma1 + sigma
            cosSigmaM2 = cos(sigmaM2)
            cos2SigmaM2 = cosSigmaM2 * cosSigmaM2
            sinSigma = sin(sigma)
            val cosSignma = cos(sigma)
            // eq. 6
            deltaSigma = (bigB
                    * sinSigma
                    * (cosSigmaM2 + bigB / 4.0
                    * (cosSignma * (-1 + 2 * cos2SigmaM2) - bigB / 6.0 * cosSigmaM2 * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM2))))
            // eq. 7
            sigma = sOverbA + deltaSigma
            // break after converging to tolerance
            if (abs(sigma - prevSigma) < 0.0000000000001) break
            prevSigma = sigma
        }
        sigmaM2 = 2.0 * sigma1 + sigma
        cosSigmaM2 = cos(sigmaM2)
        cos2SigmaM2 = cosSigmaM2 * cosSigmaM2
        val cosSigma = cos(sigma)
        sinSigma = sin(sigma)
        // eq. 8
        val phi2 = atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1.0 - f)
                * sqrt(sin2Alpha + (sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1).pow(2.0)))
        // eq. 9
        // This fixes the pole crossing defect spotted by Matt Feemster. When a
        // path passes a pole and essentially crosses a line of latitude twice -
        // once in each direction - the longitude calculation got messed up. Using
        // atan2 instead of atan fixes the defect. The change is in the next 3
        // lines.
        // double tanLambda = sinSigma * sinAlpha1 / (cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        // double lambda = Math.atan(tanLambda);
        val lambda = atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1)
        // eq. 10
        val bigC = f / 16 * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha))
        // eq. 11
        val bigL = lambda - (1 - bigC) * f * sinAlpha * (sigma + bigC * sinSigma * (cosSigmaM2 + bigC * cosSigma * (-1 + 2 * cos2SigmaM2)))
        // eq. 12
        val alpha2 = atan2(sinAlpha, -sinU1 * sinSigma + cosU1 * cosSigma * cosAlpha1)
        // build result
        val latitude = toDegrees(phi2)
        val longitude = start.longitude + toDegrees(bigL)
        if (endBearing != null && endBearing.isNotEmpty()) endBearing[0] = toDegrees(alpha2)
        return ExternalGlobalCoordinates(latitude, longitude)
    }
}
package joshuatee.wx.radar

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CoordinateConversion {
    internal val toRadians = 0.017453292519943295
    internal var x = 0.0
    internal var y = 0.0

    var scalingfactor = 10.0

    internal val LOG_TAG = CoordinateConversion::class.java.simpleName
    internal val PI = 3.141592653589793
    internal val R = 3437.9092710000004
    private val SIZE_BYTE = 1
    private val SIZE_SHORT = 2
    private var degToMeters: Double
    private var degToRad: Double
    private var earthRadius: Double
    private val verbose = false

    fun ComputeAzimuth(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lon1 = lon1
        var lon2 = lon2
        lat1 *= degToRad
        lon1 *= degToRad
        lon2 *= degToRad
        return Math.atan2(Math.sin(lon2 - lon1), -Math.sin(lat1) * Math.cos(lon2 - lon1) + Math.tan(lat2 * degToRad) * Math.cos(lat1))
    }

    fun getZDepth(x: Int, y: Int): Short {
        val zDepth = allocBytes(2)
        return 0.toShort()
    }

    fun allocBytes(howmany: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(howmany * 1).order(ByteOrder.nativeOrder())
    }

    inner class XYLOC {
        var x: Int = 0
        var y: Int = 0
    }

    init {
        degToRad = 0.017453292519943295
        degToMeters = 111130.55555555556
        earthRadius = 180.0 * degToMeters / 3.141592653589793
    }

    fun AdjustIconSize(width: Float, height: Float): Float {
        if (width == 0.0f || height == 0.0f) {
            return 1.0f
        }
        var max = width
        if (height > width) {
            max = height
        }
        val autoscale = 480.0f / max
        if (verbose) {
            Log.d(LOG_TAG, "IconScale set to $autoscale")
        }
        return autoscale
    }

    internal fun ComputeDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lat2 = lat2
        lat1 *= degToRad
        lat2 *= degToRad
        return earthRadius * Math.asin(Math.sqrt(Math.sin((lat2 - lat1) / 2.0) * Math.sin((lat2 - lat1) / 2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.sin((lon2 - lon1) * degToRad / 2.0) * Math.sin((lon2 - lon1) * degToRad / 2.0))) / 926.0
    }

    fun computeMidpoint(lat1: Double, lon1: Double, lat2: Double, lon2: Double): DoubleArray {
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        var lon2 = lon2
        lat1 *= 0.017453292519943295
        lat2 *= 0.017453292519943295
        lon1 *= 0.017453292519943295
        lon2 *= 0.017453292519943295
        val result = DoubleArray(2)
        val bx = Math.cos(lat2) * Math.cos(lon2 - lon1)
        val by = Math.cos(lat2) * Math.sin(lon2 - lon1)
        result[0] = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by))
        result[1] = Math.atan2(by, Math.cos(lat1) + bx) + lon1
        result[0] = result[0] / 0.017453292519943295
        result[1] = result[1] / 0.017453292519943295
        return result
    }

    fun course(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        var lon2 = lon2
        lat1 *= 0.017453292519943295
        lat2 *= 0.017453292519943295
        lon1 *= 0.017453292519943295
        lon2 *= 0.017453292519943295
        this.y = Math.sin(lon1 - lon2) * Math.cos(lat2)
        this.x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2)
        return Math.atan2(this.y, this.x) * -1.0
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lat2 = lat2
        lat1 *= 0.017453292519943295
        lat2 *= 0.017453292519943295
        return R * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 * 0.017453292519943295 - lon1 * 0.017453292519943295))
    }

    fun getScreenScaling(width: Float, height: Float): Float {
        var max = width
        if (height > width) {
            max = height
        }
        return 400.0f / max
    }

    fun isPointWithinPolygon(polylat: FloatArray, polylon: FloatArray, mylat: Double, mylon: Double): Boolean {
        var pointInPolygon = false
        val polySides = polylat.size
        if (polySides != polylon.size) {
            Log.e(LOG_TAG, "Number of points in polygon does not match")
            return false
        }
        var j = polySides - 1
        var i = 0
        while (i < polySides) {
            if ((polylat[i].toDouble() < mylat && polylat[j].toDouble() >= mylat || polylat[j].toDouble() < mylat && polylat[i].toDouble() >= mylat) && (polylon[i].toDouble() <= mylon || polylon[j].toDouble() <= mylon)) {
                pointInPolygon = pointInPolygon xor if (polylon[i].toDouble() + (mylat - polylat[i].toDouble()) / (polylat[j] - polylat[i]).toDouble() * (polylon[j] - polylon[i]).toDouble() < mylon) true else false
            }
            j = i
            i++
        }
        return pointInPolygon
    }

    fun latlonFromRadialDist(lat1: Double, lon1: Double, tc: Double, dist: Double): DoubleArray {
        var lat1 = lat1
        var lon1 = lon1
        var tc = tc
        var dist = dist
        val result = DoubleArray(2)
        lat1 *= 0.017453292519943295
        lon1 *= 0.017453292519943295
        tc *= -1.0
        dist /= R
        result[0] = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(tc))
        result[1] = ((lon1 - Math.atan2(Math.sin(tc) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(result[0])) + 3.141592653589793) % 6.283185307179586 - 3.141592653589793) / 0.017453292519943295
        result[0] = result[0] / 0.017453292519943295
        return result
    }

    fun latLonToGl(centerlat: Double, centerlon: Double, lat: Double, lon: Double): XYLOC {
        val result = XYLOC()
        val dist = ComputeDistance(centerlat, centerlon, lat, lon)
        val bearing = ComputeAzimuth(centerlat, centerlon, lat, lon)
        var xvalue = (scalingfactor * dist * Math.cos(bearing))
        var yvalue = (scalingfactor * dist * Math.sin(bearing))
        if (xvalue > 32000) {
            xvalue = 32000.0
        }
        if (xvalue < -32000) {
            xvalue = -32000.0
        }
        if (yvalue > 32000) {
            yvalue = 32000.0
        }
        if (yvalue < -32000) {
            yvalue = -32000.0
        }
        result.x = xvalue.toShort().toInt()
        result.y = yvalue.toShort().toInt()
        return result
    }

    fun polygonArea(polylat: FloatArray, polylon: FloatArray): Float {
        val points = polylat.size
        if (points != polylon.size) {
            Log.e(LOG_TAG, "Number of points in polygon does not match")
            return 0.0f
        }
        var area = 0.0f
        var j = points - 1
        for (i in 0 until points) {
            area += (polylon[j] + polylon[i]) * (polylat[j] - polylat[i])
            j = i
        }
        return 0.5f * area
    }

}

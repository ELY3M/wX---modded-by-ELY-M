package joshuatee.wx.external

/*

     Copyright 2013-present Roman Kushnarenko

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

https://github.com/sromku/polygon-contains-point

 */

import joshuatee.wx.objects.LatLon

/**
 * The 2D polygon. <br></br>
 *
 * @see {@link Builder}
 *
 * @author Roman Kushnarenko (sromku@gmail.com)
 */
class ExternalPolygon private constructor(
    private val sides: List<ExternalLine>,
    private val boundingBox: BoundingBox
) {

    /**
     * Builder of the polygon
     *
     * @author Roman Kushnarenko (sromku@gmail.com)
     */
    class Builder {
        private var vertexes = mutableListOf<ExternalPoint>()
        private val sides = mutableListOf<ExternalLine>()
        private var boundingBox: BoundingBox? = null
        private var firstPoint = true
        private var isClosed = false

        /**
         * Add vertex points of the polygon.<br></br>
         * It is very important to add the vertexes by order, like you were drawing them one by one.
         *
         * @param point
         * The vertex point
         * @return The builder
         */
        fun addVertex(point: ExternalPoint): Builder {
            if (isClosed) {
                // each hole we start with the new array of vertex points
                vertexes = mutableListOf()
                isClosed = false
            }
            updateBoundingBox(point)
            vertexes.add(point)
            // add line (edge) to the polygon
            if (vertexes.size > 1) {
                val line = ExternalLine(vertexes[vertexes.size - 2], point)
                sides.add(line)
            }
            return this
        }

        /**
         * Close the polygon shape. This will create a new side (edge) from the **last** vertex point to the **first** vertex point.
         *
         * @return The builder
         */
//        fun close(): Builder {
//            validate()
//            // add last Line
//            sides.add(ExternalLine(vertexes[vertexes.size - 1], vertexes[0]))
//            isClosed = true
//            return this
//        }

        /**
         * Build the instance of the polygon shape.
         *
         * @return The polygon
         */
        fun build(): ExternalPolygon {
            validate()
            // in case you forgot to close
            if (!isClosed) {
                // add last Line
                sides.add(ExternalLine(vertexes[vertexes.size - 1], vertexes[0]))
            }
            return ExternalPolygon(sides, boundingBox!!)
        }

        /**
         * Update bounding box with a new point.<br></br>
         *
         * @param point
         * New point
         */
        private fun updateBoundingBox(point: ExternalPoint) {
            if (firstPoint) {
                boundingBox = BoundingBox()
                boundingBox!!.xMax = point.x
                boundingBox!!.xMin = point.x
                boundingBox!!.yMax = point.y
                boundingBox!!.yMin = point.y
                firstPoint = false
            } else {
                // set bounding box
                if (point.x > boundingBox!!.xMax) {
                    boundingBox!!.xMax = point.x
                } else if (point.x < boundingBox!!.xMin) {
                    boundingBox!!.xMin = point.x
                }
                if (point.y > boundingBox!!.yMax) {
                    boundingBox!!.yMax = point.y
                } else if (point.y < boundingBox!!.yMin) {
                    boundingBox!!.yMin = point.y
                }
            }
        }

        private fun validate() {
            if (vertexes.size < 3) throw RuntimeException("Polygon must have at least 3 points")
        }
    }

    /**
     * Check if the the given point is inside of the polygon.<br></br>
     *
     * @param point
     * The point to check
     * @return `True` if the point is inside the polygon, otherwise return `False`
     */
    operator fun contains(point: ExternalPoint): Boolean {
        if (inBoundingBox(point)) {
            val ray = createRay(point)
            var intersection = 0
            for (side in sides) {
                if (intersect(ray, side)) intersection += 1
            }
            /*
			 * If the number of intersections is odd, then the point is inside the polygon
			 */
            if (intersection % 2 == 1) {
                return true
            }
        }
        return false
    }

    /**
     * By given ray and one side of the polygon, check if both lines intersect.
     *
     * @param ray
     * @param side
     * @return `True` if both lines intersect, otherwise return `False`
     */
    private fun intersect(ray: ExternalLine, side: ExternalLine): Boolean {
        val intersectPoint: ExternalPoint?
        // if both vectors aren't from the kind of x=1 lines then go into
        if (!ray.isVertical && !side.isVertical) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (ray.a - side.a == 0f) {
                return false
            }
            val x = (side.b - ray.b) / (ray.a - side.a) // x = (b2-b1)/(a1-a2)
            val y = side.a * x + side.b // y = a2*x+b2
            intersectPoint = ExternalPoint(x, y)
        } else if (ray.isVertical && !side.isVertical) {
            val x = ray.start.x
            val y = side.a * x + side.b
            intersectPoint = ExternalPoint(x, y)
        } else if (!ray.isVertical && side.isVertical) {
            val x = side.start.x
            val y = ray.a * x + ray.b
            intersectPoint = ExternalPoint(x, y)
        } else {
            return false
        }
        return side.isInside(intersectPoint) && ray.isInside(intersectPoint)
    }

    /**
     * Create a ray. The ray will be created by given point and on point outside of the polygon.<br></br>
     * The outside point is calculated automatically.
     *
     * @param point
     * @return
     */
    private fun createRay(point: ExternalPoint): ExternalLine {
        // create outside point
        val epsilon = (boundingBox.xMax - boundingBox.xMin) / 100.0f
        val outsidePoint = ExternalPoint(boundingBox.xMin - epsilon, boundingBox.yMin)
        return ExternalLine(outsidePoint, point)
    }

    /**
     * Check if the given point is in bounding box
     *
     * @param point
     * @return `True` if the point in bounding box, otherwise return `False`
     */
    private fun inBoundingBox(point: ExternalPoint) =
        !(point.x < boundingBox.xMin || point.x > boundingBox.xMax || point.y < boundingBox.yMin || point.y > boundingBox.yMax)

    private class BoundingBox {
        var xMax = Float.NEGATIVE_INFINITY
        var xMin = Float.NEGATIVE_INFINITY
        var yMax = Float.NEGATIVE_INFINITY
        var yMin = Float.NEGATIVE_INFINITY
    }

    companion object {

        fun polygonContainsPoint(latLon: LatLon, latLons: List<LatLon>): Boolean {
            val polygonFrame = Builder()
            latLons.forEach { polygonFrame.addVertex(ExternalPoint(it)) }
            val polygonShape = polygonFrame.build()
            return polygonShape.contains(latLon.asPoint())
        }
    }
}

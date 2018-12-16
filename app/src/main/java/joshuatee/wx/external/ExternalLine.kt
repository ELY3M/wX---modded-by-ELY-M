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

/**
 * Line is defined by starting point and ending point on 2D dimension.<br></br>
 *
 * @author Roman Kushnarenko (sromku@gmail.com)
 */
internal class ExternalLine(
    /**
     * Get start point
     *
     * @return The start point
     */
    val start: ExternalPoint,
    /**
     * Get end point
     *
     * @return The end point
     */
    val end: ExternalPoint
) {
    /**
     * y = **A**x + B
     *
     * @return The **A**
     */
    var a = Float.NaN
        private set
    /**
     * y = Ax + **B**
     *
     * @return The **B**
     */
    var b = Float.NaN
        private set
    /**
     * Indicate whereas the line is vertical. <br></br>
     * For example, line like x=1 is vertical, in other words parallel to axis Y. <br></br>
     * In this case the A is (+/-)infinite.
     *
     * @return `True` if the line is vertical, otherwise return `False`
     */
    var isVertical = false
        private set

    init {

        if (this.end.x - this.start.x != 0f) {
            a = (this.end.y - this.start.y) / (this.end.x - this.start.x)
            b = this.start.y - a * this.start.x
        } else {
            isVertical = true
        }
    }

    /**
     * Indicate whereas the point lays on the line.
     *
     * @param point
     * - The point to check
     * @return `True` if the point lays on the line, otherwise return `False`
     */
    fun isInside(point: ExternalPoint): Boolean {
        val maxX = if (start.x > end.x) start.x else end.x
        val minX = if (start.x < end.x) start.x else end.x
        val maxY = if (start.y > end.y) start.y else end.y
        val minY = if (start.y < end.y) start.y else end.y

        //return if (point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY) {
        return point.x in minX..maxX && point.y >= minY && point.y <= maxY
    }

    override fun toString(): String {
        return String.format("%s-%s", start.toString(), end.toString())
    }
}


/*
 * 
 * 
 * 
 Builder poly2 = new Polygon.Builder();
    for(int i = 0; i< xpoints.length;i++){
        poly2.addVertex(new Point(xpoints[i],ypoints[i]));
    }
    Polygon polygon2 = poly2.build();
    http://stackoverflow.com/questions/15816928/test-of-point-inside-polygon-in-android
 
 import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

public class Tests
{
	public static void main(String[] args)
	{
		testSimplePolygon();
		
		testPolygonWithHoles();
	}
	
	
	public static void testSimplePolygon()
	{
		Polygon polygon = Polygon.Builder()
				.addVertex(new Point(1, 3))
				.addVertex(new Point(2, 8))
				.addVertex(new Point(5, 4))
				.addVertex(new Point(5, 9))
				.addVertex(new Point(7, 5))
				.addVertex(new Point(6, 1))
				.addVertex(new Point(3, 1))
				.build();
		
		// Point is inside
		isInside(polygon, new Point(5.5f, 7));
		
		// Point isn't inside
		isInside(polygon, new Point(4.5f, 7));
	}
	
	
	  Create polygon two holes and check that the point is inside
	 
	public static void testPolygonWithHoles()
	{
		Polygon polygon = Polygon.Builder()
				.addVertex(new Point(1, 2)) // polygon
				.addVertex(new Point(1, 6))
				.addVertex(new Point(8, 7))
				.addVertex(new Point(8, 1))
				.close() 
				.addVertex(new Point(2, 3)) // hole one
				.addVertex(new Point(5, 5))
				.addVertex(new Point(6, 2))
				.close() 
				.addVertex(new Point(6, 6)) // hole two
				.addVertex(new Point(7, 6))
				.addVertex(new Point(7, 5))
				.build();
		
		// Point is inside
		isInside(polygon, new Point(6, 5));
		
		// Point isn't inside
		isInside(polygon, new Point(4, 3));
		
		// Point isn't inside
		isInside(polygon, new Point(6.5f, 5.8f));
	}
	
	
	 * Check if point inside the polygon
	 * 
	 * @param polygon
	 * @param point
	 
	private static void isInside(Polygon polygon, Point point)
	{
		boolean contains = polygon.contains(point);
		System.out.println("The point:" + point.toString() + " is " + (contains ? "" : "not ") + "inside the polygon");
	}
}

 
 */

package joshuatee.wx.fingerdraw

/*

Credit for this file goes to
FingerDraw https://github.com/ChrisLizon/FingerDraw (Chris Lizon)

This class is used for the drawing tool accessible from the screen recording toolbar.

 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.UtilityUI

/**
 * The drawview is the view that intercepts the MotionEvents
 * and does the actual drawing of line segments on the display.

 * @author Chris Lizon
 */

class DrawView : View {

    companion object {
        private const val ACTION_ADDSEGMENT = 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MyApplication.dm.widthPixels,
            MyApplication.dm.heightPixels - UtilityUI.statusBarHeight(context) - MyApplication.actionBarHeight
        )
    }

    private val undoButton: Button? = null

    /** the paint holds the draw options like colors  */
    private val paint = Paint()

    /** hold a list of segments that are on the screen  */
    private val segments = mutableListOf<Segment>()

    /** hold a list of segments removed from the screen  */
    //private final List<Segment> deletedSegments = new mutableListOf<>();

    /** keep track to determine if we last added or deleted  */
    private val actions = mutableListOf<Int>()

    //listers to process the correct events
    //when we change modes, we swap out the listeners
    private val drawListener = DrawListener()

    /** this constructor allows us to use this in an xml layout  */
    constructor(context: Context, attribs: AttributeSet) : super(context, attribs) {
        setup()
    }

    /** default constructor  */
    constructor(context: Context) : super(context) {
        setup()
    }

    private fun setup() {
        isFocusable = true
        isFocusableInTouchMode = true
        this.setOnTouchListener(drawListener)
        paint.color = MyApplication.drawToolColor // was Color.RED
        paint.isAntiAlias = true
        //paint.setStrokeWidth(4.5f); // was 3.0f
        paint.strokeWidth = MyApplication.drawtoolSize.toFloat() // was 3.0f
    }

    /**Called when the system is painting the view to the screen
     * in our case we need to draw all the lines we've stored
     */
    public override fun onDraw(canvas: Canvas) {

        //draw the line segments
        for (segment in segments) {
            val points = segment.points
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                //simply draw lines between all points on a segment
                canvas.drawLine(start.x, start.y, end.x, end.y, paint)
            }
        }

        //either enable or disable the undo button
        if (undoButton != null)
        //if there are actions recorded enable the undo button
            undoButton.isEnabled = actions.size > 0
    }

    /** DrawListener will take touches and add segments  */
    private inner class DrawListener : OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                //action down is fired when your finger hits the screen
                if (segments.size == 0 || segments[segments.size - 1].points.size != 0) {
                    //when you put your finger down, we'll start a new segment
                    segments.add(Segment())
                    //and we'll indicate that the last action was a draw
                    actions.add(ACTION_ADDSEGMENT)
                }

            } else if (event.action == MotionEvent.ACTION_MOVE) {
                //action move is fired over and over as your finger is
                //dragged across the screen
                //here we add a point to the last segment
                val point = Point(event.x, event.y)
                segments[segments.size - 1].points.add(point)
                invalidate() //time to redraw
            }
            //else if(event.getAction() == MotionEvent.ACTION_UP)
            //we do nothing, but we could do something
            //if we wanted to.
            return true //let the OS know this touch event was handled
        }
    }
}

internal class Point {

    var x = 0.toFloat()
    var y = 0.toFloat()

    constructor(x2: Float, y2: Float) {
        this.x = x2
        this.y = y2
    }

    constructor()

    //overwrote toString to represent the points coordinates
    override fun toString() = "$x,$y"
}

internal class Segment {

    val points = mutableListOf<Point>()

    //overwrote toString to printout a list of points for debugging
    override fun toString(): String {
        val buffer = StringBuilder()
        for (p in points) {
            buffer.append("|")
            buffer.append(p.toString())
        }
        return buffer.toString().replaceFirst("\\|".toRegex(), "")
    }
}



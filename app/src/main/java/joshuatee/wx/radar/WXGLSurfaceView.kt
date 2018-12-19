/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.radar

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

import kotlin.math.*

class WXGLSurfaceView : GLSurfaceView, GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    // currently used in location frag and USWXOGLRadarAtivity to more clearly seperate touch events
    // WXGLSurfaceView is used to track and respond to user touch events when in the OpenGL based radar
    // pinch zoom, drag, double/single tap, and long press are all handled here

    companion object {
        var scaleFactorGlobal: Float = 1.0f
            private set
    }

    var idx: Int = 0
    private var listener: OnProgressChangeListener? = null
    var fullScreen: Boolean = false
    var toolbar: Toolbar? = null
    var toolbarBottom: Toolbar? = null
    var archiveMode: Boolean = false
    var toolbarsHidden: Boolean = false
        private set
    private var mScaleFactor = 1.0f
    var newX: Float = 0.0f
        private set
    var newY: Float = 0.0f
        private set
    private var centerX = 0.0f
    private var centerY = 0.0f
    private var xPos = 0.0f
    private var yPos = 0.0f
    private var xMiddle = 0.0f
    private var yMiddle = 0.0f
    private val mScaleDetector: ScaleGestureDetector
    private val mGestureDetector: GestureDetector
    private var numPanes = 0
    private var oglr = mutableListOf<WXGLRender>()
    private lateinit var oglrCurrent: WXGLRender
    private var density = 0.0f
    private var wxgl = mutableListOf<WXGLSurfaceView>()
    var idxInt: Int = 0
    private var widthDivider = 0
    var citiesExtAl: MutableList<TextView> = mutableListOf()
    var countyLabelsAl: MutableList<TextView> = mutableListOf()
    var obsAl: MutableList<TextView> = mutableListOf()
    var spottersLabelAl: MutableList<TextView> = mutableListOf()
    var spotterTv: MutableList<TextView> = mutableListOf()
    var wxgltextArr: MutableList<WXGLTextObject> = mutableListOf()
    var locfrag: Boolean = false
    private var act: Activity? = null

    constructor(context: Context, wd: Int, np: Int) : super(context) {
        this.widthDivider = wd
        this.numPanes = np
        mGestureDetector = GestureDetector(context, this)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mGestureDetector = GestureDetector(context, this)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    fun setRenderVar(
        oglrR: WXGLRender,
        OGLR_r: MutableList<WXGLRender>,
        wxglR: MutableList<WXGLSurfaceView>
    ) {
        oglr = OGLR_r
        oglrCurrent = oglrR
        wxgl = wxglR
    }

    fun setRenderVar(
        oglrR: WXGLRender,
        OGLR_r: MutableList<WXGLRender>,
        wxglR: MutableList<WXGLSurfaceView>,
        activity: Activity
    ) {
        oglr = OGLR_r
        oglrCurrent = oglrR
        wxgl = wxglR
        act = activity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!locfrag) {
                    (0 until numPanes).forEach {
                        wxgltextArr[it].hideTV()
                        oglr[it].displayHold = true
                    }
                }
                if (numPanes == 1 && fullScreen || numPanes > 1) {
                    UtilityUI.immersiveMode(act!!)
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                listener?.onProgressChanged(50000, idx, idxInt)
                (0 until numPanes).forEach {
                    oglr[it].displayHold = false
                    wxgl[it].requestRender()
                }
            }
        }
        var retVal = mScaleDetector.onTouchEvent(event)
        retVal = mGestureDetector.onTouchEvent(event) || retVal
        return retVal || super.onTouchEvent(event)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScalefactor = mScaleFactor
            mScaleFactor *= detector.scaleFactor
            if (MyApplication.dualpaneshareposn) {
                (0 until numPanes).forEach {
                    oglr[it].x = oglr[it].x * (mScaleFactor / oldScalefactor)
                    oglr[it].y = oglr[it].y * (mScaleFactor / oldScalefactor)
                    oglr[it].zoom = mScaleFactor
                }
            } else {
                oglrCurrent.x = oglrCurrent.x * (mScaleFactor / oldScalefactor)
                oglrCurrent.y = oglrCurrent.y * (mScaleFactor / oldScalefactor)
                oglrCurrent.zoom = mScaleFactor
            }
            if (MyApplication.dualpaneshareposn) {
                (0 until numPanes).forEach {
                    wxgl[it].mScaleFactor = mScaleFactor
                    wxgl[it].requestRender()
                }
            } else {
                requestRender()
            }
            scaleFactorGlobal = mScaleFactor
            return true
        }
    }

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        if (fullScreen) {
            toolbar!!.visibility = View.VISIBLE
            if (!archiveMode) toolbarBottom!!.visibility = View.VISIBLE
        }
        density = (oglrCurrent.ortInt * 2).toFloat() / width
        xPos = event.x
        yPos = event.y
        xMiddle = (width / 2).toFloat()
        yMiddle = (height / 2).toFloat()
        val diffX = density * (xMiddle - xPos) / mScaleFactor
        val diffY = density * (yMiddle - yPos) / mScaleFactor
        val xStr = Utility.readPref(context, "RID_" + oglrCurrent.rid + "_X", "0.00")
        val yStr = Utility.readPref(context, "RID_" + oglrCurrent.rid + "_Y", "0.00")
        centerX = xStr.toFloatOrNull() ?: 0.0f
        centerY = yStr.toFloatOrNull() ?: 0.0f
        val ppd = oglrCurrent.oneDegreeScaleFactor
        newX = centerY + (oglrCurrent.x / mScaleFactor + diffX) / ppd
        val test2 = 180 / PI * log(tan(PI / 4 + centerX * (PI / 180) / 2), E)
        newY = test2.toFloat() + (-oglrCurrent.y / mScaleFactor + diffY) / ppd
        newY = (180 / PI * (2 * atan(exp(newY * PI / 180)) - PI / 2)).toFloat()
        oglrCurrent.ridNewList = UtilityLocation.getNearestRid(
            context,
            LatLon(newY.toString(), (newX * -1).toString()),
            5
        )
        listener?.onProgressChanged(idx, idx, idxInt)
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        var panned = false
        if (!locfrag) {
            if (distanceX != 0f) {
                if (MyApplication.dualpaneshareposn) {
                    (0 until numPanes).forEach { oglr[it].x += -1.0f * distanceX }
                } else {
                    oglrCurrent.x += -1.0f * distanceX
                }
                panned = true
            }
            if (distanceY != 0f) {
                if (MyApplication.dualpaneshareposn) {
                    (0 until numPanes).forEach { oglr[it].y += distanceY }
                } else {
                    oglrCurrent.y += distanceY
                }
                panned = true
            }
            if (panned) {
                if (MyApplication.dualpaneshareposn) {
                    (0 until numPanes).forEach { wxgl[it].requestRender() }
                } else {
                    requestRender()
                }
            }
            if (fullScreen || numPanes > 1) {
                if (!MyApplication.lockToolbars) {
                    toolbar!!.visibility = View.GONE
                    toolbarBottom!!.visibility = View.GONE
                    toolbarsHidden = true
                }
            }
        }
        return panned
    }

    override fun onShowPress(event: MotionEvent) {}

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        density = (oglrCurrent.ortInt * 2).toFloat() / width
        xPos = event.x
        yPos = event.y
        xMiddle = (width / 2).toFloat()
        yMiddle = (height / 2).toFloat()
        if (MyApplication.dualpaneshareposn && !locfrag) {
            mScaleFactor *= 2.0f
            (0 until numPanes).forEach {
                oglr[it].setViewInitial(
                    mScaleFactor,
                    oglr[it].x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                    oglr[it].y * 2.0f + (yMiddle - yPos) * -2.0f * density
                )
                wxgl[it].mScaleFactor = mScaleFactor
                wxgl[it].requestRender()
            }
        } else {
            mScaleFactor *= 2.0f
            oglrCurrent.setViewInitial(
                mScaleFactor,
                oglrCurrent.x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                oglrCurrent.y * 2.0f + (yMiddle - yPos) * -2.0f * density
            )
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) toolbarBottom!!.visibility = View.VISIBLE
        }
        listener?.onProgressChanged(50000, idx, idxInt)
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        mScaleFactor /= 2.0f
        if (MyApplication.dualpaneshareposn && !locfrag) {
            (0 until numPanes).forEach {
                oglr[it].setViewInitial(mScaleFactor, oglr[it].x / 2.0f, oglr[it].y / 2.0f)
                wxgl[it].mScaleFactor = mScaleFactor
                wxgl[it].requestRender()
            }
        } else {
            oglrCurrent.setViewInitial(mScaleFactor, oglrCurrent.x / 2.0f, oglrCurrent.y / 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) toolbarBottom!!.visibility = View.VISIBLE
        }
        listener?.onProgressChanged(50000, idx, idxInt)
        return true
    }

    fun setOnProgressChangeListener(l: OnProgressChangeListener) {
        listener = l
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(progress: Int, idx: Int, idxInt: Int)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        var height: Int
        if (numPanes == 1) {
            if (fullScreen) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                width = View.MeasureSpec.getSize(widthMeasureSpec)
                this.setMeasuredDimension(width, width)
            }
        } else {
            width =
                    if (Build.VERSION.SDK_INT >= 19 && (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent))
                        MyApplication.dm.widthPixels / widthDivider
                    else
                        MyApplication.dm.widthPixels / widthDivider
            if (Build.VERSION.SDK_INT >= 19 && (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)) {
                height = MyApplication.dm.heightPixels / 2 + UtilityUI.statusBarHeight(context)
                if (numPanes == 2) {
                    height = MyApplication.dm.heightPixels /
                            2 - UtilityUI.statusBarHeight(context) / 2
                }
            } else {
                height = MyApplication.dm.heightPixels / 2 - MyApplication.actionBarHeight
            }
            if (Build.VERSION.SDK_INT >= 19 && UIPreferences.radarToolbarTransparent && !UIPreferences.radarImmersiveMode && numPanes == 4)
                height = MyApplication.dm.heightPixels / 2 - UtilityUI.statusBarHeight(context) / 2
            this.setMeasuredDimension(width, height)
        }
    }

    val latLon: LatLon
        get() = LatLon(newY.toDouble(), newX.toDouble() * -1.0)

    var scaleFactor: Float
        get() = mScaleFactor
        set(mScaleFactor) {
            this.mScaleFactor = mScaleFactor
            scaleFactorGlobal = mScaleFactor
        }
}





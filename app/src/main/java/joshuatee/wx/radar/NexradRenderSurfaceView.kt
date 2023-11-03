/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
//modded by ELY M. 

package joshuatee.wx.radar

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.TextView
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.To
import kotlin.math.*

class NexradRenderSurfaceView : GLSurfaceView, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    //
    // currently used in LocationFragment and WXGLRadarActivity / WXGLRadarActivityMultiPane to more clearly separate touch events
    // WXGLSurfaceView is used to track and respond to user touch events when in the OpenGL based radar
    // pinch zoom, drag, double/single tap, and long press are all handled here
    //

    companion object {
        var scaleFactorGlobal = 1.0f
            private set
    }

    var index = 0
    private var listener: OnProgressChangeListener? = null
    var fullScreen = false
    var toolbar: Toolbar? = null
    var toolbarBottom: Toolbar? = null
    var archiveMode = false
    private var toolbarsHidden = false
    private var mScaleFactor = 1.0f
    private var newX = 0.0
    private var newY = 0.0
    private var centerX = 0.0f
    private var centerY = 0.0f
    private var xPos = 0.0f
    private var yPos = 0.0f
    private var xMiddle = 0.0f
    private var yMiddle = 0.0f
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var wxglRenders = listOf<NexradRender>()
    private lateinit var wxglRender: NexradRender
    private var density = 0.0f
    private var wxglSurfaceViews = listOf<NexradRenderSurfaceView>()
    var idxInt = 0
    private var widthDivider = 0
    private var heightDivider = 2
    val cities = mutableListOf<TextView>()
    val countyLabels = mutableListOf<TextView>()
    val observations = mutableListOf<TextView>()
    val pressureCenterLabels = mutableListOf<TextView>()
    val spotterLabels = mutableListOf<TextView>()
    val spotterTextView = mutableListOf<TextView>()
    var hailLabels = mutableListOf<TextView>()
    var hailTextView = mutableListOf<TextView>()
    var textObjects = mutableListOf<NexradRenderTextObject>()
    var locationFragment = false
    private var activity: Activity? = null

    constructor(context: Context, widthDivider: Int, heightDivider: Int) : super(context) {
        this.widthDivider = widthDivider
        this.heightDivider = heightDivider
        gestureDetector = GestureDetector(context, this)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        gestureDetector = GestureDetector(context, this)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    fun setRenderVar(wxglRender: NexradRender, wxglRenders: List<NexradRender>, wxglSurfaceViews: List<NexradRenderSurfaceView>) {
        this.wxglRenders = wxglRenders
        this.wxglRender = wxglRender
        this.wxglSurfaceViews = wxglSurfaceViews
    }

    fun setRenderVar(wxglRender: NexradRender, wxglRenders: List<NexradRender>, wxglSurfaceViews: List<NexradRenderSurfaceView>, activity: Activity) {
        this.wxglRenders = wxglRenders
        this.wxglRender = wxglRender
        this.wxglSurfaceViews = wxglSurfaceViews
        this.activity = activity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!locationFragment) {
                    wxglRenders.forEachIndexed { index, wxgl ->
                        textObjects[index].hideLabels()
                        wxgl.state.displayHold = true
                    }
                }
                if (!locationFragment && wxglRenders.size == 1 && fullScreen || (wxglRenders.size > 1 && !locationFragment)) {
                    UtilityUI.immersiveMode(activity!!)
                }
            }
	        //elys mod - 3 finger to show conus radar map
            MotionEvent.ACTION_MOVE -> {
	            //elys mod
                //3 fingers press to show conus
                val count = event.pointerCount
                //UtilityLog.d("wx-elys", "Fingers Count: "+count)
                if (count == 3) {
                    if (!locationFragment) {
                        wxglRenders.forEachIndexed { index, wxgl ->
                            textObjects[index].hideLabels()
                            wxgl.state.displayHold = true
                            wxgl.displayConus = true
                        }
                    }
                } else {
                    if (!locationFragment) {
                        wxglRenders.forEachIndexed { index, wxgl ->
                            textObjects[index].hideLabels()
                            wxgl.state.displayHold = true
                            wxgl.displayConus = false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                listener?.onProgressChanged(50000, index, idxInt)
                wxglRenders.forEachIndexed { index, wxgl ->
                    wxgl.state.displayHold = false
                    wxglSurfaceViews[index].requestRender()
                }
            }
        }
        var retVal = scaleDetector.onTouchEvent(event)
        retVal = gestureDetector.onTouchEvent(event) || retVal
        return retVal || super.onTouchEvent(event)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScaleFactor = mScaleFactor
            mScaleFactor *= detector.scaleFactor
            if (RadarPreferences.dualpaneshareposn) {
                wxglRenders.forEach {
                    it.state.x *= mScaleFactor / oldScaleFactor
                    it.state.y *= mScaleFactor / oldScaleFactor
                    it.state.zoom = mScaleFactor
                }
            } else {
                wxglRender.state.x *= mScaleFactor / oldScaleFactor
                wxglRender.state.y *= mScaleFactor / oldScaleFactor
                wxglRender.state.zoom = mScaleFactor
            }
            if (RadarPreferences.dualpaneshareposn) {
                wxglSurfaceViews.forEach {
                    it.mScaleFactor = mScaleFactor
                    it.requestRender()
                }
            } else {
                requestRender()
            }
            scaleFactorGlobal = mScaleFactor
            return true
        }
    }

    override fun onDown(event: MotionEvent): Boolean = true

    // API34 changed first arg to Optional
    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = true

    override fun onLongPress(event: MotionEvent) {
        if (fullScreen) {
            toolbar!!.visibility = View.VISIBLE
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
        density = (wxglRender.state.ortInt * 2.0f) / width
        xPos = event.x
        yPos = event.y
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        val diffX = density * (xMiddle - xPos) / mScaleFactor
        val diffY = density * (yMiddle - yPos) / mScaleFactor
        val xStr = UtilityLocation.getRadarSiteX(wxglRender.state.rid)
        val yStr = UtilityLocation.getRadarSiteY(wxglRender.state.rid)
        centerX = To.float(xStr)
        centerY = To.float(yStr)
        val ppd = wxglRender.state.projectionNumbers.oneDegreeScaleFactor
        newX = centerY + (wxglRender.state.x / mScaleFactor + diffX) / ppd
        val test2 = 180.0 / PI * log(tan(PI / 4.0 + centerX * (PI / 180.0) / 2.0), E)
        newY = test2 + (-1.0 * wxglRender.state.y / mScaleFactor + diffY) / ppd
        newY = (180.0 / PI * (2.0 * atan(exp(newY * PI / 180.0)) - PI / 2.0))
        wxglRender.state.closestRadarSites = UtilityLocation.getNearestRadarSites(LatLon(newY, newX * -1.0), 5)
        listener?.onProgressChanged(index, index, idxInt)
    }

    // API34 changed first arg to Optional
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        var panned = false
        if (!locationFragment && !RadarPreferences.wxoglCenterOnLocation) {
            if (abs(distanceX) > 0.001f) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglRenders.forEach {
                        it.state.x += -1.0f * distanceX
                    }
                } else {
                    wxglRender.state.x += -1.0f * distanceX
                }
                panned = true
            }
            if (abs(distanceY) > 0.001f) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglRenders.forEach {
                        it.state.y += distanceY
                    }
                } else {
                    wxglRender.state.y += distanceY
                }
                panned = true
            }
            if (panned) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglSurfaceViews.forEach {
                        it.requestRender()
                    }
                } else {
                    requestRender()
                }
            }
            adjustToolbarsLock()
        }
        return panned
    }

    fun onScrollByKeyboard(distanceX: Float, distanceY: Float) {
        var panned = false
        if (!locationFragment) {
            if (abs(distanceX) > 0.001f) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglRenders.forEach {
                        it.state.x += -1.0f * distanceX
                    }
                } else {
                    wxglRender.state.x += -1.0f * distanceX
                }
                panned = true
            }
            if (abs(distanceY) > 0.001f) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglRenders.forEach {
                        it.state.y += distanceY
                    }
                } else {
                    wxglRender.state.y += distanceY
                }
                panned = true
            }
            if (panned) {
                if (RadarPreferences.dualpaneshareposn) {
                    wxglSurfaceViews.forEach {
                        it.requestRender()
                    }
                } else {
                    requestRender()
                }
            }
            adjustToolbarsLock()
        }
    }

    private fun adjustToolbarsLock() {
        if (fullScreen || wxglRenders.size > 1) {
            if (!UIPreferences.lockToolbars) {
                toolbar!!.visibility = View.GONE
                toolbarBottom!!.visibility = View.GONE
                toolbarsHidden = true
            }
        }
    }

    override fun onShowPress(event: MotionEvent) {}

    override fun onSingleTapUp(event: MotionEvent): Boolean = true

    // used only if center on location enabled
    fun resetView() {
        density = (wxglRender.state.ortInt * 2.0f) / width
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        val zoomFactor = 1.0f
        if (RadarPreferences.wxoglCenterOnLocation) {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= zoomFactor
                wxglRenders.forEachIndexed { index, wxgl ->
                    wxgl.setViewInitial(mScaleFactor, wxgl.state.x * zoomFactor, wxgl.state.y * zoomFactor)
                    wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[index].requestRender()
                }
            } else {
                mScaleFactor *= zoomFactor
                wxglRender.setViewInitial(mScaleFactor, wxglRender.state.x * zoomFactor, wxglRender.state.y * zoomFactor)
                requestRender()
            }
            scaleFactorGlobal = mScaleFactor
            adjustToolbars()
            listener?.onProgressChanged(50000, index, idxInt)
        }
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        density = (wxglRender.state.ortInt * 2.0f) / width
        xPos = event.x
        yPos = event.y
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        if (!RadarPreferences.wxoglCenterOnLocation) {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= 2.0f
                wxglRenders.forEachIndexed { index, wxgl ->
                    wxgl.setViewInitial(
                            mScaleFactor,
                            wxgl.state.x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                            wxgl.state.y * 2.0f + (yMiddle - yPos) * -2.0f * density
                    )
                    wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[index].requestRender()
                }
            } else {
                mScaleFactor *= 2.0f
                wxglRender.setViewInitial(
                        mScaleFactor,
                        wxglRender.state.x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                        wxglRender.state.y * 2.0f + (yMiddle - yPos) * -2.0f * density
                )
                requestRender()
            }
        } else {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= 2.0f
                wxglRenders.forEachIndexed { index, wxgl ->
                    wxgl.setViewInitial(mScaleFactor, wxgl.state.x * 2.0f, wxgl.state.y * 2.0f)
                    wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[index].requestRender()
                }
            } else {
                mScaleFactor *= 2.0f
                wxglRender.setViewInitial(mScaleFactor, wxglRender.state.x * 2.0f, wxglRender.state.y * 2.0f)
                requestRender()
            }
        }
        scaleFactorGlobal = mScaleFactor
        adjustToolbars()
        listener?.onProgressChanged(50000, index, idxInt)
        return true
    }

    fun zoomInByKey(paneScaleFactor: Float = 1.0f) {
        density = (wxglRender.state.ortInt * 2.0f) / width
        mScaleFactor *= 2.0f * paneScaleFactor
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            wxglRenders.forEachIndexed { index, wxgl ->
                wxgl.setViewInitial(mScaleFactor, wxgl.state.x * 2.0f, wxgl.state.y * 2.0f)
                wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                wxglSurfaceViews[index].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.state.x * 2.0f, wxglRender.state.y * 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        adjustToolbars()
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean = true

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        mScaleFactor /= 2.0f
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            wxglRenders.forEachIndexed { index, wxgl ->
                wxgl.setViewInitial(mScaleFactor, wxgl.state.x / 2.0f, wxgl.state.y / 2.0f)
                wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                wxglSurfaceViews[index].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.state.x / 2.0f, wxglRender.state.y / 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        adjustToolbars()
        listener?.onProgressChanged(50000, index, idxInt)
        return true
    }

    fun zoomOutByKey(paneScaleFactor: Float = 1.0f) {
        mScaleFactor /= 2.0f / paneScaleFactor
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            wxglRenders.forEachIndexed { index, wxgl ->
                wxgl.setViewInitial(mScaleFactor, wxgl.state.x / 2.0f, wxgl.state.y / 2.0f)
                wxglSurfaceViews[index].mScaleFactor = mScaleFactor
                wxglSurfaceViews[index].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.state.x / 2.0f, wxglRender.state.y / 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        adjustToolbars()
    }

    private fun adjustToolbars() {
        if (!locationFragment && (fullScreen || wxglRenders.size > 1)) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener) {
        this.listener = listener
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(progress: Int, idx: Int, idxInt: Int)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        var height: Int
        if (wxglRenders.size == 1 || locationFragment) {
            if (fullScreen) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                width = MeasureSpec.getSize(widthMeasureSpec)
                this.setMeasuredDimension(width, width)
            }
        } else {
            width =
                    if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)
                        MyApplication.dm.widthPixels / widthDivider
                    else
                        MyApplication.dm.widthPixels / widthDivider
            if ((UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)) {
                height = MyApplication.dm.heightPixels / heightDivider
                if (wxglRenders.size == 2) {
                    height = MyApplication.dm.heightPixels / heightDivider
                }
            } else {
                height = MyApplication.dm.heightPixels / heightDivider - UIPreferences.actionBarHeight
            }
            this.setMeasuredDimension(width, height)
        }
    }

    val latLon: LatLon
        get() = LatLon(newY, newX * -1.0)

    var scaleFactor: Float
        get() = mScaleFactor
        set(mScaleFactor) {
            this.mScaleFactor = mScaleFactor
            scaleFactorGlobal = mScaleFactor
        }
}

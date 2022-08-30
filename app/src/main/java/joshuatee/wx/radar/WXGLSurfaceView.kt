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
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import kotlin.math.*

class WXGLSurfaceView : GLSurfaceView, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

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
    var newX = 0.0
        private set
    var newY = 0.0
        private set
    private var centerX = 0.0f
    private var centerY = 0.0f
    private var xPos = 0.0f
    private var yPos = 0.0f
    private var xMiddle = 0.0f
    private var yMiddle = 0.0f
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var numPanes = 0
    private var wxglRenders = listOf<WXGLRender>()
    private lateinit var wxglRender: WXGLRender
    private var density = 0.0f
    private var wxglSurfaceViews = listOf<WXGLSurfaceView>()
    var idxInt = 0
    private var widthDivider = 0
    private var heightDivider = 2
    val cities = mutableListOf<TextView>()
    val countyLabels= mutableListOf<TextView>()
    val observations = mutableListOf<TextView>()
    val pressureCenterLabels = mutableListOf<TextView>()
    val spotterLabels = mutableListOf<TextView>()
    val spotterTextView = mutableListOf<TextView>()
    var hailLabels = mutableListOf<TextView>()
    var hailTextView = mutableListOf<TextView>()
    var wxglTextObjects = mutableListOf<WXGLTextObject>()
    var locationFragment = false
    private var activity: Activity? = null

    constructor(context: Context, widthDivider: Int, numPanes: Int, heightDivider: Int) : super(context) {
        this.widthDivider = widthDivider
        this.heightDivider = heightDivider
        this.numPanes = numPanes
        gestureDetector = GestureDetector(context, this)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        gestureDetector = GestureDetector(context, this)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    fun setRenderVar(wxglRender: WXGLRender, wxglRenders: List<WXGLRender>, wxglSurfaceViews: List<WXGLSurfaceView>) {
        this.wxglRenders = wxglRenders
        this.wxglRender = wxglRender
        this.wxglSurfaceViews = wxglSurfaceViews
    }

    fun setRenderVar(wxglRender: WXGLRender, wxglRenders: List<WXGLRender>, wxglSurfaceViews: List<WXGLSurfaceView>, activity: Activity) {
        this.wxglRenders = wxglRenders
        this.wxglRender = wxglRender
        this.wxglSurfaceViews = wxglSurfaceViews
        this.activity = activity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!locationFragment) {
                    (0 until numPanes).forEach {
                        wxglTextObjects[it].hideLabels()
                        wxglRenders[it].displayHold = true
                    }
                }
                if (numPanes == 1 && fullScreen || numPanes > 1) UtilityUI.immersiveMode(activity!!)
            }
	        //elys mod - 3 finger to show conus radar map
            MotionEvent.ACTION_MOVE -> {
	            //elys mod
                //3 fingers press to show conus
                val count = event.pointerCount
                UtilityLog.d("wx", "Fingers Count: "+count)
                if (count == 3) {
                    if (!locationFragment) {
                        (0 until numPanes).forEach {
                            wxglTextObjects[it].hideLabels()
                            wxglRenders[it].displayHold = true
                            wxglRenders[it].displayConus = true
                        }
                    }
                } else {
                    if (!locationFragment) {
                        (0 until numPanes).forEach {
                            wxglTextObjects[it].hideLabels()
                            wxglRenders[it].displayHold = true
                            wxglRenders[it].displayConus = false
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                listener?.onProgressChanged(50000, index, idxInt)
                (0 until numPanes).forEach {
                    wxglRenders[it].displayHold = false
                    wxglSurfaceViews[it].requestRender()
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
                (0 until numPanes).forEach {
                    wxglRenders[it].x *= mScaleFactor / oldScaleFactor
                    wxglRenders[it].y *= mScaleFactor / oldScaleFactor
                    wxglRenders[it].zoom = mScaleFactor
                }
            } else {
                wxglRender.x *= mScaleFactor / oldScaleFactor
                wxglRender.y *= mScaleFactor / oldScaleFactor
                wxglRender.zoom = mScaleFactor
            }
            if (RadarPreferences.dualpaneshareposn) {
                (0 until numPanes).forEach {
                    wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[it].requestRender()
                }
            } else {
                requestRender()
            }
            scaleFactorGlobal = mScaleFactor
            return true
        }
    }

    override fun onDown(event: MotionEvent) = true

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float) = true

    override fun onLongPress(event: MotionEvent) {
        if (fullScreen) {
            toolbar!!.visibility = View.VISIBLE
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
        density = (wxglRender.ortInt * 2.0f) / width
        xPos = event.x
        yPos = event.y
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        val diffX = density * (xMiddle - xPos) / mScaleFactor
        val diffY = density * (yMiddle - yPos) / mScaleFactor
        val xStr = Utility.getRadarSiteX(wxglRender.rid)
        val yStr = Utility.getRadarSiteY(wxglRender.rid)
        centerX = xStr.toFloatOrNull() ?: 0.0f
        centerY = yStr.toFloatOrNull() ?: 0.0f
        val ppd = wxglRender.oneDegreeScaleFactor.toDouble()
        newX = centerY + (wxglRender.x / mScaleFactor + diffX) / ppd
        val test2 = 180.0 / PI * log(tan(PI / 4.0 + centerX * (PI / 180.0) / 2.0), E)
        newY = test2 + (-1.0 * wxglRender.y / mScaleFactor + diffY) / ppd
        newY = (180.0 / PI * (2.0 * atan(exp(newY * PI / 180.0)) - PI / 2.0))
        wxglRender.ridNewList = UtilityLocation.getNearestRadarSites(LatLon(newY, newX * -1), 5)
        listener?.onProgressChanged(index, index, idxInt)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        var panned = false
        if (!locationFragment && !RadarPreferences.wxoglCenterOnLocation) {
            if (distanceX != 0f) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglRenders[it].x += -1.0f * distanceX
                    }
                } else {
                    wxglRender.x += -1.0f * distanceX
                }
                panned = true
            }
            if (distanceY != 0f) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglRenders[it].y += distanceY
                    }
                } else {
                    wxglRender.y += distanceY
                }
                panned = true
            }
            if (panned) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglSurfaceViews[it].requestRender()
                    }
                } else {
                    requestRender()
                }
            }
            if (fullScreen || numPanes > 1) {
                if (!UIPreferences.lockToolbars) {
                    toolbar!!.visibility = View.GONE
                    toolbarBottom!!.visibility = View.GONE
                    toolbarsHidden = true
                }
            }
        }
        return panned
    }

    fun onScrollByKeyboard(distanceX: Float, distanceY: Float) {
        var panned = false
        if (!locationFragment) {
            if (distanceX != 0f) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglRenders[it].x += -1.0f * distanceX
                    }
                } else {
                    wxglRender.x += -1.0f * distanceX
                }
                panned = true
            }
            if (distanceY != 0f) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglRenders[it].y += distanceY
                    }
                } else {
                    wxglRender.y += distanceY
                }
                panned = true
            }
            if (panned) {
                if (RadarPreferences.dualpaneshareposn) {
                    (0 until numPanes).forEach {
                        wxglSurfaceViews[it].requestRender()
                    }
                } else {
                    requestRender()
                }
            }
            if (fullScreen || numPanes > 1) {
                if (!UIPreferences.lockToolbars) {
                    toolbar!!.visibility = View.GONE
                    toolbarBottom!!.visibility = View.GONE
                    toolbarsHidden = true
                }
            }
        }
    }

    override fun onShowPress(event: MotionEvent) {}

    override fun onSingleTapUp(event: MotionEvent) = true

    // used only if center on location enabled
    fun resetView() {
        density = (wxglRender.ortInt * 2.0f) / width
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        val zoomFactor = 1.0f
        if (RadarPreferences.wxoglCenterOnLocation) {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= zoomFactor
                (0 until numPanes).forEach {
                    wxglRenders[it].setViewInitial(mScaleFactor, wxglRenders[it].x * zoomFactor, wxglRenders[it].y * zoomFactor)
                    wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[it].requestRender()
                }
            } else {
                mScaleFactor *= zoomFactor
                wxglRender.setViewInitial(mScaleFactor, wxglRender.x * zoomFactor, wxglRender.y * zoomFactor)
                requestRender()
            }

            scaleFactorGlobal = mScaleFactor
            if (fullScreen || numPanes > 1) {
                toolbar!!.visibility = View.VISIBLE
                toolbarsHidden = false
                if (!archiveMode) {
                    toolbarBottom!!.visibility = View.VISIBLE
                }
            }
            listener?.onProgressChanged(50000, index, idxInt)
        }
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        density = (wxglRender.ortInt * 2.0f) / width
        xPos = event.x
        yPos = event.y
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        if (!RadarPreferences.wxoglCenterOnLocation) {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= 2.0f
                (0 until numPanes).forEach {
                    wxglRenders[it].setViewInitial(
                            mScaleFactor,
                            wxglRenders[it].x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                            wxglRenders[it].y * 2.0f + (yMiddle - yPos) * -2.0f * density
                    )
                    wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[it].requestRender()
                }
            } else {
                mScaleFactor *= 2.0f
                wxglRender.setViewInitial(
                        mScaleFactor,
                        wxglRender.x * 2.0f + (xPos - xMiddle) * -2.0f * density,
                        wxglRender.y * 2.0f + (yMiddle - yPos) * -2.0f * density
                )
                requestRender()
            }
        } else {
            if (RadarPreferences.dualpaneshareposn && !locationFragment) {
                mScaleFactor *= 2.0f
                (0 until numPanes).forEach {
                    wxglRenders[it].setViewInitial(mScaleFactor, wxglRenders[it].x * 2.0f, wxglRenders[it].y * 2.0f)
                    wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                    wxglSurfaceViews[it].requestRender()
                }
            } else {
                mScaleFactor *= 2.0f
                wxglRender.setViewInitial(mScaleFactor, wxglRender.x * 2.0f, wxglRender.y * 2.0f)
                requestRender()
            }
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
        listener?.onProgressChanged(50000, index, idxInt)
        return true
    }

    fun zoomInByKey(paneScaleFactor: Float = 1.0f) {
        density = (wxglRender.ortInt * 2.0f) / width
        mScaleFactor *= 2.0f * paneScaleFactor
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            (0 until numPanes).forEach {
                wxglRenders[it].setViewInitial(mScaleFactor, wxglRenders[it].x * 2.0f, wxglRenders[it].y * 2.0f)
                wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                wxglSurfaceViews[it].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.x * 2.0f, wxglRender.y * 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onDoubleTapEvent(event: MotionEvent) = true

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        mScaleFactor /= 2.0f
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            (0 until numPanes).forEach {
                wxglRenders[it].setViewInitial(mScaleFactor, wxglRenders[it].x / 2.0f, wxglRenders[it].y / 2.0f)
                wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                wxglSurfaceViews[it].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.x / 2.0f, wxglRender.y / 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
        listener?.onProgressChanged(50000, index, idxInt)
        return true
    }

    fun zoomOutByKey(paneScaleFactor: Float = 1.0f) {
        mScaleFactor /= 2.0f / paneScaleFactor
        if (RadarPreferences.dualpaneshareposn && !locationFragment) {
            (0 until numPanes).forEach {
                wxglRenders[it].setViewInitial(mScaleFactor, wxglRenders[it].x / 2.0f, wxglRenders[it].y / 2.0f)
                wxglSurfaceViews[it].mScaleFactor = mScaleFactor
                wxglSurfaceViews[it].requestRender()
            }
        } else {
            wxglRender.setViewInitial(mScaleFactor, wxglRender.x / 2.0f, wxglRender.y / 2.0f)
            requestRender()
        }
        scaleFactorGlobal = mScaleFactor
        if (fullScreen || numPanes > 1) {
            toolbar!!.visibility = View.VISIBLE
            toolbarsHidden = false
            if (!archiveMode) {
                toolbarBottom!!.visibility = View.VISIBLE
            }
        }
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
                if (numPanes == 2) {
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

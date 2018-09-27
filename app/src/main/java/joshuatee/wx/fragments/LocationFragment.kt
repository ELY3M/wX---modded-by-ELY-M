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

package joshuatee.wx.fragments

import android.annotation.SuppressLint
import java.util.Locale

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.AsyncTask
import android.os.Bundle
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AlertDialog
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import joshuatee.wx.radar.LatLon

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.SunMoonActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.radar.UtilityWXGLTextObject
import joshuatee.wx.radar.UtilityWXOGL
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCALegal
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCard7Day
import joshuatee.wx.ui.ObjectCardCC
import joshuatee.wx.ui.ObjectCardHSImage
import joshuatee.wx.ui.ObjectCardHSText
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radar.WXGLRender
import joshuatee.wx.radar.WXGLSurfaceView
import joshuatee.wx.radar.WXGLTextObject
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.spc.SPCSoundingsActivity
import joshuatee.wx.util.*
import joshuatee.wx.vis.USNWSGOESActivity

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType

class LocationFragment : Fragment(), OnItemSelectedListener, OnClickListener {

    // Displays the main content when wX is first opened including current conditions
    // hazards, 7 days and radar ( option )
    //

    private lateinit var sv: ScrollView
    private lateinit var spinner1: Spinner
    private var lastRefresh = 0.toLong()
    private var ccTime = ""
    private var radarTime = ""
    private var x = ""
    private var y = ""
    private var ts = ""
    private var tmpArr = Array(2) { _ -> "" }
    private var glviewInitialized = false
    private var hazardsSum = ""
    private var currentLoc = -1
    private var sevenDayExtShown = false
    private var hazardRaw = ""
    private lateinit var dataAdapter: ArrayAdapter<String>
    private lateinit var buttonFor: TextView
    private lateinit var intent: Intent
    private var locationCard: CardView? = null
    private var cardCC: ObjectCardCC? = null
    private var cv5: ObjectCard? = null
    private lateinit var ll: LinearLayout
    private val helpForecastGenericStatus = 1
    private val helpCurrentGeneric = 2
    private val helpForecastGeneric = 3
    private var homescreenFavLocal = ""
    private val cardsAl = mutableListOf<CardView>()
    private val hsTextAl = mutableListOf<ObjectCardHSText>()
    private val hsImageAl = mutableListOf<ObjectCardHSImage>()
    private var oglrArr = mutableListOf<WXGLRender>()
    private var glviewArr = mutableListOf<WXGLSurfaceView>()
    private var numRadars = 0
    private var oldRidArr = MutableList(2) { _ -> "" }
    private val radarLocationChangedAl = mutableListOf<Boolean>()
    // used to track the wxogl # for the wxogl that is tied to current location
    private var oglrIdx = -1
    // total # of wxogl
    private var oglrCnt = 0
    private var needForecastData = false
    private var bmCcSize = 300
    private var llCv5V: LinearLayout? = null
    // hazards
    private var llCv4V: LinearLayout? = null
    private val hazardsCardAl = mutableListOf<ObjectCardText>()
    private val hazardsExpandedAl = mutableListOf<Boolean>()
    private var dataNotInitialized = true
    private var alertDialogStatus: AlertDialog.Builder? = null
    private val checkedItem = -1
    private val alertDialogStatusAl = mutableListOf<String>()
    private var idxIntG = 0
    private var alertDialogRadarLongpress: AlertDialog.Builder? = null
    private val alertDialogRadarLongpressAl = mutableListOf<String>()
    private var wxgltextArr = mutableListOf<WXGLTextObject>()
    private var objFcst: ObjectForecastPackage? = null

    private fun addDynamicCards() {
        var ccAdded = false
        var day7Added = false
        val tmpArr = MyApplication.colon.split(homescreenFavLocal)
        numRadars = tmpArr.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        oldRidArr = MutableList(numRadars) { _ -> "" }
        val rlArr = mutableListOf<RelativeLayout>()
        glviewArr.clear()
        wxgltextArr.clear()
        var z = 0
        tmpArr.forEach { tok ->
            val widthDivider = 1
            val numPanes = 1
            if (tok == "TXT-CC" || tok == "TXT-CC2") {
                if (!ccAdded && cardCC != null) {
                    ll.addView(cardCC!!.card)
                    ccAdded = true
                }
            } else if (tok == "TXT-HAZ") {
                llCv4V = LinearLayout(activityReference)
                llCv4V?.orientation = LinearLayout.VERTICAL
                ll.addView(llCv4V)
            } else if (tok == "TXT-7DAY" || tok == "TXT-7DAY2") {
                if (!day7Added) {
                    if (tok.contains("TXT-7DAY")) {
                        ll.addView(llCv5V)
                    } else {
                        ll.addView(cv5?.card)
                        ll.addView(llCv5V)
                    }
                    day7Added = true
                }
            } else if (tok == "OGL-RADAR") {
                oglrArr.add(WXGLRender(activityReference))
                oglrIdx = oglrCnt
                oglrCnt += 1
                cardsAl.add(ObjectCard(activityReference).card)
                glviewArr.add(WXGLSurfaceView(activityReference, widthDivider, numPanes))
                oglrArr[z].rid = ""
                oldRidArr[z] = ""
                radarLocationChangedAl.add(false)
                glviewArr[z].idx = z
                rlArr.add(RelativeLayout(activityReference))
                wxgltextArr.add(WXGLTextObject(activityReference, rlArr[z], glviewArr[z], oglrArr[z], numPanes))
                glviewArr[z].wxgltextArr = wxgltextArr
                glviewArr[z].locfrag = true
                wxgltextArr[z].initTV(activityReference)
                rlArr[z].addView(glviewArr[z])
                cardsAl[cardsAl.size - 1].addView(rlArr[z])
                cardsAl[cardsAl.size - 1].layoutParams = RelativeLayout.LayoutParams(MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt())
                ll.addView(cardsAl[cardsAl.size - 1])
                z += 1
            } else if (tok.contains("TXT-")) {
                val hsTextTmp = ObjectCardHSText(activityReference, tok.replace("TXT-", ""))
                ll.addView(hsTextTmp.card)
                hsTextAl.add(hsTextTmp)
                hsTextTmp.tv.setOnClickListener { hsTextTmp.toggleText() }
            } else if (tok.contains("IMG-")) {
                val hsImageTmp = ObjectCardHSImage(activityReference, tok.replace("IMG-", ""))
                ll.addView(hsImageTmp.card)
                hsImageAl.add(hsImageTmp)
                setImageOnClick()
            } else if (tok.contains("NXRD-")) {
                oglrArr.add(WXGLRender(activityReference))
                oglrCnt += 1
                cardsAl.add(ObjectCard(activityReference).card)
                glviewArr.add(WXGLSurfaceView(activityReference, widthDivider, numPanes))
                glviewArr[z].idx = z
                oglrArr[z].rid = tok.replace("NXRD-", "")
                oldRidArr[z] = ""
                radarLocationChangedAl.add(false)
                rlArr.add(RelativeLayout(activityReference))
                wxgltextArr.add(WXGLTextObject(activityReference, rlArr[z], glviewArr[z], oglrArr[z], numPanes))
                glviewArr[z].wxgltextArr = wxgltextArr
                glviewArr[z].locfrag = true
                wxgltextArr[z].initTV(activityReference)
                rlArr[z].addView(glviewArr[z])
                cardsAl[cardsAl.size - 1].addView(rlArr[z])
                cardsAl[cardsAl.size - 1].layoutParams = RelativeLayout.LayoutParams(MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt())
                ll.addView(cardsAl[cardsAl.size - 1])
                z += 1
            }
        } // end of loop over HM tokens
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        alertDialogStatus()
        alertDialogRadarLongpress()
        val view: View = if (android.os.Build.VERSION.SDK_INT < 21 && UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB)
            inflater.inflate(R.layout.fragment_location_white, container, false)
        else
            inflater.inflate(R.layout.fragment_location, container, false)
        homescreenFavLocal = MyApplication.homescreenFav
        if (homescreenFavLocal.contains("TXT-CC") || homescreenFavLocal.contains("TXT-HAZ") || homescreenFavLocal.contains("TXT-7DAY"))
            needForecastData = true
        spinner1 = view.findViewById(R.id.spinner1)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
                ObjectSpinner.setupSpinner(spinner1, false)
            }
        }
        dataAdapter = ArrayAdapter(activityReference, R.layout.simple_spinner_item, Location.listOf)
        dataAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        ll = view.findViewById(R.id.ll)
        buttonFor = TextView(activityReference)
        buttonFor.gravity = Gravity.START
        buttonFor.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        buttonFor.setTextColor(UIPreferences.backgroundColor)
        buttonFor.setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
        locationCard = view.findViewById(R.id.cv1)
        cv5 = ObjectCard(activityReference)
        if (homescreenFavLocal.contains("TXT-CC2")) {
            cardCC = ObjectCardCC(activityReference, 2)
            cardCC?.imageView?.setOnClickListener {
                if (MyApplication.helpMode) {
                    showHelp(helpCurrentGeneric)
                } else {
                    alertDialogStatusAl.clear()
                    alertDialogStatusAl.add("Edit Location...")
                    alertDialogStatusAl.add("Sun/Moon data...")
                    alertDialogStatusAl.add("Force Data Refresh...")
                    if (MyApplication.locDisplayImg && Location.isUS) {
                        alertDialogStatusAl.add("Radar type: Reflectivity")
                        alertDialogStatusAl.add("Radar type: Velocity")
                        alertDialogStatusAl.add("Reset zoom and center")
                        alertDialogStatusAl += radarTimestamps
                    }
                    alertDialogStatus?.show()
                }
            }
        } else {
            cardCC = ObjectCardCC(activityReference, 1)
        }
        if (homescreenFavLocal.contains("TXT-7DAY")) {
            llCv5V = LinearLayout(activityReference)
            llCv5V?.orientation = LinearLayout.VERTICAL
            cv5?.setVisibility(View.GONE)
        } else {
            cv5?.addView(buttonFor)
        }
        addDynamicCards()
        cardCC?.let {
            it.textViewTop.setOnClickListener {
                if (Location.isUS) {
                    if (MyApplication.helpMode) {
                        showHelp(helpCurrentGeneric)
                    } else {
                        ObjectIntent(activityReference, SPCSoundingsActivity::class.java, SPCSoundingsActivity.URL, arrayOf(Utility.readPref("NWS" + Location.currentLocationStr, ""), ""))
                    }
                }
            }
        }
        cardCC?.let {
            it.textViewBottom.setOnClickListener {
                if (MyApplication.helpMode) {
                    showHelp(helpForecastGenericStatus)
                } else {
                    refreshDynamicContent()
                }
            }
        }
        buttonFor.setOnClickListener {
            if (MyApplication.helpMode) {
                showHelp(helpForecastGeneric)
            } else {
                if (sevenDayExtShown) {
                    buttonFor.text = objFcst!!.objSevenDay.sevenDayShort
                    sevenDayExtShown = false
                } else {
                    buttonFor.text = objFcst!!.objSevenDay.sevenDayExtStr + MyApplication.newline + MyApplication.newline + UtilityDownload.getSunriseSunset(activityReference, Location.currentLocationStr)
                    sevenDayExtShown = true
                }
            }
        }
        if (MyApplication.locDisplayImg) {
            glviewArr.indices.forEach { initGLVIEW(glviewArr[it], it) }
        }
        sv = view.findViewById(R.id.sv)
        spinner1.adapter = dataAdapter
        spinner1.onItemSelectedListener = this
        spinner1.setSelection(currentLoc)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLoc = Location.currentLocation
        if (currentLoc > Location.numLocations)
            currentLoc = Location.numLocations - 1
        else
            currentLoc -= 1
        bmCcSize = UtilityLocationFragment.setNWSIconSize()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (currentLoc != pos) {
            if (pos != Location.numLocations) {
                currentLoc = pos
                Utility.writePref(activityReference, "CURRENT_LOC_FRAGMENT", (pos + 1).toString())
                Location.currentLocationStr = (pos + 1).toString()
                x = Location.x
                y = Location.y
                if (oglrIdx != -1)
                    radarLocationChangedAl[oglrIdx] = false
                if (MyApplication.locDisplayImg && oglrIdx != -1) {
                    glviewArr[oglrIdx].scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
                    oglrArr[oglrIdx].setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
                }
                hsImageAl.forEach { it.resetZoom() }
                setImageOnClick()
                refreshDynamicContent()
            } else {
                ObjectIntent(activityReference, SettingsLocationGenericActivity::class.java, SettingsLocationGenericActivity.LOC_NUM, arrayOf((pos + 1).toString(), ""))
                spinner1.setSelection(currentLoc)
            }
        } // end check if current loc is pos
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun refreshDynamicContent() {
        locationCard?.let { UtilityUI.cardViewSetup(it) }
        sevenDayExtShown = false
        if (needForecastData) {
            getForecastData()
        }
        hsTextAl.indices.forEach { GetTEXT().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, it.toString()) }
        hsImageAl.indices.forEach { GetIMG().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, it.toString()) }
        x = Location.x
        y = Location.y
        if (MyApplication.locDisplayImg) {
            getAllRadars()
        }
        val currentTime = System.currentTimeMillis()
        lastRefresh = currentTime / 1000
        Utility.writePref(activityReference, "LOC_LAST_UPDATE", lastRefresh)
    }

    override fun onResume() {
        super.onResume()
        //sv.smoothScrollTo(0, 0)
        if (glviewInitialized) {
            glviewArr.forEach { it.onResume() }
        }
        LocalBroadcastManager.getInstance(activityReference).registerReceiver(onBroadcast, IntentFilter("locationadded"))
        updateSpinner()
        val currentTime = System.currentTimeMillis()
        val currentTimeSec = currentTime / 1000
        val refreshIntervalSec = (UIPreferences.refreshLocMin * 60).toLong()
        val xOld = x
        val yOld = y
        if (MyApplication.locDisplayImg) {
            if (!glviewInitialized) {
                glviewArr.indices.forEach { initGLVIEW(glviewArr[it], it) }
            }
        }
        if (UIPreferences.refreshLocMin != 0 || dataNotInitialized) {
            if (currentTimeSec > lastRefresh + refreshIntervalSec || Location.x != xOld || Location.y != yOld) {
                refreshDynamicContent()
            }
            dataNotInitialized = false
        }
    }

    override fun onClick(v2: View) {
        if (MyApplication.helpMode) showHelp(v2.id)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRadar internal constructor(internal val idx: Int) : AsyncTask<Int, String, String>() {

        override fun onPreExecute() {
            if (oglrIdx != -1)
                if (!radarLocationChangedAl[oglrIdx])
                    oglrArr[oglrIdx].rid = Location.rid
            if (oglrArr[idx].product == "N0Q" && WXGLNexrad.isRIDTDWR(oglrArr[idx].rid))
                oglrArr[idx].product = "TZL"
            if (oglrArr[idx].product == "TZL" && !WXGLNexrad.isRIDTDWR(oglrArr[idx].rid))
                oglrArr[idx].product = "N0Q"
            if (oglrArr[idx].product == "N0U" && WXGLNexrad.isRIDTDWR(oglrArr[idx].rid))
                oglrArr[idx].product = "TV0"
            if (oglrArr[idx].product == "TV0" && !WXGLNexrad.isRIDTDWR(oglrArr[idx].rid))
                oglrArr[idx].product = "N0U"
            initWXOGLGeom(glviewArr[idx], oglrArr[idx], idx)
        }

        override fun doInBackground(vararg params: Int?): String? {
            if (Location.isUS)
                oglrArr[idx].constructPolygons("", "", true)
            if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)
                oglrArr[idx].constructSpotters()
            else
                oglrArr[idx].deconstructSpotters()
            if (PolygonType.STI.pref)
                oglrArr[idx].constructSTILines()
            else
                oglrArr[idx].deconstructSTILines()
            if (PolygonType.HI.pref)
                oglrArr[idx].constructHI()
            else
                oglrArr[idx].deconstructHI()
            if (PolygonType.TVS.pref)
                oglrArr[idx].constructTVS()
            else
                oglrArr[idx].deconstructTVS()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (Location.isUS) {
                if (PolygonType.SPOTTER_LABELS.pref)
                    UtilityWXGLTextObject.updateSpotterLabels(numRadars, wxgltextArr)
                glviewArr[idx].requestRender()
                if (idx == oglrIdx) {
                    radarTime = radarTimeStamp
                    cardCC?.setStatus(ccTime + radarTime)
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetTEXT : AsyncTask<String, String, String>() {

        internal var l = 0
        internal var longText = ""
        internal var shortText = ""

        override fun doInBackground(vararg params: String): String {
            l = params[0].toIntOrNull() ?: 0
            if (activityReference != null) {
                longText = UtilityDownload.getTextProduct(activityReference, hsTextAl[l].product).replace("<br>AREA FORECAST DISCUSSION", "AREA FORECAST DISCUSSION")
            }
            hsTextAl[l].setTextLong(longText)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            shortText = UtilityStringExternal.truncate(longText, UIPreferences.homescreenTextLength)
            hsTextAl[l].setTextShort(shortText)
            hsTextAl[l].setText(shortText)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetIMG : AsyncTask<String, String, String>() {

        internal var b = UtilityImg.getBlankBitmap()
        internal var l = 0

        override fun doInBackground(vararg params: String): String {
            l = params[0].toIntOrNull() ?: 0
            if (activityReference != null) {
                b = UtilityDownload.getImgProduct(activityReference, hsImageAl[l].product)
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            hsImageAl[l].setImage(b)
        }
    }

    private fun showHelp(helpItem: Int) {
        when (helpItem) {
            helpForecastGenericStatus -> showHelpText(resources.getString(R.string.help_forecast_generic_status))
            helpCurrentGeneric -> showHelpText(resources.getString(R.string.help_current_generic))
            helpForecastGeneric -> showHelpText(resources.getString(R.string.help_forecast_generic))
        }
    }

    private fun showHelpText(helpStr: String) {
        UtilityAlertDialog.showHelpText(helpStr, activityReference)
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                idxIntG = idx
                alertDialogRadarLongpressAl.clear()
                val locX = x.toDoubleOrNull() ?: 0.0
                val locY = y.toDoubleOrNull() ?: 0.0
                val pointX = glviewArr[idx].newY.toDouble()
                val pointY = (glviewArr[idx].newX * -1).toDouble()
                val ridX = (Utility.readPref("RID_" + oglrArr[idx].rid + "_X", "0.0")).toDoubleOrNull()
                        ?: 0.0
                val ridY = -1.0 * ((Utility.readPref("RID_" + oglrArr[idx].rid + "_Y", "0.0")).toDoubleOrNull()
                        ?: 0.0)
                val dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
                val distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
                alertDialogRadarLongpress?.setTitle(UtilityStringExternal.truncate((glviewArr[idx].newY).toString(), 6) + ",-" + UtilityStringExternal.truncate((glviewArr[idx].newX).toString(), 6))
                alertDialogRadarLongpressAl.add(UtilityStringExternal.truncate(dist.toString(), 6) + " miles from location")
                alertDialogRadarLongpressAl.add(UtilityStringExternal.truncate(distRid.toString(), 6) + " miles from " + oglrArr[idx].rid)
                oglrArr[idx].ridNewList.mapTo(alertDialogRadarLongpressAl) { "Radar: (" + it.distance.toString() + " mi) " + it.name + " " + Utility.readPref("RID_LOC_" + it.name, "") }
                alertDialogRadarLongpressAl.add("Show warning text")
                alertDialogRadarLongpressAl.add("Show radar status message")
                alertDialogRadarLongpress?.show()
            } else {
                (0 until numRadars).forEach { wxgltextArr[it].addTV() }
            }
        }
    }

    private fun resetGLVIEW(glviewloc: WXGLSurfaceView, OGLRLOC: WXGLRender) {
        glviewloc.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        OGLRLOC.setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
        glviewloc.requestRender()
    }

    private fun initGLVIEW(glviewloc: WXGLSurfaceView, z: Int) {
        glviewloc.setEGLContextClientVersion(2)
        wxgltextArr[z].setOGLR(oglrArr[z])
        oglrArr[z].idxStr = z.toString()
        //glviewloc.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // a test to see if android emulator will now work
        glviewloc.setRenderer(oglrArr[z])
        glviewloc.setRenderVar(oglrArr[z], oglrArr, glviewArr)
        glviewloc.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glviewloc.setOnProgressChangeListener(changeListener)
        glviewInitialized = true
        oglrArr[z].zoom = MyApplication.wxoglSize.toFloat() / 10.0f
        glviewloc.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
    }

    private val radarTimeStamp: String
        get() {
            ts = ""
            val info = Utility.readPref("WX_RADAR_CURRENT_INFO", "")
            tmpArr = MyApplication.space.split(info)
            if (tmpArr.size > 3)
                ts = tmpArr[3]

            return if (oglrIdx != -1)
                " " + oglrArr[idxIntG].rid + ": " + ts
            else
                ""
        }

    private fun getRadarTimeStamp(str: String, j: Int): String {
        ts = ""
        tmpArr = MyApplication.space.split(str)
        if (tmpArr.size > 3)
            ts = tmpArr[3]
        return oglrArr[j].rid + ": " + ts + " (" + Utility.readPref("RID_LOC_" + oglrArr[j].rid, "") + ")"
    }

    private fun initWXOGLGeom(glviewloc: WXGLSurfaceView, OGLRLOC: WXGLRender, z: Int) {
        OGLRLOC.initGEOM()
        if (oldRidArr[z] != oglrArr[z].rid) {
            OGLRLOC.setChunkCount(0)
            OGLRLOC.setChunkCountSti(0)
            OGLRLOC.setHiInit(false)
            OGLRLOC.setTvsInit(false)
            Thread(Runnable {
                OGLRLOC.constructStateLines()
                glviewloc.requestRender()
            }).start()
            Thread(Runnable {
                if (GeographyType.LAKES.pref)
                    OGLRLOC.constructLakes()
                else
                    OGLRLOC.deconstructLakes()
            }).start()
            Thread(Runnable {
                if (GeographyType.COUNTY_LINES.pref) {
                    OGLRLOC.constructCounty()
                    glviewloc.requestRender()
                } else
                    OGLRLOC.deconstructCounty()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS.pref) {
                    OGLRLOC.constructHWLines()
                    glviewloc.requestRender()
                } else
                    OGLRLOC.deconstructHWLines()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS_EXTENDED.pref) {
                    OGLRLOC.constructHWEXTLines()
                    glviewloc.requestRender()
                } else
                    OGLRLOC.deconstructHWEXTLines()
            }).start()
            wxgltextArr[z].addTV()
            oldRidArr[z] = oglrArr[z].rid
        }
        Thread(Runnable {
            if (PolygonType.TOR.pref)
                OGLRLOC.constructWarningLines()
            else
                OGLRLOC.deconstructWarningLines()

            if (PolygonType.TST.pref)
                OGLRLOC.constructWarningLines()
            else
                OGLRLOC.deconstructWarningLines()

            if (PolygonType.FFW.pref)
                OGLRLOC.constructWarningLines()
            else
                OGLRLOC.deconstructWarningLines()

            if (PolygonType.SMW.pref)
                OGLRLOC.constructWarningLines()
            else
                OGLRLOC.deconstructWarningLines()

            if (PolygonType.SPS.pref)
                OGLRLOC.constructWarningLines()
            else
                OGLRLOC.deconstructWarningLines()

            if (PolygonType.MCD.pref)
                OGLRLOC.constructWATMCDLines()
            else
                OGLRLOC.deconstructWATMCDLines()
            if (PolygonType.MPD.pref)
                OGLRLOC.constructMPDLines()
            else
                OGLRLOC.deconstructMPDLines()
            glviewloc.requestRender()
        }).start()
        if (PolygonType.LOCDOT.pref) {
            OGLRLOC.constructLocationDot(Location.x, Location.y, false)
        } else {
            OGLRLOC.deconstructLocationDot()
        }
        glviewloc.requestRender()
    }

    override fun onPause() {
        if (glviewInitialized) {
            glviewArr.forEach { it.onPause() }
        }
        LocalBroadcastManager.getInstance(activityReference).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            updateSpinner()
        }
    }

    private fun updateSpinner() {
        lastRefresh = Utility.readPref(activityReference, "LOC_LAST_UPDATE", 0.toLong())
        dataAdapter = ArrayAdapter(activityReference, android.R.layout.simple_spinner_item, Location.listOf)
        dataAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        // fix for 2 or more loc deleted
        currentLoc = Location.currentLocation
        spinner1.adapter = dataAdapter
        spinner1.setSelection(Location.currentLocation)
    }

    private fun setImageOnClick() {
        hsImageAl.indices.forEach { ii ->
            val cl = MyApplication.HM_CLASS[hsImageAl[ii].product]
            val id = MyApplication.HM_CLASS_ID[hsImageAl[ii].product]
            val argsOrig = MyApplication.HM_CLASS_ARGS[hsImageAl[ii].product]
            hsImageAl[ii].setOnClickListener(OnClickListener {
                if (argsOrig != null) {
                    val args = arrayOfNulls<String>(argsOrig.size)
                    System.arraycopy(argsOrig, 0, args, 0, argsOrig.size)
                    args.indices.forEach { z ->
                        if (args[z] == "WFO_FOR_SND")
                            args[z] = UtilityLocation.getNearestSnd(activityReference, LatLon(Location.x, Location.y))
                        if (args[z] == "WFO_FOR_GOES")
                            args[z] = Location.wfo.toLowerCase(Locale.US)
                        if (args[z] == "STATE_LOWER")
                            args[z] = Location.state.toLowerCase(Locale.US)
                        if (args[z] == "STATE_UPPER")
                            args[z] = Location.state
                        if (args[z] == "RID_FOR_CA")
                            args[z] = Location.rid
                        if (args[z] == "ONEK")
                            args[z] = Utility.readPref("COD_1KM_" + Location.rid, "")
                        if (args[z] == "TWOK")
                            args[z] = Utility.readPref("STATE_CODE_" + Location.state, "")
                    }
                    if (cl != null && id != null) {
                        intent = Intent(activityReference, cl)
                        intent.putExtra(id, args)
                        startActivity(intent)
                    }
                } else {
                    ObjectIntent(activityReference, USNWSGOESActivity::class.java, USNWSGOESActivity.RID, arrayOf("nws", hsImageAl[ii].product.replace("IMG-", "").toLowerCase(Locale.US)))
                }
            })
        }
    }

    private fun getAllRadars() {
        glviewArr.indices.forEach {
            if (!(PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)) {
                GetRadar(it).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, it)
            } else {
                GetRadar(it).execute(it)
            }
        }
    }

    private fun resetAllGLView() {
        glviewArr.indices.forEach {
            resetGLVIEW(glviewArr[it], oglrArr[it])
            wxgltextArr[it].addTV()
        }
    }

    private val radarTimestamps: List<String>
        get() = (0 until glviewArr.size).mapTo(mutableListOf()) { getRadarTimeStamp(oglrArr[it].radarL3Object.timestamp, it) }

    private fun setupHazardCardsCA(hazUrl: String) {
        llCv4V?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCardAl.clear()
        hazardsExpandedAl.add(false)
        hazardsCardAl.add(ObjectCardText(activityReference))
        hazardsCardAl[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        hazardsCardAl[0].setTextColor(UIPreferences.textHighlightColor)
        hazardsCardAl[0].setText(hazUrl)
        val expandIndexCa = 0
        val hazUrlCa = objFcst!!.objHazards.hazards
        val hazardsSumCa = objFcst!!.objHazards.getHazardsShort()
        hazardsCardAl[0].setOnClickListener(OnClickListener {
            if (!hazardsExpandedAl[expandIndexCa]) {
                hazardsCardAl[expandIndexCa].setTextColor(UIPreferences.backgroundColor)
                hazardsCardAl[expandIndexCa].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
                hazardsCardAl[expandIndexCa].setText(Utility.fromHtml(hazUrlCa))
                hazardsExpandedAl[expandIndexCa] = true
            } else {
                hazardsCardAl[expandIndexCa].setTextColor(UIPreferences.textHighlightColor)
                hazardsCardAl[expandIndexCa].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
                hazardsCardAl[expandIndexCa].setText(hazardsSumCa)
                hazardsExpandedAl[expandIndexCa] = false
                sv.smoothScrollTo(0, 0)
            }
        })
        llCv4V?.addView(hazardsCardAl[0].card)
    }

    private fun alertDialogStatus() {
        alertDialogStatus = AlertDialog.Builder(activityReference)
        val arrayAdapterRadar = ArrayAdapter(activityReference, R.layout.simple_spinner_item, alertDialogStatusAl)
        arrayAdapterRadar.setDropDownViewResource(MyApplication.spinnerLayout)

        alertDialogStatus?.setNegativeButton(
                "Done"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialogStatus?.setSingleChoiceItems(arrayAdapterRadar, checkedItem
        ) { dialog, which ->
            val strName = alertDialogStatusAl[which]
            when {
                strName.contains("Edit Location..") -> ObjectIntent(activityReference, SettingsLocationGenericActivity::class.java, SettingsLocationGenericActivity.LOC_NUM, arrayOf(Location.currentLocationStr, ""))
                strName.contains("Sun/Moon data") -> ObjectIntent(activityReference, SunMoonActivity::class.java)
                strName.contains("Force Data Refresh") -> refreshDynamicContent()
                strName.contains("Radar type: Reflectivity") -> {
                    oglrArr[0].product = "N0Q"
                    getAllRadars()
                }
                strName.contains("Radar type: Velocity") -> {
                    oglrArr[0].product = "N0U"
                    getAllRadars()
                }
                strName.contains("Reset zoom and center") -> resetAllGLView()
                else -> {
                    val ridContext = strName.split(":")[0]
                    var stateContext = Utility.readPref("RID_LOC_$ridContext", "")
                    stateContext = stateContext.split(",")[0]
                    ObjectIntent(activityReference, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(ridContext, stateContext, oglrArr[0].product, ""))
                }
            }
            dialog.dismiss()
        }
    }

    private fun alertDialogRadarLongpress() {
        alertDialogRadarLongpress = AlertDialog.Builder(activityReference)
        val arrayAdapterRadar = ArrayAdapter(activityReference, R.layout.simple_spinner_item, alertDialogRadarLongpressAl)
        arrayAdapterRadar.setDropDownViewResource(MyApplication.spinnerLayout)
        alertDialogRadarLongpress?.setNegativeButton(
                "Cancel"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialogRadarLongpress?.setSingleChoiceItems(arrayAdapterRadar, checkedItem
        ) { dialog, which ->
            val strName = alertDialogRadarLongpressAl[which]
            if (strName.contains("Radar:")) {
                val ridNew = strName.parse("\\) ([A-Z]{3,4}) ")
                val oldRidIdx = oglrArr[idxIntG].rid
                oglrArr[idxIntG].rid = ridNew
                oglrArr[idxIntG].rid = ridNew
                if (idxIntG != oglrIdx) {
                    MyApplication.homescreenFav = MyApplication.homescreenFav.replace("NXRD-$oldRidIdx", "NXRD-" + oglrArr[idxIntG].rid)
                    Utility.writePref(activityReference, "HOMESCREEN_FAV", MyApplication.homescreenFav)
                }
                radarLocationChangedAl[idxIntG] = true
                glviewArr[idxIntG].scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
                oglrArr[idxIntG].setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
                GetRadar(idxIntG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, idxIntG)
            } else if (strName.contains("Show warning text")) {
                val polygonUrl = UtilityWXOGL.showTextProducts(glviewArr[idxIntG].newY.toDouble(), (glviewArr[idxIntG].newX * -1.0))
                if (polygonUrl != "") ObjectIntent(activityReference, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(polygonUrl, ""))
            } else if (strName.contains("Show radar status message")) {
                GetRadarStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            dialog.dismiss()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRadarStatus : AsyncTask<String, String, String>() {

        internal var radarStatus = ""

        override fun doInBackground(vararg params: String): String {
            radarStatus = UtilityDownload.getRadarStatusMessage(activityReference, oglrArr[idxIntG].rid)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), activityReference)
        }
    }

    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                mActivity = activity
            }
            return mActivity!!
        }

    private fun setupHazardCards(hazStr: String, idAl: List<String>) {
        llCv4V?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCardAl.clear()
        val tmpArr = hazStr.split(MyApplication.newline).dropLastWhile { it.isEmpty() }
        tmpArr.indices.forEach { z ->
            hazardsExpandedAl.add(false)
            hazardsCardAl.add(ObjectCardText(activityReference))
            hazardsCardAl[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            hazardsCardAl[z].setTextColor(UIPreferences.textHighlightColor)
            hazardsCardAl[z].setText(tmpArr[z].toUpperCase(Locale.US))
            val url = idAl[z]
            hazardsCardAl[z].setOnClickListener(OnClickListener { ObjectIntent(activityReference, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(url)) })
            llCv4V?.addView(hazardsCardAl[z].card)
        }
    }

    private fun getForecastData() {
        GetLocationForecast().execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetLocationForecast : AsyncTask<String, String, String>() {

        internal var bmCc: Bitmap? = null
        internal val bmArr = mutableListOf<Bitmap>()

        override fun doInBackground(vararg params: String): String {
            //
            // CC
            //
            try {
                objFcst = Utility.getCurrentConditionsV2(activityReference, Location.currentLocation)
                Utility.writePref(activityReference, "FCST", objFcst!!.objSevenDay.sevenDayExtStr)
                //hazardRaw = if (Location.isUS) {
                //    objFcst!!.objHazards.hazards
                //} else {
                //    objFcst!!.objHazards.hazards.getHtmlSep()
                //}
                hazardRaw = objFcst?.objHazards?.hazards ?: ""
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    bmCc = if (Location.isUS) {
                        UtilityNWS.getIconV2(activityReference, objFcst!!.objCC.iconUrl)
                    } else {
                        UtilityNWS.getIconV2(activityReference, UtilityCanada.translateIconNameCurrentConditions(objFcst!!.objCC.data1, objFcst!!.objCC.status))
                    }
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            //
            // 7day
            //
            try {
                Utility.writePref(activityReference, "FCST", objFcst!!.objSevenDay.sevenDayExtStr)
                if (homescreenFavLocal.contains("TXT-7DAY")) {
                    objFcst!!.objSevenDay.iconAl.mapTo(bmArr) { UtilityNWS.getIconV2(activityReference, it) }
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            //
            // hazards
            //
            //try {
            //    hazardRaw = objFcst!!.objHazards.hazards
            //} catch (e: Exception) { UtilityLog.HandleException(e) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (isAdded) {
                //
                // CC
                //
                bmCcSize = UtilityLocationFragment.setNWSIconSize()
                objFcst?.let {
                    cardCC?.let {
                        if (homescreenFavLocal.contains("TXT-CC2")) {
                            ccTime = objFcst!!.objCC.status
                            if (bmCc != null) {
                                it.updateContent(bmCc!!, bmCcSize, objFcst!!, Location.isUS, ccTime, radarTime)
                            }
                        } else {
                            it.setTopLine(objFcst!!.objCC.data1)
                            ccTime = objFcst!!.objCC.status
                            it.setStatus(ccTime + radarTime)
                        }
                    }
                }
                //
                // 7day
                //
                bmCcSize = UtilityLocationFragment.setNWSIconSize()
                objFcst?.let {
                    if (homescreenFavLocal.contains("TXT-7DAY")) {
                        llCv5V?.removeAllViewsInLayout()
                        val day7Arr = objFcst!!.objSevenDay.fcstList
                        bmArr.forEachIndexed { idx, bm ->
                            val c7day = ObjectCard7Day(activityReference, bm, Location.isUS, idx, day7Arr)
                            c7day.setOnClickListener(OnClickListener { sv.smoothScrollTo(0, 0) })
                            llCv5V?.addView(c7day.card)
                        }
                        // sunrise card
                        val cardSunrise = ObjectCardText(activityReference)
                        cardSunrise.center()
                        cardSunrise.lightText()
                        cardSunrise.setOnClickListener(OnClickListener { refreshDynamicContent() })
                        try {
                            if (Location.isUS) {
                                cardSunrise.setText(UtilityDownload.getSunriseSunset(activityReference, Location.currentLocationStr) + MyApplication.newline + UtilityTime.gmtTime())
                            } else {
                                cardSunrise.setText(UtilityDownload.getSunriseSunset(activityReference, Location.currentLocationStr) + MyApplication.newline + UtilityTime.gmtTime())
                            }
                        } catch (e: Exception) {
                            UtilityLog.HandleException(e)
                        }
                        llCv5V?.addView(cardSunrise.card)
                    } else {
                        buttonFor.text = objFcst!!.objSevenDay.sevenDayShort
                    }
                }
                //
                // CA Legal card
                //
                //
                // Canada legal card
                //
                if (!Location.isUS) {
                    val canLegal = ObjectCALegal(activityReference, UtilityCanada.getLocationURL(x, y))
                    if (homescreenFavLocal.contains("TXT-7DAY2")) {
                        llCv5V?.addView(canLegal.card)
                    }
                }
                //
                // hazards
                //
                if (Location.isUS) {
                    var hazardSumAsync = ""
                    //val idAl = hazardRaw.parseColumn("\"@id\": \"(.*?)\"")
                    val idAl = hazardRaw.parseColumn("\"id\": \"(http.*?)\"")
                    val hazardTitles = hazardRaw.parseColumn("\"event\": \"(.*?)\"")
                    hazardTitles.forEach { hazardSumAsync += it + MyApplication.newline }
                    if (hazardSumAsync == "") {
                        if (homescreenFavLocal.contains("TXT-HAZ")) {
                            llCv4V?.removeAllViews()
                            llCv4V?.visibility = View.GONE
                        }
                    } else {
                        if (homescreenFavLocal.contains("TXT-HAZ")) {
                            llCv4V?.visibility = View.VISIBLE
                            setupHazardCards(hazardSumAsync, idAl)
                        }
                    }
                    hazardsSum = hazardSumAsync
                } else {
                    objFcst?.let {
                        if (objFcst!!.objHazards.getHazardsShort() != "") {
                            hazardsSum = objFcst!!.objHazards.getHazardsShort().toUpperCase(Locale.US)
                            if (homescreenFavLocal.contains("TXT-HAZ")) {
                                llCv4V?.visibility = View.VISIBLE
                                setupHazardCardsCA(hazardsSum)
                            }
                        }
                    }
                } // end hazard check
            } // end isAdded() check
        }
    }
}


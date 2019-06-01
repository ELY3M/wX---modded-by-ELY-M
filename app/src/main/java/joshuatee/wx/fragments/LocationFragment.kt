/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
//hail size texts
//OBS text fix
//spotter - us location removed

package joshuatee.wx.fragments

import android.content.*
import java.util.Locale

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.TypedValue
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
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.*
import joshuatee.wx.ui.*
import joshuatee.wx.vis.GoesActivity
import kotlinx.coroutines.*

class LocationFragment : Fragment(), OnItemSelectedListener, OnClickListener {

    // Displays the main content when wX is first opened including current conditions
    // hazards, 7 days and radar ( option )
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var scrollView: ScrollView
    private lateinit var spinner: Spinner
    private var lastRefresh = 0.toLong()
    private var ccTime = ""
    private var radarTime = ""
    private var x = ""
    private var y = ""
    private var ts = ""
    private var glviewInitialized = false
    private var currentLoc = -1
    private var sevenDayExtShown = false
    private lateinit var dataAdapter: ArrayAdapter<String>
    private lateinit var intent: Intent
    private var locationCard: CardView? = null
    private var cardCC: ObjectCardCC? = null
    private lateinit var linearLayout: LinearLayout
    private val helpForecastGenericStatus = 1
    private val helpCurrentGeneric = 2
    private val helpForecastGeneric = 3
    private var homescreenFavLocal = ""
    private val cardViews = mutableListOf<CardView>()
    private val hsTextAl = mutableListOf<ObjectCardHSText>()
    private val hsImages = mutableListOf<ObjectCardHSImage>()
    private var oglrArr = mutableListOf<WXGLRender>()
    private var glviewArr = mutableListOf<WXGLSurfaceView>()
    private var wxgltextArr = mutableListOf<WXGLTextObject>()
    private var numRadars = 0
    private var oldRidArr = Array(2) { "" }
    private val radarLocationChangedAl = mutableListOf<Boolean>()
    // used to track the wxogl # for the wxogl that is tied to current location
    private var oglrIdx = -1
    // total # of wxogl
    private var oglrCnt = 0
    private var needForecastData = false
    private var linearLayoutForecast: LinearLayout? = null
    // hazards
    private var linearLayoutHazards: LinearLayout? = null
    private val hazardsCards = mutableListOf<ObjectCardText>()
    private val hazardsExpandedAl = mutableListOf<Boolean>()
    private var dataNotInitialized = true
    private var alertDialogStatus: ObjectDialogue? = null
    private val alertDialogStatusList = mutableListOf<String>()
    private var idxIntG = 0
    private var alertDialogRadarLongPress: ObjectDialogue? = null
    private val alertDialogRadarLongpressAl = mutableListOf<String>()
    private var objCc = ObjectForecastPackageCurrentConditions()
    private var objHazards = ObjectForecastPackageHazards()
    private var objSevenDay = ObjectForecastPackage7Day()
    private var locationChangedSevenDay = false
    private var locationChangedHazards = false
    private var numPanesArr = listOf<Int>()

    private fun addDynamicCards() {
        var ccAdded = false
        var day7Added = false
        val homeScreenTokens = MyApplication.colon.split(homescreenFavLocal)
        numRadars = homeScreenTokens.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        oldRidArr = Array(numRadars) { "" }
        val rlArr = mutableListOf<RelativeLayout>()
        glviewArr.clear()
        wxgltextArr.clear()
        var index = 0
        homeScreenTokens.forEach { tok ->
            val widthDivider = 1
            val numPanes = 1
            if (tok == "TXT-CC" || tok == "TXT-CC2") {
                if (!ccAdded && cardCC != null) {
                    linearLayout.addView(cardCC!!.card)
                    ccAdded = true
                }
            } else if (tok == "TXT-HAZ") {
                linearLayoutHazards = LinearLayout(activityReference)
                linearLayoutHazards?.orientation = LinearLayout.VERTICAL
                linearLayout.addView(linearLayoutHazards)
            } else if (tok == "TXT-7DAY" || tok == "TXT-7DAY2") {
                if (!day7Added) {
                    linearLayout.addView(linearLayoutForecast)
                    day7Added = true
                }
            } else if (tok == "OGL-RADAR") {
                oglrArr.add(WXGLRender(activityReference))
                oglrIdx = oglrCnt
                oglrCnt += 1
                cardViews.add(ObjectCard(activityReference).card)
                glviewArr.add(WXGLSurfaceView(activityReference, widthDivider, numPanes, 1))
                oglrArr[index].rid = ""
                oldRidArr[index] = ""
                radarLocationChangedAl.add(false)
                glviewArr[index].idx = index
                rlArr.add(RelativeLayout(activityReference))
                wxgltextArr.add(
                        WXGLTextObject(
                                activityReference,
                                rlArr[index],
                                glviewArr[index],
                                oglrArr[index],
                                numPanes
                        )
                )
                glviewArr[index].wxgltextArr = wxgltextArr
                glviewArr[index].locfrag = true
                wxgltextArr[index].initTV(activityReference)
                rlArr[index].addView(glviewArr[index])
                cardViews.last().addView(rlArr[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()
                )
                linearLayout.addView(cardViews.last())
                index += 1
            } else if (tok.contains("TXT-")) {
                val hsTextTmp = ObjectCardHSText(activityReference, tok.replace("TXT-", ""))
                linearLayout.addView(hsTextTmp.card)
                hsTextAl.add(hsTextTmp)
                hsTextTmp.tv.setOnClickListener { hsTextTmp.toggleText() }
            } else if (tok.contains("IMG-")) {
                val hsImageTmp = ObjectCardHSImage(activityReference, tok.replace("IMG-", ""))
                linearLayout.addView(hsImageTmp.card)
                hsImages.add(hsImageTmp)
                setImageOnClick()
            } else if (tok.contains("NXRD-")) {
                oglrArr.add(WXGLRender(activityReference))
                oglrCnt += 1
                cardViews.add(ObjectCard(activityReference).card)
                glviewArr.add(WXGLSurfaceView(activityReference, widthDivider, numPanes, 1))
                glviewArr[index].idx = index
                oglrArr[index].rid = tok.replace("NXRD-", "")
                oldRidArr[index] = ""
                radarLocationChangedAl.add(false)
                rlArr.add(RelativeLayout(activityReference))
                wxgltextArr.add(
                        WXGLTextObject(
                                activityReference,
                                rlArr[index],
                                glviewArr[index],
                                oglrArr[index],
                                numPanes
                        )
                )
                glviewArr[index].wxgltextArr = wxgltextArr
                glviewArr[index].locfrag = true
                wxgltextArr[index].initTV(activityReference)
                rlArr[index].addView(glviewArr[index])
                cardViews.last().addView(rlArr[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()
                )
                linearLayout.addView(cardViews.last())
                index += 1
            }
        } // end of loop over HM tokens
        numPanesArr = (0 until glviewArr.size).toList()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setupAlertDialogStatus()
        setupAlertDialogRadarLongPress()
        val view: View =
                if (android.os.Build.VERSION.SDK_INT < 21 && UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB)
                    inflater.inflate(R.layout.fragment_location_white, container, false)
                else
                    inflater.inflate(R.layout.fragment_location, container, false)
        homescreenFavLocal = MyApplication.homescreenFav
        if (homescreenFavLocal.contains("TXT-CC") || homescreenFavLocal.contains("TXT-HAZ") || homescreenFavLocal.contains(
                        "TXT-7DAY"
                )
        )
            needForecastData = true
        spinner = view.findViewById(R.id.spinner1)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
                UtilityUI.setupSpinner(spinner, false)
            }
        }
        dataAdapter = ArrayAdapter(activityReference, R.layout.simple_spinner_item, Location.listOf)
        dataAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        linearLayout = view.findViewById(R.id.ll)
        locationCard = view.findViewById(R.id.cv1)
        if (homescreenFavLocal.contains("TXT-CC2")) {
            cardCC = ObjectCardCC(activityReference, 2)
            cardCC?.setListener(
                    alertDialogStatus,
                    alertDialogStatusList,
                    ::radarTimestamps,
                    helpCurrentGeneric,
                    ::showHelp
            )
        } else {
            cardCC = ObjectCardCC(activityReference, 1)
        }
        if (homescreenFavLocal.contains("TXT-7DAY")) {
            linearLayoutForecast = LinearLayout(activityReference)
            linearLayoutForecast?.orientation = LinearLayout.VERTICAL
        }
        addDynamicCards()
        cardCC?.let { objectCardCC ->
            objectCardCC.textViewTop.setOnClickListener(OnClickListener {
                if (Location.isUS) {
                    if (MyApplication.helpMode) {
                        showHelp(helpCurrentGeneric)
                    }
                }
            })
            objectCardCC.textViewBottom.setOnClickListener(OnClickListener {
                if (MyApplication.helpMode) {
                    showHelp(helpForecastGenericStatus)
                }
            })
        }
        refreshDynamicContent()
        if (MyApplication.locDisplayImg) {
            glviewArr.indices.forEach {
                glviewInitialized = UtilityRadarUI.initGlviewFragment(
                        glviewArr[it],
                        it,
                        oglrArr,
                        glviewArr,
                        wxgltextArr,
                        changeListener
                )
            }
        }
        scrollView = view.findViewById(R.id.sv)
        spinner.adapter = dataAdapter
        spinner.onItemSelectedListener = this
        spinner.setSelection(currentLoc)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLoc = Location.currentLocation
        if (currentLoc > Location.numLocations)
            currentLoc = Location.numLocations - 1
        else
            currentLoc -= 1
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (currentLoc != pos) {
            locationChangedHazards = true
            locationChangedSevenDay = true
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
                    oglrArr[oglrIdx].setViewInitial(
                            MyApplication.wxoglSize.toFloat() / 10.0f,
                            0.0f,
                            0.0f
                    )
                }
                hsImages.forEach { it.resetZoom() }
                setImageOnClick()
                refreshDynamicContent()
            } else {
                ObjectIntent(
                        activityReference,
                        SettingsLocationGenericActivity::class.java,
                        SettingsLocationGenericActivity.LOC_NUM,
                        arrayOf((pos + 1).toString(), "")
                )
                spinner.setSelection(currentLoc)
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
        hsTextAl.indices.forEach { getTextProduct(it.toString()) }
        hsImages.indices.forEach { getImageProduct(it.toString()) }
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
        if (glviewInitialized) {
            glviewArr.forEach { it.onResume() }
        }
        LocalBroadcastManager.getInstance(activityReference)
                .registerReceiver(onBroadcast, IntentFilter("locationadded"))
        updateSpinner()
        val currentTime = System.currentTimeMillis()
        val currentTimeSec = currentTime / 1000
        val refreshIntervalSec = (UIPreferences.refreshLocMin * 60).toLong()
        val xOld = x
        val yOld = y
        if (MyApplication.locDisplayImg) {
            if (!glviewInitialized) {
                glviewArr.indices.forEach {
                    glviewInitialized = UtilityRadarUI.initGlviewFragment(
                            glviewArr[it],
                            it,
                            oglrArr,
                            glviewArr,
                            wxgltextArr,
                            changeListener
                    )
                }
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
        if (MyApplication.helpMode)
            showHelp(v2.id)
    }

    private fun getRadar(idx: Int) = GlobalScope.launch(uiDispatcher) {
        if (oglrIdx != -1)
            if (!radarLocationChangedAl[oglrIdx])
                oglrArr[oglrIdx].rid = Location.rid
        if (oglrArr[idx].product == "N0Q" && WXGLNexrad.isRidTdwr(oglrArr[idx].rid))
            oglrArr[idx].product = "TZL"
        if (oglrArr[idx].product == "TZL" && !WXGLNexrad.isRidTdwr(oglrArr[idx].rid))
            oglrArr[idx].product = "N0Q"
        if (oglrArr[idx].product == "N0U" && WXGLNexrad.isRidTdwr(oglrArr[idx].rid))
            oglrArr[idx].product = "TV0"
        if (oglrArr[idx].product == "TV0" && !WXGLNexrad.isRidTdwr(oglrArr[idx].rid))
            oglrArr[idx].product = "N0U"
        UtilityRadarUI.initWxOglGeom(
                glviewArr[idx],
                oglrArr[idx],
                idx,
                oldRidArr,
                oglrArr,
                wxgltextArr,
                numPanesArr,
                null,
                glviewArr,
                ::getGPSFromDouble,
                ::getLatLon
        )
        withContext(Dispatchers.IO) {
            if (Location.isUS) {
                UtilityRadarUI.plotRadar(
                        oglrArr[idx],
                        "",
                        activityReference,
                        ::getGPSFromDouble,
                        ::getLatLon,
                        false
                )
            }
        }

        //hmmm not all spotters are in us//
        //if (Location.isUS) {
        if (PolygonType.SPOTTER_LABELS.pref) {
            UtilityWXGLTextObject.updateSpotterLabels(numRadars, wxgltextArr)
        }
        //}

        //FIXME - make obs texts work on multi radars on home screen and multi-pane
        //update OBS texts
        if (PolygonType.OBS.pref) {
            UtilityWXGLTextObject.updateObs(numRadars, wxgltextArr)
        }

        //update hail size texts
	    //TODO test this with multi radars and panes!
        if (PolygonType.HAIL_LABELS.pref) {
            UtilityWXGLTextObject.updateHailLabels(numRadars, wxgltextArr)
	    }
	    
	    
            glviewArr[idx].requestRender()
            if (idx == oglrIdx) {
                radarTime = radarTimeStamp
                cardCC?.setStatus(ccTime + radarTime)
            }
       // }
    }

    private fun getTextProduct(productString: String) = GlobalScope.launch(uiDispatcher) {
        val productIndex = productString.toIntOrNull() ?: 0
        val longText = withContext(Dispatchers.IO) {
            UtilityDownload.getTextProduct(
                    MyApplication.appContext,
                    hsTextAl[productIndex].product
            ).replace("<br>AREA FORECAST DISCUSSION", "AREA FORECAST DISCUSSION")
        }
        hsTextAl[productIndex].setTextLong(longText)
        val shortText = UtilityStringExternal.truncate(longText, UIPreferences.homescreenTextLength)
        hsTextAl[productIndex].setTextShort(shortText)
        hsTextAl[productIndex].setText(shortText)
    }

    private fun getImageProduct(productString: String) = GlobalScope.launch(uiDispatcher) {
        val productIndex = productString.toIntOrNull() ?: 0
        val b = withContext(Dispatchers.IO) {
            UtilityDownload.getImageProduct(
                    MyApplication.appContext,
                    hsImages[productIndex].product
            )
        }
        hsImages[productIndex].setImage(b)
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
                UtilityRadarUI.addItemsToLongPress(
                        alertDialogRadarLongpressAl,
                        x,
                        y,
                        activityReference,
                        glviewArr[idx],
                        oglrArr[idx],
                        alertDialogRadarLongPress!!
                )
            } else {
                (0 until numRadars).forEach { wxgltextArr[it].addTV() }
            }
        }
    }

    private val radarTimeStamp: String
        get() {
            ts = ""
            val info = Utility.readPref("WX_RADAR_CURRENT_INFO", "")
            val tmpArr = MyApplication.space.split(info)
            if (tmpArr.size > 3)
                ts = tmpArr[3]

            return if (oglrIdx != -1)
                " " + oglrArr[idxIntG].rid + ": " + ts
            else
                ""
        }

    private fun getRadarTimeStamp(str: String, j: Int): String {
        ts = ""
        val tmpArr = MyApplication.space.split(str)
        if (tmpArr.size > 3)
            ts = tmpArr[3]
        return oglrArr[j].rid + ": " + ts + " (" + Utility.readPref(
                "RID_LOC_" + oglrArr[j].rid,
                ""
        ) + ")"
    }

    private fun getGPSFromDouble() {
    }

    private fun getLatLon() = LatLon(Location.x, Location.y)

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
        dataAdapter = ArrayAdapter(
                activityReference,
                android.R.layout.simple_spinner_item,
                Location.listOf
        )
        dataAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        // fix for 2 or more loc deleted
        currentLoc = Location.currentLocation
        spinner.adapter = dataAdapter
        spinner.setSelection(Location.currentLocation)
    }

    private fun setImageOnClick() {
        hsImages.indices.forEach { ii ->
            val cl = MyApplication.HM_CLASS[hsImages[ii].product]
            val id = MyApplication.HM_CLASS_ID[hsImages[ii].product]
            val argsOrig = MyApplication.HM_CLASS_ARGS[hsImages[ii].product]
            hsImages[ii].setOnClickListener(OnClickListener {
                if (argsOrig != null) {
                    val args = arrayOfNulls<String>(argsOrig.size)
                    System.arraycopy(argsOrig, 0, args, 0, argsOrig.size)
                    args.indices.forEach { z ->
                        if (args[z] == "WFO_FOR_SND")
                            args[z] = UtilityLocation.getNearestSnd(
                                    activityReference,
                                    LatLon(Location.x, Location.y)
                            )
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
                    ObjectIntent(
                            activityReference,
                            GoesActivity::class.java,
                            GoesActivity.RID,
                            arrayOf("")
                    )
                }
            })
        }
    }

    private fun getAllRadars() {
        glviewArr.indices.forEach {
            if (!(PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)) {
                getRadar(it)
            } else {
                getRadar(it)
            }
        }
    }

    private fun resetAllGlview() {
        glviewArr.indices.forEach {
            UtilityRadarUI.resetGlview(glviewArr[it], oglrArr[it])
            wxgltextArr[it].addTV()
        }
    }

    private fun radarTimestamps(): List<String> {
        return (0 until glviewArr.size).mapTo(mutableListOf()) {
            getRadarTimeStamp(
                    oglrArr[it].radarL3Object.timestamp,
                    it
            )
        }
    }

    private fun setupHazardCardsCA(hazUrl: String) {
        linearLayoutHazards?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        hazardsExpandedAl.add(false)
        hazardsCards.add(ObjectCardText(activityReference))
        hazardsCards[0].setPaddingAmount(MyApplication.paddingSettings)
        hazardsCards[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        hazardsCards[0].setTextColor(UIPreferences.textHighlightColor)
        hazardsCards[0].setText(hazUrl)
        val expandIndexCa = 0
        val hazUrlCa = objHazards.hazards
        val hazardsSumCa = objHazards.getHazardsShort()
        hazardsCards[0].setOnClickListener(OnClickListener {
            if (!hazardsExpandedAl[expandIndexCa]) {
                hazardsCards[expandIndexCa].setTextColor(UIPreferences.backgroundColor)
                hazardsCards[expandIndexCa].setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        MyApplication.textSizeSmall
                )
                hazardsCards[expandIndexCa].setText(Utility.fromHtml(hazUrlCa))
                hazardsExpandedAl[expandIndexCa] = true
            } else {
                hazardsCards[expandIndexCa].setTextColor(UIPreferences.textHighlightColor)
                hazardsCards[expandIndexCa].setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        MyApplication.textSizeNormal
                )
                hazardsCards[expandIndexCa].setText(hazardsSumCa)
                hazardsExpandedAl[expandIndexCa] = false
                scrollView.smoothScrollTo(0, 0)
            }
        })
        linearLayoutHazards?.addView(hazardsCards[0].card)
    }

    private fun setupAlertDialogStatus() {
        alertDialogStatus = ObjectDialogue(activityReference, alertDialogStatusList)
        alertDialogStatus!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        alertDialogStatus!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogStatusList[which]
            if (oglrArr.size > 0) {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        oglrArr[0],
                        activityReference,
                        ::refreshDynamicContent,
                        ::resetAllGlview,
                        ::getAllRadars
                )
            } else {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        null,
                        activityReference,
                        ::refreshDynamicContent,
                        ::resetAllGlview,
                        ::getAllRadars
                )
            }
            dialog.dismiss()
        })
    }

    private fun setupAlertDialogRadarLongPress() {
        alertDialogRadarLongPress = ObjectDialogue(activityReference, alertDialogRadarLongpressAl)
        alertDialogRadarLongPress!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        alertDialogRadarLongPress!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogRadarLongpressAl[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    activityReference,
                    activityReference,
                    glviewArr[idxIntG],
                    oglrArr[idxIntG],
                    uiDispatcher,
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        })
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        val ridNew = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
        val oldRidIdx = oglrArr[idxIntG].rid
        oglrArr[idxIntG].rid = ridNew
        if (idxIntG != oglrIdx) {
            MyApplication.homescreenFav = MyApplication.homescreenFav.replace(
                    "NXRD-$oldRidIdx",
                    "NXRD-" + oglrArr[idxIntG].rid
            )
            Utility.writePref(
                    activityReference,
                    "HOMESCREEN_FAV",
                    MyApplication.homescreenFav
            )
        }
        radarLocationChangedAl[idxIntG] = true
        glviewArr[idxIntG].scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        oglrArr[idxIntG].setViewInitial(
                MyApplication.wxoglSize.toFloat() / 10.0f,
                0.0f,
                0.0f
        )
        getRadar(idxIntG)
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

    // FIXME change to return context and use getContext in API greater then 22
    // FIXME duplicate for 2 other areas

    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                if (android.os.Build.VERSION.SDK_INT >= 23 ) {
                    mActivity = activity
                    //val test = context
                } else {
                    mActivity = activity
                }
            }
            return mActivity!!
        }

    private fun setupHazardCards() {
        linearLayoutHazards?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        objHazards.titles.indices.forEach { z ->
            hazardsExpandedAl.add(false)
            hazardsCards.add(ObjectCardText(activityReference))
            hazardsCards[z].setPaddingAmount(MyApplication.paddingSettings)
            hazardsCards[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            hazardsCards[z].setTextColor(UIPreferences.textHighlightColor)
            hazardsCards[z].setText(objHazards.titles[z].toUpperCase(Locale.US))
            hazardsCards[z].setOnClickListener(OnClickListener {
                ObjectIntent(
                        activityReference,
                        USAlertsDetailActivity::class.java,
                        USAlertsDetailActivity.URL,
                        arrayOf(objHazards.urls[z])
                )
            })
            linearLayoutHazards?.addView(hazardsCards[z].card)
        }
    }

    private fun getForecastData() {
        getLocationForecast()
        getLocationForecastSevenDay()
        getLocationHazards()
    }

    private fun getLocationForecast() = GlobalScope.launch(uiDispatcher) {
        var bitmapForCurrentConditions: Bitmap? = null
        //
        // Current Conditions
        //
        withContext(Dispatchers.IO) {
            try {
                objCc = ObjectForecastPackageCurrentConditions(activityReference, Location.currentLocation)
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    bitmapForCurrentConditions = if (Location.isUS) {
                        UtilityNws.getIcon(activityReference, objCc.iconUrl)
                    } else {
                        UtilityNws.getIcon(
                                activityReference,
                                UtilityCanada.translateIconNameCurrentConditions(
                                        objCc.data,
                                        objCc.status
                                )
                        )
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            //
            // Current Conditions
            //
            cardCC?.let {
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    ccTime = objCc.status
                    if (bitmapForCurrentConditions != null) {
                        it.updateContent(
                                bitmapForCurrentConditions!!,
                                objCc,
                                Location.isUS,
                                ccTime,
                                radarTime
                        )
                    }
                } else {
                    it.setTopLine(objCc.data)
                    ccTime = objCc.status
                    it.setStatus(ccTime + radarTime)
                }
            }
        }
    }

    private fun getLocationForecastSevenDay() = GlobalScope.launch(uiDispatcher) {
        val bitmaps = mutableListOf<Bitmap>()
        if (locationChangedSevenDay) {
            linearLayoutForecast?.removeAllViewsInLayout()
            locationChangedSevenDay = false
        }
        withContext(Dispatchers.IO) {
            try {
                objSevenDay = ObjectForecastPackage7Day(Location.currentLocation)
                Utility.writePref(activityReference, "FCST", objSevenDay.sevenDayLong)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            try {
                Utility.writePref(activityReference, "FCST", objSevenDay.sevenDayLong)
                if (homescreenFavLocal.contains("TXT-7DAY")) {
                    objSevenDay.icons.mapTo(bitmaps) {
                        UtilityNws.getIcon(
                                activityReference,
                                it
                        )
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            if (homescreenFavLocal.contains("TXT-7DAY")) {
                linearLayoutForecast?.removeAllViewsInLayout()
                val day7Arr = objSevenDay.forecastList
                bitmaps.forEachIndexed { index, bitmap ->
                    val c7day = ObjectCard7Day(activityReference, bitmap, Location.isUS, index, day7Arr)
                    c7day.setOnClickListener(OnClickListener {
                        scrollView.smoothScrollTo(
                                0,
                                0
                        )
                    })
                    linearLayoutForecast?.addView(c7day.card)
                }
                // sunrise card
                val cardSunrise = ObjectCardText(activityReference)
                cardSunrise.center()
                cardSunrise.setOnClickListener(OnClickListener {
                    scrollView.smoothScrollTo(
                            0,
                            0
                    )
                })
                try {
                    if (Location.isUS) {
                        cardSunrise.setText(
                                UtilityTimeSunMoon.getSunriseSunset(
                                        activityReference,
                                        Location.currentLocationStr
                                ) + MyApplication.newline + UtilityTime.gmtTime()
                        )
                    } else {
                        cardSunrise.setText(
                                UtilityTimeSunMoon.getSunriseSunset(
                                        activityReference,
                                        Location.currentLocationStr
                                ) + MyApplication.newline + UtilityTime.gmtTime()
                        )
                    }
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
                linearLayoutForecast?.addView(cardSunrise.card)
            }
            //
            // Canada legal card
            //
            if (!Location.isUS) {
                if (homescreenFavLocal.contains("TXT-7DAY2")) {
                    ObjectCALegal(
                            activityReference,
                            linearLayoutForecast!!,
                            UtilityCanada.getLocationUrl(x, y)
                    )
                }
            }
        }
    }

    private fun getLocationHazards() = GlobalScope.launch(uiDispatcher) {
        if (locationChangedHazards) {
            linearLayoutHazards?.removeAllViewsInLayout()
            linearLayoutHazards?.visibility = View.GONE
            locationChangedHazards = false
        }
        withContext(Dispatchers.IO) {
            try {
                //objHazards = Utility.getCurrentHazards(Location.currentLocation)
                objHazards = if (Location.isUS(Location.currentLocation)) {
                    ObjectForecastPackageHazards(Location.currentLocation)
                } else {
                    val html = UtilityCanada.getLocationHtml(Location.getLatLon(Location.currentLocation))
                    ObjectForecastPackageHazards(html)
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            if (Location.isUS) {
                if (objHazards.titles.isEmpty()) {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.removeAllViews()
                        linearLayoutHazards?.visibility = View.GONE
                    }
                } else {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.visibility = View.VISIBLE
                        setupHazardCards()
                    }
                }
            } else {
                if (objHazards.getHazardsShort() != "") {
                    val hazardsSum = objHazards.getHazardsShort().toUpperCase(Locale.US)
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.visibility = View.VISIBLE
                        setupHazardCardsCA(hazardsSum)
                    }
                }
            }
        }
    }
}


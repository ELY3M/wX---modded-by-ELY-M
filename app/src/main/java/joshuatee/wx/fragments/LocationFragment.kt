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
//hail size texts
//sunrise card - not center it...  

package joshuatee.wx.fragments

import android.content.*
import java.util.Locale
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.util.*
import joshuatee.wx.Extensions.*
import joshuatee.wx.notifications.UtilityNotificationTools
import joshuatee.wx.objects.*
import joshuatee.wx.radar.*
import joshuatee.wx.settings.*
import joshuatee.wx.settings.UtilityHomeScreen
import joshuatee.wx.ui.*

class LocationFragment : Fragment() {

    //
    // Displays the main content when wX is first opened including current conditions
    // hazards, 7 days and radar (settings->homescreen can change this)
    //

    private lateinit var scrollView: ScrollView
    private lateinit var locationDialogue: ObjectDialogue
    private lateinit var locationLabel: CardText
    private var lastRefresh = 0.toLong()
    private var currentConditionsTime = ""
    private var radarTime = ""
    private var x = ""
    private var y = ""
    private var glviewInitialized = false
    private var sevenDayExtShown = false
    private var objectCardCurrentConditions: ObjectCardCurrentConditions? = null
    private lateinit var linearLayout: LinearLayout
    private var homescreenFavLocal = ""
    private val sevenDayCards = mutableListOf<SevenDayCard>()
    private val homeScreenTextCards = mutableListOf<CardHSText>()
    private val homeScreenImageCards = mutableListOf<CardHSImage>()
    private val homeScreenWebCards = mutableListOf<Card>()
    private val homeScreenWebViews = mutableListOf<WebView>()
    private val wxglRenders = mutableListOf<WXGLRender>()
    private val wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    private val wxglTextObjects = mutableListOf<WXGLTextObject>()
    private var numberOfRadars = 0
    private var oldRadarSites = Array(2) { "" }
    private val radarLocationChangedAl = mutableListOf<Boolean>()
    // used to track the wxogl # for the wxogl that is tied to current location
    private var oglrIdx = -1
    // total # of wxogl
    private var oglrCount = 0
    private var needForecastData = false
    private var boxForecast: VBox? = null
    private var boxHazards: VBox? = null
    private val hazardsCards = mutableListOf<CardText>()
    private val hazardsExpandedAl = mutableListOf<Boolean>()
    private var dataNotInitialized = true
    private var alertDialogStatus: ObjectDialogue? = null
    private val alertDialogStatusList = mutableListOf<String>()
    private var idxIntG = 0
    private var dialogRadarLongPress: ObjectDialogue? = null
    private val radarLongPressItems = mutableListOf<String>()
    private var objectHazards = ObjectHazards()
    private var objectSevenDay = ObjectSevenDay()
    private var locationChangedSevenDay = false
    private var locationChangedHazards = false
    private var paneList = listOf<Int>()
    private var objectCurrentConditions = ObjectCurrentConditions()

    private fun addDynamicCards() {
        var currentConditionsAdded = false
        var sevenDayAdded = false
        val cardViews = mutableListOf<CardView>()
        val homeScreenTokens = homescreenFavLocal.split(":").dropLastWhile { it.isEmpty() }
        numberOfRadars = homeScreenTokens.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        oldRadarSites = Array(numberOfRadars) { "" }
        val relativeLayouts = mutableListOf<RelativeLayout>()
        wxglSurfaceViews.clear()
        wxglTextObjects.clear()
        var index = 0
        homeScreenTokens.forEach { token ->
            val widthDivider = 1
            val numPanes = 1
            if (token == "TXT-CC" || token == "TXT-CC2") {
                if (!currentConditionsAdded && objectCardCurrentConditions != null) {
                    linearLayout.addView(objectCardCurrentConditions!!.get())
                    currentConditionsAdded = true
                }
            } else if (token == "TXT-HAZ") {
                boxHazards = VBox(activityReference)
                linearLayout.addView(boxHazards!!.get())
            } else if (token == "TXT-7DAY" || token == "TXT-7DAY2") {
                if (!sevenDayAdded) {
                    linearLayout.addView(boxForecast?.get())
                    sevenDayAdded = true
                }
            } else if (token == "OGL-RADAR") {
                wxglRenders.add(WXGLRender(MyApplication.appContext, 4))
                oglrIdx = oglrCount
                oglrCount += 1
                cardViews.add(Card(activityReference).get())
                wxglSurfaceViews.add(WXGLSurfaceView(MyApplication.appContext, widthDivider, numPanes, 1))
                wxglRenders[index].rid = ""
                oldRadarSites[index] = ""
                radarLocationChangedAl.add(false)
                wxglSurfaceViews[index].index = index
                relativeLayouts.add(RelativeLayout(activityReference))
                wxglTextObjects.add(WXGLTextObject(activityReference,
                        relativeLayouts[index],
                        wxglSurfaceViews[index],
                        wxglRenders[index],
                        numPanes, 4))
                wxglSurfaceViews[index].wxglTextObjects = wxglTextObjects
                wxglSurfaceViews[index].locationFragment = true
                wxglTextObjects[index].initializeLabels(activityReference)
                relativeLayouts[index].addView(wxglSurfaceViews[index])
                cardViews.last().addView(relativeLayouts[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt())
                linearLayout.addView(cardViews.last())
                index += 1
            } else if (token.contains("TXT-")) {
                val hsTextTmp = CardHSText(activityReference, token.replace("TXT-", ""))
                linearLayout.addView(hsTextTmp.get())
                homeScreenTextCards.add(hsTextTmp)
                hsTextTmp.connect { hsTextTmp.toggleText() }
            } else if (token.contains("IMG-")) {
                val hsImageTmp = CardHSImage(activityReference, token.replace("IMG-", ""))
                linearLayout.addView(hsImageTmp.get())
                homeScreenImageCards.add(hsImageTmp)
                setImageOnClick()
            } else if (token.contains("NXRD-")) {
                wxglRenders.add(WXGLRender(activityReference, 4))
                oglrCount += 1
                cardViews.add(Card(activityReference).get())
                wxglSurfaceViews.add(WXGLSurfaceView(activityReference, widthDivider, numPanes, 1))
                wxglSurfaceViews[index].index = index
                wxglRenders[index].rid = token.replace("NXRD-", "")
                oldRadarSites[index] = ""
                radarLocationChangedAl.add(false)
                relativeLayouts.add(RelativeLayout(activityReference))
                wxglTextObjects.add(WXGLTextObject(activityReference,
                        relativeLayouts[index],
                        wxglSurfaceViews[index],
                        wxglRenders[index],
                        numPanes, 4))
                wxglSurfaceViews[index].wxglTextObjects = wxglTextObjects
                wxglSurfaceViews[index].locationFragment = true
                wxglTextObjects[index].initializeLabels(activityReference)
                relativeLayouts[index].addView(wxglSurfaceViews[index])
                cardViews.last().addView(relativeLayouts[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()
                )
                linearLayout.addView(cardViews.last())
                index += 1
            } else if (token.contains("WEB-")) {
                if (token == "WEB-7DAY") {
                    val wv = WebView(activityReference)
                    homeScreenWebCards.add(Card(activityReference))
                    homeScreenWebViews.add(wv)
                    homeScreenWebCards.last().addWidget(homeScreenWebViews.last())
                    linearLayout.addView(homeScreenWebCards.last().get())
                }
            }
        } // end of loop over HM tokens
        paneList = (0 until wxglSurfaceViews.size).toList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setupAlertDialogStatus()
        setupAlertDialogRadarLongPress()
        val view: View =
                if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB)
                    inflater.inflate(R.layout.fragment_location_white, container, false)
                else
                    inflater.inflate(R.layout.fragment_location, container, false)
        homescreenFavLocal = UIPreferences.homescreenFav
        if (homescreenFavLocal.contains("TXT-CC") || homescreenFavLocal.contains("TXT-HAZ") || homescreenFavLocal.contains("TXT-7DAY")) {
            needForecastData = true
        }
        // The dialogue that opens when the user wants to change location
        locationDialogue = ObjectDialogue(activityReference, "Select location:", Location.listOf)
        locationDialogue.setSingleChoiceItems { dialog, locationIndex ->
            changeLocation(locationIndex)
            dialog.dismiss()
        }
        // The main LinearLayout that holds all content
        linearLayout = view.findViewById(R.id.linearLayout)
        // The button the user will tap to change the current location
        locationLabel = CardText(activityReference, linearLayout, Location.name, TextSize.MEDIUM)
        val locationLabelPadding = if (UtilityUI.isTablet()) {
            10
        } else {
            20
        }
        locationLabel.setPaddingAmount(locationLabelPadding)
        locationLabel.setTextColor(UIPreferences.textHighlightColor)
        locationLabel.connect { locationDialogue.show() }
        if (homescreenFavLocal.contains("TXT-CC2")) {
            objectCardCurrentConditions = ObjectCardCurrentConditions(activityReference, 2)
            objectCardCurrentConditions?.connect(alertDialogStatus, alertDialogStatusList, ::radarTimestamps)
        } else {
            objectCardCurrentConditions = ObjectCardCurrentConditions(activityReference, 1)
        }
        if (homescreenFavLocal.contains("TXT-7DAY")) {
            boxForecast = VBox(activityReference)
        }
        addDynamicCards()
        getContent()
        if (UIPreferences.locDisplayImg) {
            wxglSurfaceViews.indices.forEach {
                glviewInitialized = UtilityRadarUI.initGlviewFragment(
                        wxglSurfaceViews[it],
                        it,
                        wxglRenders,
                        wxglSurfaceViews,
                        wxglTextObjects,
                        changeListener)
            }
        }
        scrollView = view.findViewById(R.id.sv)
        return view
    }

    private fun changeLocation(position: Int) {
        locationChangedHazards = true
        locationChangedSevenDay = true
        if (position != Location.numLocations) {
            Utility.writePref(activityReference, "CURRENT_LOC_FRAGMENT", (position + 1).toString())
            Location.currentLocationStr = (position + 1).toString()
            x = Location.x
            y = Location.y
            if (oglrIdx != -1) {
                radarLocationChangedAl[oglrIdx] = false
            }
            if (UIPreferences.locDisplayImg && oglrIdx != -1) {
                wxglSurfaceViews[oglrIdx].scaleFactor = RadarPreferences.wxoglSize.toFloat() / 10.0f
                wxglRenders[oglrIdx].setViewInitial(RadarPreferences.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
            }
            homeScreenImageCards.forEach {
                it.resetZoom()
            }
            setImageOnClick()
            getContent()
        } else {
            Route.locationEdit(activityReference, arrayOf((position + 1).toString(), ""))
        }
        locationLabel.text = Location.name
    }

    fun getContent() {
        locationLabel.text = Location.name
        sevenDayExtShown = false
        if (needForecastData) {
            getForecastData()
        }
        homeScreenTextCards.indices.forEach {
//            getTextProduct(it)
            FutureText(MyApplication.appContext, homeScreenTextCards[it].product, homeScreenTextCards[it]::setup)
        }
        homeScreenImageCards.indices.forEach {
//            getImageProduct(it)
            FutureBytes2(MyApplication.appContext,
                    { UtilityDownload.getImageProduct(MyApplication.appContext, homeScreenImageCards[it].product) },
                    homeScreenImageCards[it]::set
            )
        }
        homeScreenWebViews.indices.forEach { _ ->
            getWebProduct()
        }
        x = Location.x
        y = Location.y
        if (UIPreferences.locDisplayImg) {
            getAllRadars()
        }
        val currentTime = UtilityTime.currentTimeMillis()
        lastRefresh = currentTime / 1000
        // TODO FIXME what is this for?
        Utility.writePrefLong(MyApplication.appContext, "LOC_LAST_UPDATE", lastRefresh)
    }

    override fun onResume() {
        super.onResume()
        if (glviewInitialized) {
            wxglSurfaceViews.forEach {
                it.onResume()
            }
        }
        objectCardCurrentConditions?.refreshTextSize()
        locationLabel.refreshTextSize(TextSize.MEDIUM)
        locationLabel.text = Location.name
        sevenDayCards.forEach {
            it.refreshTextSize()
        }
        homeScreenTextCards.forEach {
            it.refreshTextSize()
        }
        hazardsCards.forEach {
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
        }
        // TODO use a Timer class to handle the data refresh stuff
        val currentTime = UtilityTime.currentTimeMillis()
        val currentTimeSec = currentTime / 1000
        val refreshIntervalSec = (UIPreferences.refreshLocMin * 60).toLong()
        val xOld = x
        val yOld = y
        if (UIPreferences.locDisplayImg) {
            if (!glviewInitialized) {
                wxglSurfaceViews.indices.forEach {
                    glviewInitialized = UtilityRadarUI.initGlviewFragment(
                            wxglSurfaceViews[it],
                            it,
                            wxglRenders,
                            wxglSurfaceViews,
                            wxglTextObjects,
                            changeListener)
                }
            }
        }
        if (UIPreferences.refreshLocMin != 0 || dataNotInitialized) {
            if (currentTimeSec > lastRefresh + refreshIntervalSec || Location.x != xOld || Location.y != yOld) {
                getContent()
            }
            dataNotInitialized = false
        }
    }

    private fun getRadar(idx: Int) {
        var radarTimeStampLocal = ""
        if (oglrIdx != -1)
            if (!radarLocationChangedAl[oglrIdx]) {
                wxglRenders[oglrIdx].rid = Location.rid
            }
        if (wxglRenders[idx].product == "N0Q" && WXGLNexrad.isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "TZL"
        }
        if (wxglRenders[idx].product == "TZL" && !WXGLNexrad.isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "N0Q"
        }
        if (wxglRenders[idx].product == "N0U" && WXGLNexrad.isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "TV0"
        }
        if (wxglRenders[idx].product == "TV0" && !WXGLNexrad.isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "N0U"
        }
        UtilityRadarUI.initWxOglGeom(
                wxglSurfaceViews[idx],
                wxglRenders[idx],
                idx,
                oldRadarSites,
                wxglRenders,
                wxglTextObjects,
                paneList,
                null,
                wxglSurfaceViews,
                ::getGPSFromDouble,
                ::getLatLon)
        FutureVoid(MyApplication.appContext, {
            // attempted bugfix for most plentiful crash
            //kotlin.KotlinNullPointerException:
            //at joshuatee.wx.fragments.LocationFragment.getActivityReference (LocationFragment.kt:783)
            //at joshuatee.wx.fragments.LocationFragment.access$getActivityReference$p (LocationFragment.kt:65)
            //at joshuatee.wx.fragments.LocationFragment$getRadar$1$3.invokeSuspend (LocationFragment.kt:440)
            if (Location.isUS && mActivity != null ) {
                UtilityRadarUI.plotRadar(
                        wxglRenders[idx],
                        "",
                        MyApplication.appContext,
                        ::getGPSFromDouble,
                        ::getLatLon,
                        false)
            }
        }) {
            if (idx == oglrIdx) {
                radarTimeStampLocal = getRadarTimeStampForHomescreen(wxglRenders[oglrIdx].rid)
            }
            // NOTE: below was backed out, data structures for these features only support one radar site
            // so locfrag and multi-pane don't current support. Would be nice to fix someday.
            // Show extras a few lines above was changed from false to true along with few lines added below
            // some time ago there were crashes caused by this additional content but I don't recall the details
            // guess it's worth another try to see if the issue back then was fixed in the various re-writes that have
            // occurred since
	    
	    //elys mod
	    if (PolygonType.HAIL_LABELS.pref) { 
	    	UtilityWXGLTextObject.updateHailLabels(numberOfRadars, wxglTextObjects)
	    }
	    	    
            if (Location.isUS && idx == 0) {
                if (PolygonType.SPOTTER_LABELS.pref) {
                    UtilityWXGLTextObject.updateSpotterLabels(numberOfRadars, wxglTextObjects)
                }
            }
            if (PolygonType.OBS.pref) {
                UtilityWXGLTextObject.updateObservationsSinglePane(idx, wxglTextObjects)
            }
            wxglSurfaceViews[idx].requestRender()
            if (idx == oglrIdx) {
                radarTime = radarTimeStampLocal
                objectCardCurrentConditions?.setStatus(currentConditionsTime + radarTime)
            }
            if (RadarPreferences.wxoglCenterOnLocation) {
                wxglSurfaceViews[idx].resetView()
            }
        }
        // recent adds Jan 2020
        if (RadarPreferences.radarWarnings && activityReferenceWithNull != null) {
            FutureVoid(MyApplication.appContext, { UtilityDownloadWarnings.get(MyApplication.appContext) }) {
                if (!wxglRenders[idx].product.startsWith("2")) {
                    UtilityRadarUI.plotWarningPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
                }
            }
        }
        if (PolygonType.MCD.pref && activityReferenceWithNull != null) {
            FutureVoid(MyApplication.appContext, {
                ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]!!.get(MyApplication.appContext)
                if (activityReferenceWithNull != null) {
                    ObjectPolygonWatch.polygonDataByType[PolygonType.WATCH]!!.get(MyApplication.appContext)
                }
            }) {
                if (!wxglRenders[idx].product.startsWith("2")) {
                    UtilityRadarUI.plotMcdWatchPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
                }
            }
        }
        if (PolygonType.MPD.pref && activityReferenceWithNull != null) {
            FutureVoid(MyApplication.appContext, { ObjectPolygonWatch.polygonDataByType[PolygonType.MPD]!!.get(MyApplication.appContext) }) {
                if (!wxglRenders[idx].product.startsWith("2")) {
                    UtilityRadarUI.plotMpdPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
                }
            }
        }
        // end recent adds Jan 2020
        // don't enable until more stable
        /*if (MyApplication.radarShowWpcFronts) {
            withContext(Dispatchers.IO) {
                UtilityWpcFronts.get(activityReference)
            }
            if (!oglrArr[idx].product.startsWith("2")) {
                UtilityRadarUI.plotWpcFronts(glviewArr[idx], oglrArr[idx], false)
            }
            UtilityWXGLTextObject.updateWpcFronts(numPanesArr.size, wxgltextArr)
        }*/
    }

    private fun getWebProduct() {
        val forecastUrl = "https://forecast.weather.gov/MapClick.php?lat=" + Location.x + "&lon=" + Location.y + "&unit=0&lg=english&FcstType=text&TextType=2"
        homeScreenWebViews.last().loadUrl(forecastUrl)
    }

//    private fun getTextProduct(productIndex: Int) {
//        FutureText(MyApplication.appContext, homeScreenTextCards[productIndex].product, homeScreenTextCards[productIndex]::setup)
//    }

//    private fun getImageProduct(productIndex: Int) {
//        FutureBytes2(MyApplication.appContext,
//                { UtilityDownload.getImageProduct(MyApplication.appContext, homeScreenImageCards[productIndex].product) },
//                homeScreenImageCards[productIndex]::set
//        )
//    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                idxIntG = idx
                UtilityRadarUI.addItemsToLongPress(radarLongPressItems, Location.x, Location.y, activityReference, wxglSurfaceViews[idx], wxglRenders[idx], dialogRadarLongPress!!)
            } else {
                (0 until numberOfRadars).forEach {
                    wxglTextObjects[it].addLabels()
                }
            }
        }
    }

    private fun getRadarTimeStampForHomescreen(radarSite: String): String {
        var timestamp = ""
        val tokens = WXGLNexrad.getRadarInfo(radarSite).split(" ")
        if (tokens.size > 3) {
            timestamp = tokens[3]
        }
        return if (oglrIdx != -1) {
            " " + wxglRenders[idxIntG].rid + ": " + timestamp
        } else {
            ""
        }
    }

    private fun getRadarTimeStamp(string: String, j: Int): String {
        var timestamp = ""
        val tokens = string.split(" ")
        if (tokens.size > 3) {
            timestamp = tokens[3]
        }
        return wxglRenders[j].rid + ": " + timestamp + " (" + Utility.getRadarSiteName(wxglRenders[j].rid) + ")"
    }

    private fun getGPSFromDouble() {}

    private fun getLatLon() = LatLon(Location.x, Location.y)

    override fun onPause() {
        if (glviewInitialized) {
            wxglSurfaceViews.forEach {
                it.onPause()
            }
        }
        super.onPause()
    }

    private fun setImageOnClick() {
        homeScreenImageCards.indices.forEach { ii ->
            val cl = UtilityHomeScreen.HM_CLASS[homeScreenImageCards[ii].product]
            val id = UtilityHomeScreen.HM_CLASS_ID[homeScreenImageCards[ii].product]
            val argsOrig = UtilityHomeScreen.HM_CLASS_ARGS[homeScreenImageCards[ii].product]
            homeScreenImageCards[ii].connect {
                if (argsOrig != null) {
//                    val args = arrayOfNulls<String>(argsOrig.size)
//                    val args = Array(argsOrig.size) {""}
                    val args = argsOrig.copyOf(argsOrig.size)
//                    System.arraycopy(argsOrig, 0, args, 0, argsOrig.size)
                    args.indices.forEach { z ->
                        if (args[z] == "WFO_FOR_SND")
                            args[z] = UtilityLocation.getNearestSoundingSite(LatLon(Location.x, Location.y))
                        if (args[z] == "WFO_FOR_GOES")
                            args[z] = Location.wfo.lowercase(Locale.US)
                        if (args[z] == "STATE_LOWER")
                            args[z] = Location.state.lowercase(Locale.US)
                        if (args[z] == "STATE_UPPER")
                            args[z] = Location.state
                        if (args[z] == "RID_FOR_CA")
                            args[z] = Location.rid
                    }
                    if (cl != null && id != null) {
//                        Route(MyApplication.appContext, cl, id, args)
                        val intent = Intent(MyApplication.appContext, cl)
                        intent.putExtra(id, args)
                        startActivity(intent)
                    }
                } else {
                    Route.vis(activityReference)
                }
            }
        }
    }

    private fun getAllRadars() {
        wxglSurfaceViews.indices.forEach {
            getRadar(it)
//            if (!(PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)) {
//                getRadar(it)
//            } else {
//                getRadar(it)
//            }
        }
    }

    private fun resetAllGlview() {
        wxglSurfaceViews.indices.forEach {
            UtilityRadarUI.resetGlview(wxglSurfaceViews[it], wxglRenders[it])
            wxglTextObjects[it].addLabels()
        }
    }

    private fun radarTimestamps(): List<String> {
        return (0 until wxglSurfaceViews.size).map { getRadarTimeStamp(wxglRenders[it].wxglNexradLevel3.timestamp, it) }
    }

    private fun setupHazardCardsCA(hazUrl: String) {
        boxHazards?.removeChildrenAndLayout()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        hazardsExpandedAl.add(false)
        hazardsCards.add(CardText(activityReference))
        hazardsCards[0].setupHazard()
        hazardsCards[0].text = hazUrl
        val hazUrlCa = objectHazards.hazards
        hazardsCards[0].connect { Route.text(activityReference, arrayOf(Utility.fromHtml(hazUrlCa), hazUrl)) }
        if (!hazUrl.startsWith("NO WATCHES OR WARNINGS IN EFFECT")) {
            boxHazards?.addWidget(hazardsCards[0].get())
        }
    }

    private fun setupAlertDialogStatus() {
        alertDialogStatus = ObjectDialogue(activityReference, alertDialogStatusList)
        alertDialogStatus!!.setNegativeButton { dialog, _ -> dialog.dismiss() }
        alertDialogStatus!!.setSingleChoiceItems { dialog, which ->
            val strName = alertDialogStatusList[which]
            if (wxglRenders.size > 0) {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        wxglRenders[0],
                        activityReference,
                        ::getContent,
                        ::resetAllGlview,
                        ::getAllRadars)
            } else {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        null,
                        activityReference,
                        ::getContent,
                        ::resetAllGlview,
                        ::getAllRadars)
            }
            dialog.dismiss()
        }
    }

    private fun setupAlertDialogRadarLongPress() {
        dialogRadarLongPress = ObjectDialogue(activityReference, radarLongPressItems)
        dialogRadarLongPress?.setNegativeButton { dialog, _ ->
            dialog.dismiss()
        }
        dialogRadarLongPress?.setSingleChoiceItems { dialog, which ->
            val item = radarLongPressItems[which]
            UtilityRadarUI.doLongPressAction(item, activityReference, wxglSurfaceViews[idxIntG], wxglRenders[idxIntG], ::longPressRadarSiteSwitch)
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        val ridNew = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
        val oldRidIdx = wxglRenders[idxIntG].rid
        wxglRenders[idxIntG].rid = ridNew
        if (idxIntG != oglrIdx) {
            UIPreferences.homescreenFav = UIPreferences.homescreenFav.replace("NXRD-$oldRidIdx", "NXRD-" + wxglRenders[idxIntG].rid)
            Utility.writePref(activityReference, "HOMESCREEN_FAV", UIPreferences.homescreenFav)
        }
        radarLocationChangedAl[idxIntG] = true
        wxglSurfaceViews[idxIntG].scaleFactor = RadarPreferences.wxoglSize.toFloat() / 10.0f
        wxglRenders[idxIntG].setViewInitial(RadarPreferences.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
        getRadar(idxIntG)
    }

    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context) { // was Context? before 'androidx.preference:preference:1.1.0' // was 1.0.0
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    private fun setupHazardCards() {
        boxHazards?.removeChildrenAndLayout()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        objectHazards.titles.indices.forEach { z ->
            if (UtilityNotificationTools.nwsLocalAlertNotFiltered(activityReference, objectHazards.titles[z])) {
                hazardsExpandedAl.add(false)
                hazardsCards.add(CardText(activityReference))
                hazardsCards[z].setupHazard()
                hazardsCards[z].text = objectHazards.titles[z].uppercase(Locale.US)
                hazardsCards[z].connect { Route.hazard(activityReference, arrayOf(objectHazards.urls[z])) }
                boxHazards?.addWidget(hazardsCards[z].get())
            } else {
                hazardsExpandedAl.add(false)
                hazardsCards.add(CardText(activityReference))
            }
        }
    }

    private fun getForecastData() {
        FutureVoid(MyApplication.appContext, ::getCc, ::updateCc)
        if (locationChangedSevenDay) {
            boxForecast?.removeChildren()
            locationChangedSevenDay = false
        }
        FutureVoid(MyApplication.appContext, ::get7day, ::update7day)
        if (locationChangedHazards) {
            boxHazards?.removeChildren()
            boxHazards?.visibility = View.GONE
            locationChangedHazards = false
        }
        FutureVoid(MyApplication.appContext, ::getHazards, ::updateHazards)
    }

    private fun getCc() {
        try {
            objectCurrentConditions = ObjectCurrentConditions(MyApplication.appContext, Location.currentLocation)
            objectCurrentConditions.timeCheck(MyApplication.appContext)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun updateCc() {
        if (isAdded) {
            objectCardCurrentConditions?.let {
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    currentConditionsTime = objectCurrentConditions.status
                    it.update(objectCurrentConditions, Location.isUS, radarTime)
                } else {
                    it.setTopLine(objectCurrentConditions.data)
                    currentConditionsTime = objectCurrentConditions.status
                    it.setStatus(currentConditionsTime + radarTime)
                }
            }
        }
    }

    private fun get7day() {
        try {
            objectSevenDay = ObjectSevenDay(Location.currentLocation)
            Utility.writePref(MyApplication.appContext, "FCST", objectSevenDay.sevenDayLong)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        Utility.writePref(MyApplication.appContext, "FCST", objectSevenDay.sevenDayLong)
    }

    private fun update7day() {
        if (isAdded) {
            if (homescreenFavLocal.contains("TXT-7DAY")) {
                boxForecast?.removeChildren()
                sevenDayCards.clear()
                objectSevenDay.icons.forEachIndexed { index, iconUrl ->
                    val objectCard7Day = SevenDayCard(activityReference, iconUrl, Location.isUS, index, objectSevenDay.forecastList)
                    objectCard7Day.connect { scrollView.smoothScrollTo(0, 0) }
                    boxForecast?.addWidget(objectCard7Day.get())
                    sevenDayCards.add(objectCard7Day)
                }
                val cardSunrise = CardText(activityReference)
		//elys mod - for full sun and moon times//
                //cardSunrise.center()
                cardSunrise.connect { scrollView.smoothScrollTo(0, 0) }
                try {
                    cardSunrise.text = UtilityTimeSunMoon.getForHomeScreen(activityReference)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
                boxForecast?.addWidget(cardSunrise.get())
            }
            if (!Location.isUS && homescreenFavLocal.contains("TXT-7DAY2")) {
                ObjectCALegal(activityReference, boxForecast!!.get(), UtilityCanada.getLocationUrl(Location.x, Location.y))
            }
        }
    }

    private fun getHazards() {
        try {
            objectHazards = if (Location.isUS(Location.currentLocation)) {
                ObjectHazards(Location.currentLocation)
            } else {
                val html = UtilityCanada.getLocationHtml(Location.getLatLon(Location.currentLocation))
                ObjectHazards(html)
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun updateHazards() {
        if (isAdded) {
            if (Location.isUS) {
                if (objectHazards.titles.isEmpty()) {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        boxHazards?.removeChildrenAndLayout()
                        boxHazards?.visibility = View.GONE
                    }
                } else {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        boxHazards?.visibility = View.VISIBLE
                        setupHazardCards()
                    }
                }
            } else {
                if (objectHazards.getHazardsShort() != "") {
                    val hazardsSum = objectHazards.getHazardsShort().uppercase(Locale.US)
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        boxHazards?.visibility = View.VISIBLE
                        setupHazardCardsCA(hazardsSum)
                    }
                }
            }
        }
    }

    fun showLocations() {
        locationDialogue.show()
    }

    // FIXME change to return context and use getContext in API greater then 22
    // FIXME duplicate for 2 other areas
    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                mActivity = if (android.os.Build.VERSION.SDK_INT >= 23 ) {
                    activity
                } else {
                    activity
                }
            }
            return mActivity!!
        }

    private val activityReferenceWithNull: FragmentActivity?
        get() {
            if (mActivity == null) {
                mActivity = if (android.os.Build.VERSION.SDK_INT >= 23 ) {
                    activity
                } else {
                    activity
                }
            }
            return mActivity
        }
}

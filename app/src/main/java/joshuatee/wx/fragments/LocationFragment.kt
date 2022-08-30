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
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.util.*
import joshuatee.wx.notifications.UtilityNotificationTools
import joshuatee.wx.objects.*
import joshuatee.wx.radar.*
import joshuatee.wx.settings.*
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
    private lateinit var box: VBox
    private var homescreenFavLocal = ""
    private val sevenDayCards = mutableListOf<SevenDayCard>()
    private val homeScreenTextCards = mutableListOf<CardHSText>()
    private val homeScreenImageCards = mutableListOf<CardHSImage>()
    private val homeScreenWebCards = mutableListOf<Card>()
    private val homeScreenWebViews = mutableListOf<WebView>()
    private val radarLocationChangedAl = mutableListOf<Boolean>()
    // used to track the wxogl # for the wxogl that is tied to current location
    private var oglrIdx = -1
    // total # of wxogl
    private var oglrCount = 0
    private var needForecastData = false
    private var boxForecast: VBox? = null
    private var boxHazards: VBox? = null
    private val hazardsCards = mutableListOf<CardText>()
    private val hazardsExpandedList = mutableListOf<Boolean>()
    private var dataNotInitialized = true
    private var alertDialogStatus: ObjectDialogue? = null
    private val alertDialogStatusList = mutableListOf<String>()
    private var objectHazards = ObjectHazards()
    private var objectSevenDay = ObjectSevenDay()
    private var locationChangedSevenDay = false
    private var locationChangedHazards = false
    private var objectCurrentConditions = ObjectCurrentConditions()
    private lateinit var nexradState: NexradStateMainScreen
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradArguments: NexradArguments

    private fun addDynamicCards() {
        var currentConditionsAdded = false
        var sevenDayAdded = false
        val cards = mutableListOf<Card>()
        val homeScreenTokens = homescreenFavLocal.split(":").dropLastWhile { it.isEmpty() }
        val numberOfRadars = homeScreenTokens.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        nexradArguments = NexradArguments()
        nexradArguments.locXCurrent = Location.latLon.lat
        nexradArguments.locYCurrent = Location.latLon.lon
        nexradState = NexradStateMainScreen(MyApplication.appContext, numberOfRadars, homeScreenTokens)
        nexradLongPressMenu = NexradLongPressMenu(activityReference, nexradState, nexradArguments, ::longPressRadarSiteSwitch)
        var index = 0
        homeScreenTokens.forEach { token ->
            if (token == "TXT-CC" || token == "TXT-CC2") {
                if (!currentConditionsAdded && objectCardCurrentConditions != null) {
                    box.addWidget(objectCardCurrentConditions!!.get())
                    currentConditionsAdded = true
                }
            } else if (token == "TXT-HAZ") {
                boxHazards = VBox(activityReference)
                box.addWidget(boxHazards!!.get())
            } else if (token == "TXT-7DAY" || token == "TXT-7DAY2") {
                if (!sevenDayAdded) {
                    box.addLayout(boxForecast!!.get())
                    sevenDayAdded = true
                }
            } else if (token == "OGL-RADAR" || token.contains("NXRD-")) {
                if (token == "OGL-RADAR") {
                    oglrIdx = oglrCount
                }
                oglrCount += 1
                cards.add(Card(activityReference))
                radarLocationChangedAl.add(false)
                cards.last().addWidget(nexradState.relativeLayouts[index])
                cards.last().get().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt())
                box.addWidget(cards.last().get())
                index += 1
            } else if (token.contains("TXT-")) {
                val hsText = CardHSText(activityReference, token.replace("TXT-", ""))
                box.addWidget(hsText.get())
                homeScreenTextCards.add(hsText)
                hsText.connect { hsText.toggleText() }
            } else if (token.contains("IMG-")) {
                val hsImage = CardHSImage(activityReference, token.replace("IMG-", ""))
                box.addWidget(hsImage.get())
                homeScreenImageCards.add(hsImage)
                setImageOnClick()
            } else if (token.contains("WEB-")) {
                if (token == "WEB-7DAY") {
                    val webView = WebView(activityReference)
                    homeScreenWebCards.add(Card(activityReference))
                    homeScreenWebViews.add(webView)
                    homeScreenWebCards.last().addWidget(homeScreenWebViews.last())
                    box.addWidget(homeScreenWebCards.last().get())
                }
            }
        } // end of loop over HM tokens
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setupAlertDialogStatus()
        val view = inflater.inflate(R.layout.fragment_location, container, false)
        homescreenFavLocal = UIPreferences.homescreenFav
        if (homescreenFavLocal.contains("TXT-CC") || homescreenFavLocal.contains("TXT-HAZ") || homescreenFavLocal.contains("TXT-7DAY")) {
            needForecastData = true
        }
        // The dialogue that opens when the user wants to change location
        locationDialogue = ObjectDialogue(activityReference, "Select location:", Location.listOf)
        locationDialogue.connect { dialog, locationIndex ->
            changeLocation(locationIndex)
            dialog.dismiss()
        }
        // The main LinearLayout that holds all content
        box = VBox.fromViewResource(view)
        // The button the user will tap to change the current location
        locationLabel = CardText(activityReference, box.get(), Location.name, TextSize.MEDIUM)
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
            nexradState.wxglSurfaceViews.indices.forEach {
                nexradState.wxglSurfaceViews[it].index = it
                glviewInitialized = NexradDraw.initGlviewMainScreen(it, nexradState, nexradLongPressMenu.changeListener)
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
                nexradState.wxglSurfaceViews[oglrIdx].scaleFactor = RadarPreferences.wxoglSize / 10.0f
                nexradState.wxglRenders[oglrIdx].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
            }
            homeScreenImageCards.forEach {
                it.resetZoom()
            }
            setImageOnClick()
            getContent()
        } else {
            Route.locationEdit(activityReference, (position + 1).toString())
        }
        locationLabel.text = Location.name
    }

    fun getContent() {
        locationLabel.text = Location.name
        sevenDayExtShown = false
        if (needForecastData) {
            getForecastData()
        }
        homeScreenTextCards.forEach {
            FutureText(MyApplication.appContext, it.product, it::setup)
        }
        homeScreenImageCards.forEach {
            FutureBytes2(MyApplication.appContext,
                    { UtilityDownload.getImageProduct(MyApplication.appContext, it.product) },
                    it::set)
        }
        repeat(homeScreenWebViews.size) {
            getWebProduct()
        }
        x = Location.x
        y = Location.y
        if (UIPreferences.locDisplayImg) {
            getAllRadars()
        }
        val currentTime = ObjectDateTime.currentTimeMillis()
        lastRefresh = currentTime / 1000
        // TODO FIXME what is this for?
        Utility.writePrefLong(MyApplication.appContext, "LOC_LAST_UPDATE", lastRefresh)
    }

    override fun onResume() {
        super.onResume()
        if (glviewInitialized) {
            nexradState.onResume()
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
        val currentTime = ObjectDateTime.currentTimeMillis()
        val currentTimeSec = currentTime / 1000
        val refreshIntervalSec = (UIPreferences.refreshLocMin * 60).toLong()
        val xOld = x
        val yOld = y
        if (UIPreferences.locDisplayImg) {
            if (!glviewInitialized) {
                nexradState.wxglSurfaceViews.indices.forEach {
                    glviewInitialized = NexradDraw.initGlviewMainScreen(it, nexradState, nexradLongPressMenu.changeListener)
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
                nexradState.wxglRenders[oglrIdx].rid = Location.rid
            }
        nexradState.adjustForTdwr(idx)
        NexradDraw.initGeom(
                idx,
                nexradState.oldRadarSites,
                nexradState.wxglRenders,
                nexradState.wxglTextObjects,
                null,
                nexradState.wxglSurfaceViews,
                ::getGPSFromDouble,
                ::getLatLon,
                archived = false, forceReset = false)
        FutureVoid(MyApplication.appContext, {
            // attempted bugfix for most plentiful crash
            //kotlin.KotlinNullPointerException:
            //at joshuatee.wx.fragments.LocationFragment.getActivityReference (LocationFragment.kt:783)
            //at joshuatee.wx.fragments.LocationFragment.access$getActivityReference$p (LocationFragment.kt:65)
            //at joshuatee.wx.fragments.LocationFragment$getRadar$1$3.invokeSuspend (LocationFragment.kt:440)
            if (Location.isUS && mActivity != null) {
                NexradDraw.plotRadar(
                        nexradState.wxglRenders[idx],
                        "",
                        ::getGPSFromDouble,
                        ::getLatLon,
                        false)
            }
        }) {
            if (idx == oglrIdx) {
                radarTimeStampLocal = getRadarTimeStampForHomescreen(nexradState.wxglRenders[oglrIdx].rid)
            }
            // NOTE: below was backed out, data structures for these features only support one radar site
            // so locfrag and multi-pane don't current support. Would be nice to fix someday.
            // Show extras a few lines above was changed from false to true along with few lines added below
            // some time ago there were crashes caused by this additional content but I don't recall the details
            // guess it's worth another try to see if the issue back then was fixed in the various re-writes that have
            // occurred since
	    
	        //elys mod - not removing those!!!  unless crashing....
	        if (PolygonType.HAIL_LABELS.pref) {
	    	    UtilityWXGLTextObject.updateHailLabels(idx, nexradState.wxglTextObjects) //was numberOfRadars
            }

            if (Location.isUS && idx == 0) {
                if (PolygonType.SPOTTER_LABELS.pref) {
                    UtilityWXGLTextObject.updateSpotterLabels(idx, nexradState.wxglTextObjects) //was numberOfRadars
                }
            }
            if (PolygonType.OBS.pref) {
                UtilityWXGLTextObject.updateObservationsSinglePane(idx, nexradState.wxglTextObjects)
            }
            ////////

            nexradState.wxglSurfaceViews[idx].requestRender()
            if (idx == oglrIdx) {
                radarTime = radarTimeStampLocal
                objectCardCurrentConditions?.setStatus(currentConditionsTime + radarTime)
            }
            if (RadarPreferences.wxoglCenterOnLocation) {
                nexradState.wxglSurfaceViews[idx].resetView()
            }
        }
        NexradLayerDownload.download(
                MyApplication.appContext,
                1,
                nexradState.wxglRenders[idx],
                nexradState.wxglSurfaceViews[idx],
                nexradState.wxglTextObjects,
                {},
                false)
    }

    private fun getWebProduct() {
        val forecastUrl = "https://forecast.weather.gov/MapClick.php?lat=" + Location.x + "&lon=" + Location.y + "&unit=0&lg=english&FcstType=text&TextType=2"
        homeScreenWebViews.last().loadUrl(forecastUrl)
    }

    private fun getRadarTimeStampForHomescreen(radarSite: String): String {
        var timestamp = ""
        val tokens = WXGLNexrad.getRadarInfo(radarSite).split(" ")
        if (tokens.size > 3) {
            timestamp = tokens[3]
        }
        return if (oglrIdx != -1) {
            " " + nexradState.radarSite + ": " + timestamp
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
        return nexradState.wxglRenders[j].rid + ": " + timestamp + " (" + Utility.getRadarSiteName(nexradState.wxglRenders[j].rid) + ")"
    }

    private fun getGPSFromDouble() {}

    // main screen will not show GPS so if configured just show it off the screen
    // NOTE - this was backed out as it's not a good solution when user enables "center radar on location"
//    private fun getLatLon() = LatLon(0.0, 0.0)

    private fun getLatLon() = LatLon(Location.x, Location.y)

    override fun onPause() {
        if (glviewInitialized) {
            nexradState.onPause()
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
                    val args = argsOrig.copyOf(argsOrig.size)
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
        nexradState.wxglSurfaceViews.indices.forEach {
            getRadar(it)
        }
    }

    private fun radarTimestamps(): List<String> {
        return (0 until nexradState.wxglSurfaceViews.size).map { getRadarTimeStamp(nexradState.wxglRenders[it].wxglNexradLevel3.timestamp, it) }
    }

    private fun setupHazardCardsCA(hazUrl: String) {
        boxHazards?.removeChildrenAndLayout()
        hazardsExpandedList.clear()
        hazardsCards.clear()
        hazardsExpandedList.add(false)
        hazardsCards.add(CardText(activityReference))
        hazardsCards[0].setupHazard()
        hazardsCards[0].text = hazUrl
        val hazUrlCa = objectHazards.hazards
        hazardsCards[0].connect { Route.text(activityReference, Utility.fromHtml(hazUrlCa), hazUrl) }
        if (!hazUrl.startsWith("NO WATCHES OR WARNINGS IN EFFECT")) {
            boxHazards?.addWidget(hazardsCards[0].get())
        }
    }

    private fun setupAlertDialogStatus() {
        alertDialogStatus = ObjectDialogue(activityReference, alertDialogStatusList)
        alertDialogStatus!!.connect { dialog, which ->
            val strName = alertDialogStatusList[which]
            val renderOrNull = if (nexradState.wxglRenders.size > 0) {
                nexradState.wxglRenders[0]
            } else {
                null
            }
            UtilityLocationFragment.handleIconTap(
                    strName,
                    renderOrNull,
                    activityReference,
                    ::getContent,
                    nexradState::resetAllGlview,
                    ::getAllRadars)
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(s: String) {
        val newRadarSite = s.split(" ")[0]
        val oldRidIdx = nexradState.radarSite
        nexradState.adjustPaneTo(nexradState.curRadar, newRadarSite)
        if (nexradState.curRadar != oglrIdx) {
            UIPreferences.homescreenFav = UIPreferences.homescreenFav.replace("NXRD-$oldRidIdx", "NXRD-" + nexradState.radarSite)
            Utility.writePref(activityReference, "HOMESCREEN_FAV", UIPreferences.homescreenFav)
        }
        radarLocationChangedAl[nexradState.curRadar] = true
        getRadar(nexradState.curRadar)
    }

    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context) {
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
        hazardsExpandedList.clear()
        hazardsCards.clear()
        objectHazards.titles.indices.forEach { z ->
            if (UtilityNotificationTools.nwsLocalAlertNotFiltered(activityReference, objectHazards.titles[z])) {
                hazardsExpandedList.add(false)
                hazardsCards.add(CardText(activityReference))
                hazardsCards[z].setupHazard()
                hazardsCards[z].text = objectHazards.titles[z].uppercase(Locale.US)
                hazardsCards[z].connect { Route.hazard(activityReference, objectHazards.urls[z]) }
                boxHazards?.addWidget(hazardsCards[z].get())
            } else {
                hazardsExpandedList.add(false)
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
                    val objectCard7Day = SevenDayCard(activityReference, iconUrl, Location.isUS, objectSevenDay.forecastList[index])
                    objectCard7Day.connect { scrollView.smoothScrollTo(0, 0) }
                    boxForecast?.addWidget(objectCard7Day.get())
                    sevenDayCards.add(objectCard7Day)
                }
                if (Location.isUS) {
                    val sunRiseCard = SunRiseCard(activityReference, Location.latLon, scrollView)
                    boxForecast?.addWidget(sunRiseCard.get())
                }
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

    // used in WX.kt keyboard shortcut
    fun showLocations() {
        locationDialogue.show()
    }

    // FIXME duplicate for 2 other areas
    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                mActivity = activity
            }
            return mActivity!!
        }
}

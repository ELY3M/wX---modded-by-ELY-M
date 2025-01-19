/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import android.content.Context
import java.util.Locale
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import joshuatee.wx.MyApplication
import joshuatee.wx.R
//elys mod - leave this alone
import joshuatee.wx.objects.*
import joshuatee.wx.radar.*
import joshuatee.wx.radar.NexradDraw
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.DownloadImage
import joshuatee.wx.util.Hazards
import joshuatee.wx.util.SevenDay
import joshuatee.wx.util.Utility
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityHomeScreen
import joshuatee.wx.ui.CanadaLegal
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.CardCurrentConditions
import joshuatee.wx.ui.CardHazards
import joshuatee.wx.ui.CardHazardsCA
import joshuatee.wx.ui.CardHSImage
import joshuatee.wx.ui.CardHSText
import joshuatee.wx.ui.CardSevenDay
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.SevenDayCollection
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityLocationFragment

class LocationFragment : Fragment() {

    //
    // Displays the main content when wX is first opened including current conditions
    // hazards, 7 days and radar (settings->homescreen can change this)
    //

    private lateinit var scrollView: ScrollView
    private lateinit var locationDialogue: ObjectDialogue
    private lateinit var locationLabel: CardText
    private var downloadTimer = DownloadTimer("HOMESCREEN")
    private var currentConditionsTime = ""
    private var radarTime = ""
    private var glviewInitialized = false
    private var cardCurrentConditions: CardCurrentConditions? = null
    private lateinit var box: VBox
    private val sevenDayCards = mutableListOf<CardSevenDay>()
    private val textCards = mutableListOf<CardHSText>()
    private val imageCards = mutableListOf<CardHSImage>()
    private val radarLocationChangedList = mutableListOf<Boolean>()

    // used to track the wxogl # for the wxogl that is tied to current location
    private var radarForLocationIndex = -1
    private var needForecastData = false
    private var boxForecast: VBox? = null
    private var sevenDayCollection: SevenDayCollection? = null
    private var boxHazards: VBox? = null
    private val hazardsCards = mutableListOf<CardText>()
    private var dataNotInitialized = true
    private var locationStatusDialogue: ObjectDialogue? = null
    private val locationStatusDialogueList = mutableListOf<String>()
    private var hazards = Hazards()
    private var sevenDay = SevenDay()
    private var locationChanged = false
    private var currentConditions = CurrentConditions()
    private lateinit var nexradState: NexradStateMainScreen
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradArguments: NexradArguments

    private fun addDynamicCards() {
        var currentConditionsAdded = false
        var sevenDayAdded = false
        val cards = mutableListOf<Card>()
        val homeScreenTokens = UIPreferences.homescreenFav.split(":").dropLastWhile { it.isEmpty() }
        initNexrad(homeScreenTokens)
        homeScreenTokens.forEach { token ->
            when {
                token == "TXT-CC" || token == "TXT-CC2" -> {
                    if (!currentConditionsAdded && cardCurrentConditions != null) {
                        box.addWidget(cardCurrentConditions!!)
                        currentConditionsAdded = true
                    }
                }

                token == "TXT-HAZ" -> {
                    boxHazards = VBox(activityReference)
                    box.addWidget(boxHazards!!.get())
                }

                token == "TXT-7DAY" || token == "TXT-7DAY2" -> {
                    if (!sevenDayAdded) {
                        box.addLayout(boxForecast!!)
                        sevenDayAdded = true
                    }
                }

                token == "OGL-RADAR" || token.contains("NXRD-") -> {
                    if (token == "OGL-RADAR") {
                        radarForLocationIndex = radarLocationChangedList.size
                    }
                    cards.add(Card(activityReference))
                    cards.last()
                        .addWidget(nexradState.relativeLayouts[radarLocationChangedList.size])
                    cards.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()
                    )
                    box.addWidget(cards.last())
                    radarLocationChangedList.add(false)
                }

                token.contains("TXT-") -> {
                    val card = CardHSText(activityReference, token.replace("TXT-", ""))
                    box.addWidget(card)
                    textCards.add(card)
                    card.connect { card.toggleText() }
                }

                token.contains("IMG-") -> {
                    val card = CardHSImage(activityReference, token.replace("IMG-", ""))
                    box.addWidget(card)
                    imageCards.add(card)
                }
            }
        }
        setImageOnClick()
    }

    private fun initNexrad(homeScreenTokens: List<String>) {
        val numberOfRadars = homeScreenTokens.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        nexradArguments = NexradArguments()
        nexradArguments.locXCurrent = Location.latLon.lat
        nexradArguments.locYCurrent = Location.latLon.lon
        nexradState =
            NexradStateMainScreen(MyApplication.appContext, numberOfRadars, homeScreenTokens)
        nexradLongPressMenu = NexradLongPressMenu(
            activityReference,
            nexradState,
            nexradArguments,
            ::longPressRadarSiteSwitch
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_location, container, false)
        if (UIPreferences.homescreenFav.contains("TXT-CC") ||
            UIPreferences.homescreenFav.contains("TXT-HAZ") ||
            UIPreferences.homescreenFav.contains("TXT-7DAY")
        ) {
            needForecastData = true
        }
        box = VBox.fromViewResource(view)
        scrollView = view.findViewById(R.id.sv)
        setupLocationLabel()
        setupLocationStatusDialogue()
        setupForecastUI()
        addDynamicCards()
        if (UIPreferences.isNexradOnMainScreen) {
            nexradState.wxglSurfaceViews.indices.forEach {
                nexradState.wxglSurfaceViews[it].index = it
                NexradDraw.initGlviewMainScreen(it, nexradState, nexradLongPressMenu.changeListener)
                glviewInitialized = true
            }
        }
        return view
    }

    private fun setupLocationLabel() {
        // The dialogue that opens when the user wants to change location
        locationDialogue = ObjectDialogue(activityReference, "Select location:", Location.listOf)
        locationDialogue.connect { dialog, index ->
            changeLocation(index)
            dialog.dismiss()
        }
        // The button the user will tap to change the current location
        locationLabel = CardText(activityReference, Location.name, TextSize.MEDIUM)
        box.addWidget(locationLabel)
        val locationLabelPadding = if (UtilityUI.isTablet()) {
            10
        } else {
            20
        }
        with(locationLabel) {
            setPaddingAmount(locationLabelPadding)
            setTextColor(UIPreferences.textHighlightColor)
            typefaceBold()
            connect { locationDialogue.show() }
        }
    }

    private fun setupForecastUI() {
        if (UIPreferences.homescreenFav.contains("TXT-CC2")) {
            cardCurrentConditions = CardCurrentConditions(activityReference, 2)
            cardCurrentConditions!!.connect(
                locationStatusDialogue,
                locationStatusDialogueList,
                ::radarTimestamps
            )
        } else {
            cardCurrentConditions = CardCurrentConditions(activityReference, 1)
        }
        if (UIPreferences.homescreenFav.contains("TXT-7DAY")) {
            boxForecast = VBox(activityReference)
            sevenDayCollection = SevenDayCollection(activityReference, boxForecast!!, scrollView)
        }
    }

    private fun changeLocation(position: Int) {
        locationChanged = true
        // If user did not choose the last option "Add Location..."
        if (position != Location.numLocations) {
            Location.setCurrentLocationStr(activityReference, (position + 1).toString())
            if (UIPreferences.isNexradOnMainScreen && radarForLocationIndex != -1) {
                radarLocationChangedList[radarForLocationIndex] = false
                nexradState.wxglSurfaceViews[radarForLocationIndex].scaleFactor =
                    RadarPreferences.wxoglSize / 10.0f
                nexradState.wxglRenders[radarForLocationIndex].setViewInitial(
                    RadarPreferences.wxoglSize / 10.0f,
                    0.0f,
                    0.0f
                )
            }
            imageCards.forEach {
                it.resetZoom()
            }
            setImageOnClick()
            downloadTimer.resetTimer()
            getContent()
        } else {
            Route.locationEdit(activityReference, (position + 1).toString())
        }
        locationLabel.text = Location.name
    }

    fun getContent() {
        locationLabel.text = Location.name
        if (needForecastData) {
            getForecastData()
        }
        textCards.forEach {
            FutureText(MyApplication.appContext, it.product, it::setup)
        }
        imageCards.forEach {
            FutureBytes2(
                { DownloadImage.byProduct(MyApplication.appContext, it.product) },
                it::set
            )
        }
        if (UIPreferences.isNexradOnMainScreen) {
            getAllRadars()
        }
    }

    override fun onResume() {
        super.onResume()
        if (UIPreferences.mainScreenRefreshToTop) {
            scrollView.smoothScrollTo(0, 0)
        }
        if (glviewInitialized) {
            nexradState.onResume()
        }
        locationLabel.text = Location.name
        refreshTextSize()
        if (UIPreferences.isNexradOnMainScreen) {
            if (!glviewInitialized) {
                nexradState.wxglSurfaceViews.indices.forEach {
                    NexradDraw.initGlviewMainScreen(
                        it,
                        nexradState,
                        nexradLongPressMenu.changeListener
                    )
                    glviewInitialized = true
                }
            }
        }
        if (UIPreferences.refreshLocMin != 0 || dataNotInitialized) {
            if (downloadTimer.isRefreshNeeded() || currentConditions.latLon.toString() != Location.latLon.toString()) {
                getContent()
            }
            dataNotInitialized = false
        }
    }

    private fun refreshTextSize() {
        cardCurrentConditions?.refreshTextSize()
        locationLabel.refreshTextSize(TextSize.MEDIUM)
        sevenDayCards.forEach {
            it.refreshTextSize()
        }
        textCards.forEach {
            it.refreshTextSize()
        }
        hazardsCards.forEach {
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
        }
    }

    private fun getRadar(idx: Int) {
        // if radarForLocation is not equal to -1 it means the user has a radar for the current location (default)
        //if (radarForLocation != -1)
        if (radarForLocationIndex != -1 && !radarLocationChangedList[radarForLocationIndex]) {
            nexradState.wxglRenders[radarForLocationIndex].state.rid = Location.rid
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
            archived = false, forceReset = false
        )
        FutureVoid({
            if (Location.isUS && mActivity != null) {
                NexradDraw.plotRadar(
                    nexradState.wxglRenders[idx],
                    ::getGPSFromDouble,
                    ::getLatLon,
                    false
                )
            }
        }) {
            //if (idx == oglrIdx) {
            //    radarTimeStampLocal = getRadarTimeStampForHomescreen(nexradState.wxglRenders[oglrIdx].rid)
            //}
            // NOTE: below was backed out, data structures for these features only support one radar site
            // so locfrag and multi-pane don't current support. Would be nice to fix someday.
            // Show extras a few lines above was changed from false to true along with few lines added below
            // some time ago there were crashes caused by this additional content but I don't recall the details
            // guess it's worth another try to see if the issue back then was fixed in the various re-writes that have
            // occurred since
	    
	        //elys mod - not removing those!!!  unless crashing....
	        if (PolygonType.HAIL_LABELS.pref) {
	    	    NexradRenderTextObject.updateHailLabels(nexradState.wxglTextObjects) //was numberOfRadars
            }

            if (Location.isUS && idx == 0) {
                if (PolygonType.SPOTTER_LABELS.pref) {
                    NexradRenderTextObject.updateSpotterLabels(nexradState.wxglTextObjects)
                }
            }
            if (PolygonType.OBS.pref) {
                NexradRenderTextObject.updateObservations(nexradState.wxglTextObjects)
            }
            ////////

            nexradState.wxglSurfaceViews[idx].requestRender()
            if (idx == radarForLocationIndex) {
                radarTime =
                    getRadarTimeStampForHomescreen(nexradState.wxglRenders[radarForLocationIndex].state.rid)
                cardCurrentConditions?.setStatus(currentConditionsTime + radarTime)
            }
            if (RadarPreferences.wxoglCenterOnLocation) {
                nexradState.wxglSurfaceViews[idx].resetView()
            }
        }
        NexradLayerDownload.download(
            MyApplication.appContext,
            nexradState.wxglRenders[idx],
            nexradState.wxglSurfaceViews[idx],
            nexradState.wxglTextObjects,
            {},
            false
        )
    }

    private fun getRadarTimeStampForHomescreen(radarSite: String): String {
        val tokens = NexradUtil.getRadarInfo(MyApplication.appContext, radarSite).split(" ")
        val timestamp = if (tokens.size > 3) {
            tokens[3]
        } else {
            ""
        }
        return if (radarForLocationIndex != -1) {
            " " + nexradState.wxglRenders[radarForLocationIndex].state.rid + ": " + timestamp
        } else {
            ""
        }
    }

    private fun getRadarTimeStamp(string: String, j: Int): String {
        val tokens = string.split(" ")
        val timestamp = if (tokens.size > 3) {
            tokens[3]
        } else {
            ""
        }
        return nexradState.wxglRenders[j].state.rid + ": " + timestamp + " (" + RadarSites.getName(
            nexradState.wxglRenders[j].state.rid
        ) + ")"
    }

    @Suppress("EmptyMethod")
    private fun getGPSFromDouble() {
    }

    // main screen will not show GPS so if configured just show it off the screen
    // NOTE - this was backed out as it's not a good solution when user enables "center radar on location", removed private fun getLatLon() = LatLon(0.0, 0.0)
    private fun getLatLon() = LatLon(Location.x, Location.y)

    override fun onPause() {
        if (glviewInitialized) {
            nexradState.onPause()
        }
        super.onPause()
    }

    private fun setImageOnClick() {
        UtilityHomeScreen.launch(activityReference, imageCards)
    }

    private fun getAllRadars() {
        nexradState.wxglSurfaceViews.indices.forEach {
            getRadar(it)
        }
    }

    private fun radarTimestamps(): List<String> =
        (0 until nexradState.wxglSurfaceViews.size).map {
            getRadarTimeStamp(
                nexradState.wxglRenders[it].wxglNexradLevel3.timestamp,
                it
            )
        }

    private fun setupLocationStatusDialogue() {
        locationStatusDialogue = ObjectDialogue(activityReference, locationStatusDialogueList)
        locationStatusDialogue!!.connect { dialog, index ->
            val item = locationStatusDialogueList[index]
            val renderOrNull = if (nexradState.wxglRenders.size > 0) {
                nexradState.wxglRenders[0]
            } else {
                null
            }
            UtilityLocationFragment.handleIconTap(
                item,
                renderOrNull,
                activityReference,
                ::getContent,
                nexradState::resetAllGlview,
                ::getAllRadars
            )
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(s: String) {
        val newRadarSite = s.split(" ")[0]
        val oldRadarSite = nexradState.radarSite
        nexradState.adjustPaneTo(nexradState.curRadar, newRadarSite)
        // if user changes any non-location based nexrad this change will be permanent via homescreen string change
        if (nexradState.curRadar != radarForLocationIndex) {
            UIPreferences.homescreenFav = UIPreferences.homescreenFav.replace(
                "NXRD-$oldRadarSite",
                "NXRD-" + nexradState.radarSite
            )
            Utility.writePref(activityReference, "HOMESCREEN_FAV", UIPreferences.homescreenFav)
        }
        radarLocationChangedList[nexradState.curRadar] = true
        getRadar(nexradState.curRadar)
    }

    private fun getForecastData() {
        if (locationChanged) {
            boxForecast?.removeChildren()
            boxHazards?.removeChildren()
            boxHazards?.visibility = View.GONE
            locationChanged = false
        }
        FutureVoid(::getCc, ::updateCc)
        FutureVoid(::get7day, ::update7day)
        FutureVoid(::getHazards, ::updateHazards)
    }

    private fun getCc() {
        currentConditions = CurrentConditions(Location.currentLocation)
        currentConditions.timeCheck(MyApplication.appContext)
    }

    private fun updateCc() {
        if (isAdded) {
            cardCurrentConditions?.let {
                currentConditionsTime = currentConditions.status
                if (UIPreferences.homescreenFav.contains("TXT-CC2")) {
                    it.update(currentConditions, Location.isUS, radarTime)
                } else {
                    it.setTopLine(currentConditions.data)
                    it.setStatus(currentConditionsTime + radarTime)
                }
            }
        }
    }

    private fun get7day() {
        sevenDay = SevenDay(Location.currentLocation)
        Utility.writePref(MyApplication.appContext, "FCST", sevenDay.sevenDayLong)
    }

    private fun update7day() {
        if (isAdded) {
            if (UIPreferences.homescreenFav.contains("TXT-7DAY")) {
                sevenDayCollection?.update(sevenDay, Location.latLon, Location.isUS)
                if (!Location.isUS) {
                    CanadaLegal(activityReference, boxForecast!!, "")
                }
            }
        }
    }

    private fun getHazards() {
        hazards = if (Location.isUS(Location.currentLocation)) {
            Hazards(Location.currentLocation)
        } else {
            Hazards()
        }
    }

    private fun updateHazards() {
        if (isAdded) {
            if (Location.isUS) {
                if (hazards.titles.isEmpty()) {
                    if (UIPreferences.homescreenFav.contains("TXT-HAZ")) {
                        boxHazards?.removeChildrenAndLayout()
                        boxHazards?.visibility = View.GONE
                    }
                } else {
                    if (UIPreferences.homescreenFav.contains("TXT-HAZ")) {
                        boxHazards?.visibility = View.VISIBLE
                        CardHazards(activityReference, boxHazards, hazards)
                    }
                }
            } else {
                if (hazards.getHazardsShort() != "") {
                    val hazardsSum = hazards.getHazardsShort().uppercase(Locale.US)
                    if (UIPreferences.homescreenFav.contains("TXT-HAZ")) {
                        boxHazards?.visibility = View.VISIBLE
                        CardHazardsCA(activityReference, boxHazards, hazards, hazardsSum)
                    }
                }
            }
        }
    }

    // used in WX.kt keyboard shortcut
    fun showLocations() {
        locationDialogue.show()
    }

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

    private var mActivity: FragmentActivity? = null

    // FIXME duplicate for 2 other areas
    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                mActivity = activity
            }
            return mActivity!!
        }
}

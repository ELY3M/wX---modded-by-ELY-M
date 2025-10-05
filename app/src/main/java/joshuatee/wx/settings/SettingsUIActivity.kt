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
//theme add 

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.core.app.NavUtils
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.ui.NumberPicker
import joshuatee.wx.ui.TextEdit
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

class SettingsUIActivity : BaseActivity() {

    private var tilesPerRowStart = 0
    private var navDrawerMainScreen = false
    private var navDrawerMainScreenOnRight = true
    private lateinit var box: VBox
    private lateinit var et1: TextEdit
    private lateinit var et2: TextEdit
    private lateinit var et3: TextEdit

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_ui, null, false)
        setTitle("User Interface", GlobalVariables.PREFERENCES_HELP_TITLE)
        box = VBox.fromResource(this)
        tilesPerRowStart = UIPreferences.tilesPerRow
        navDrawerMainScreen = UIPreferences.navDrawerMainScreen
        navDrawerMainScreenOnRight = UIPreferences.navDrawerMainScreenOnRight
        addEditText()
        addCards()
        addSwitchTheme()
        addSwitch()
        addNumberPickers()
    }

    private fun addSwitchTheme() {
        box.addWidget(
            ObjectSpinner(
                this,
                "Theme (restarts app)",
                "THEME_BLUE",
                "BlackAqua",
                R.string.spinner_theme_label,
                UIPreferences.themes
            )
        )
    }

    private fun addCards() {
        var navDrawerStatus = " (disabled)"
        if (UIPreferences.navDrawerMainScreen) {
            navDrawerStatus = ""
        }
        box.addWidget(
            CardText(
                this,
                "Navigation Drawer Configuration$navDrawerStatus",
                SettingsNavDrawerActivity::class.java
            )
        )
        Card(this, R.id.cv_tab_labels)
    }

    private fun addSwitch() {
        val configs = listOf(
            Switch(this, "Check for SPC MCD/Watches", "CHECKSPC", R.string.checkspc_switch_label),
            Switch(this, "Check for WPC MPDs", "CHECKWPC", R.string.checkwpc_switch_label),
            Switch(this, "Check for TOR,TST,FFW", "CHECKTOR", R.string.checktor_switch_label),
            Switch(
                this,
                "Dual-pane radar from main screen",
                "DUALPANE_RADAR_ICON",
                R.string.dualpane_radar_icon_tv
            ),
            Switch(
                this,
                "Fahrenheit in current conditions/7day",
                "UNITS_F",
                R.string.units_f_label
            ),
            Switch(this, "Fullscreen mode", "FULLSCREEN_MODE", R.string.fullscreen_mode_label),
            Switch(
                this,
                "Hide top toolbar (restarts app)",
                "HIDE_TOP_TOOLBAR",
                R.string.hide_top_toolbar_label
            ),
	    //elys mod - I hate fab buttons!!!
            Switch(this, "Main screen radar button (requires restart)", "UI_MAIN_SCREEN_RADAR_FAB", R.string.main_screen_radar_button),
            Switch(
                this,
                "Hourly/Nexrad: show with AM/PM",
                "HOURLY_SHOW_AM_PM",
                R.string.hourly_show_am_pm
            ),
            Switch(this, "Millibars in current conditions", "UNITS_M", R.string.units_m_label),
            Switch(
                this,
                "Navigation Drawer on main screen",
                "NAV_DRAWER_MAIN_SCREEN",
                R.string.nav_drawer_main_screen_label
            ),
            Switch(
                this,
                "Navigation Drawer is on right side",
                "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT",
                R.string.nav_drawer_main_screen_on_right_label
            ),
            Switch(
                this,
                "Record screen for sharing",
                "RECORD_SCREEN_SHARE",
                R.string.record_screen_share_label
            ),
            Switch(this, "Simple mode (restarts app)", "SIMPLE_MODE", R.string.simple_mode_label),
            Switch(
                this,
                "Translate abbreviations",
                "TRANSLATE_TEXT",
                R.string.translate_text_label
            ),
            Switch(
                this,
                "Use new NWS API for 7 day",
                "USE_NWS_API_SEVEN_DAY",
                R.string.use_nws_api
            ),
            Switch(
                this,
                "Use new NWS API for Hourly",
                "USE_NWS_API_HOURLY",
                R.string.use_nws_api_hourly
            ),
            Switch(
                this,
                "WFO screen remembers location",
                "WFO_REMEMBER_LOCATION",
                R.string.wfo_remember
            ),
            Switch(
                this,
                "Widgets prevent opening app on tap",
                "UI_WIDGET_PREVENT_TAP",
                R.string.widget_prevent_tap
            ),
        )
        configs.forEach {
            box.addWidget(it)
        }
    }

    private fun addNumberPickers() {
        val numberPickers = listOf(
            NumberPicker(
                this,
                "Animation - frames for toolbar icon",
                "UI_ANIM_ICON_FRAMES",
                R.string.np_anim_generic_label,
                10,
                2,
                40
            ),
            NumberPicker(
                this,
                "Card corner radius",
                "CARD_CORNER_RADIUS",
                R.string.card_corner_radius_np_label,
                0,
                0,
                10
            ),
            NumberPicker(
                this,
                "Home screen text length",
                "HOMESCREEN_TEXT_LENGTH_PREF",
                R.string.homescreen_text_length_np_label,
                500,
                50,
                1000
            ),
            NumberPicker(
                this,
                "Image tiles per row",
                "UI_TILES_PER_ROW",
                R.string.tiles_per_row_label,
                UIPreferences.tilesPerRowDefault,
                3,
                10
            ),
            NumberPicker(
                this,
                "NWS icon size",
                "NWS_ICON_SIZE_PREF",
                R.string.nws_icon_size_np_label,
                UIPreferences.nwsIconSizeDefault,
                1,
                50
            ),
            NumberPicker(
                this,
                "Refresh interval for location in minutes",
                "REFRESH_LOC_MIN",
                R.string.refresh_loc_min_np_label,
                10,
                0,
                120
            ),
            NumberPicker(
                this,
                "Text size",
                "TEXTVIEW_FONT_SIZE",
                R.string.textview_font_size_np_label,
                UIPreferences.normalTextSizeDefault,
                12,
                25
            ),
            NumberPicker(
                this,
                "Text to speech speed, requires app restart",
                "TTS_SPEED_PREF",
                R.string.tts_speed_np_label,
                10,
                1,
                20
            ),
            NumberPicker(
                this,
                "UI elevation height",
                "ELEVATION_PREF",
                R.string.elevation_np_label,
                UIPreferences.elevationPrefDefault,
                0,
                30
            ),
        )
        numberPickers.forEach {
            box.addWidget(it)
        }
    }

    override fun onStop() {
        UIPreferences.tabHeaders[0] = et1.text
        UIPreferences.tabHeaders[1] = et2.text
        UIPreferences.tabHeaders[2] = et3.text
        Utility.writePref(this, "TAB1_HEADER", et1.text)
        Utility.writePref(this, "TAB2_HEADER", et2.text)
        Utility.writePref(this, "TAB3_HEADER", et3.text)
        MyApplication.initPreferences(this)
        UIPreferences.navDrawerMainScreen =
            Utility.readPref(this@SettingsUIActivity, "NAV_DRAWER_MAIN_SCREEN", "false")
                .startsWith("t")
        UIPreferences.navDrawerMainScreenOnRight =
            Utility.readPref(this@SettingsUIActivity, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true")
                .startsWith("t")
        if ((UIPreferences.tilesPerRow != tilesPerRowStart)
            || (UIPreferences.navDrawerMainScreen != navDrawerMainScreen)
            || (UIPreferences.navDrawerMainScreenOnRight != navDrawerMainScreenOnRight)
        ) {
            Utility.restart()
        }
        UIPreferences.settingsUIVisitedNeedRefresh = true
        super.onStop()
    }

    private fun addEditText() {
        et1 = TextEdit(this, R.id.et1)
        et2 = TextEdit(this, R.id.et2)
        et3 = TextEdit(this, R.id.et3)
        et1.text = UIPreferences.tabHeaders[0]
        et2.text = UIPreferences.tabHeaders[1]
        et3.text = UIPreferences.tabHeaders[2]
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            listOf(et1, et2, et3).forEach {
                it.setTextColor(Color.BLACK)
                it.setHintTextColor(Color.GRAY)
            }
        }

        val label1: TextView = findViewById(R.id.tab1_et_label)
        val label2: TextView = findViewById(R.id.tab2_et_label)
        val label3: TextView = findViewById(R.id.tab3_et_label)

        label1.setOnClickListener {
            ObjectDialogue(
                this,
                "Change the textual label for the 1st tab. The default is Local."
            )
        }
        label2.setOnClickListener {
            ObjectDialogue(
                this,
                "Change the textual label for the 2nd tab. The default is SPC (NWS Storm Prediction Center)."
            )
        }
        label3.setOnClickListener {
            ObjectDialogue(
                this,
                "Change the textual label for the 3rd tab. The default is MISC for miscellaneous."
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                UtilityLog.d("wx", "DEBUG - home")
                UIPreferences.navDrawerMainScreen =
                    Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
                UIPreferences.navDrawerMainScreenOnRight =
                    Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true")
                        .startsWith("t")
                if ((UIPreferences.tilesPerRow != tilesPerRowStart)
                    || (UIPreferences.navDrawerMainScreen != navDrawerMainScreen)
                    || (UIPreferences.navDrawerMainScreenOnRight != navDrawerMainScreenOnRight)
                )
                    Utility.restart()
                else
                    NavUtils.navigateUpFromSameTask(this)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

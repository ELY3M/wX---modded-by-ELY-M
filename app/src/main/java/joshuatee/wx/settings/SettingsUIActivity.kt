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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog

class SettingsUIActivity : BaseActivity() {

    private val colorArr = listOf(
        "blue",
        "mixedBlue",
        "darkBlue",
        "black",
        "green",
        "gray",
        "white",
        "whiteNew",
        "orange",
	"BlackAqua"
    )
    private val textSizeArr = mutableListOf<String>()
    private lateinit var et1: EditText
    private lateinit var et2: EditText
    private lateinit var et3: EditText
    private var tilesPerRowStart = 0

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_ui, null, false)
        toolbar.subtitle = "Please tap on text for additional help."
        ObjectCard(this, R.id.cv_tab_labels)
        tilesPerRowStart = UIPreferences.tilesPerRow
        setupEditText()
        (0 until 20).forEach { textSizeArr.add(((it + 1) * 50).toString()) }
        val ll: LinearLayout = findViewById(R.id.ll)
        ll.addView(
                ObjectSettingsSpinner(
                        this,
                        this,
                        "Theme (restarts app)",
                        "THEME_BLUE",
                        "white",
                        R.string.spinner_theme_label,
                        colorArr
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Icons evenly spaced",
                        "UI_ICONS_EVENLY_SPACED",
                        R.string.icons_spacing_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Fullscreen mode",
                        "FULLSCREEN_MODE",
                        R.string.fullscreen_mode_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Lock toolbars",
                        "LOCK_TOOLBARS",
                        R.string.lock_toolbars_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Simple mode (restarts app)",
                        "SIMPLE_MODE",
                        R.string.simple_mode_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Hide top toolbar (restarts app)",
                        "HIDE_TOP_TOOLBAR",
                        R.string.hide_top_toolbar_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Use fahrenheit",
                        "UNITS_F",
                        R.string.units_f_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Use millibars",
                        "UNITS_M",
                        R.string.units_m_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Reduce size of tile images",
                        "TILE_IMAGE_DOWNSIZE",
                        R.string.tile_img_resize_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Use FAB in models",
                        "FAB_IN_MODELS",
                        R.string.fab_in_models_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "NWS Text, remove line breaks",
                        "NWS_TEXT_REMOVELINEBREAKS",
                        R.string.nws_text_removelinebreak_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Radar: transparent toolbars",
                        "RADAR_TOOLBAR_TRANSPARENT",
                        R.string.radar_toolbar_transparent_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Radar: transparent status bar",
                        "RADAR_STATUSBAR_TRANSPARENT",
                        R.string.radar_statusbar_transparent_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Radar: immersive mode",
                        "RADAR_IMMERSIVE_MODE",
                        R.string.radar_immersive_mode_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Record screen for sharing",
                        "RECORD_SCREEN_SHARE",
                        R.string.record_screen_share_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Prevent accidental exit",
                        "PREF_PREVENT_ACCIDENTAL_EXIT",
                        R.string.prevent_accidental_exit_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show VR button on main screen",
                        "VR_BUTTON",
                        R.string.vr_button_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "WFO - remember location",
                        "WFO_REMEMBER_LOCATION",
                        R.string.wfo_remember
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Main screen radar button (requires restart)",
                        "UI_MAIN_SCREEN_RADAR_FAB",
                        R.string.mainscreen_radar_button
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Widgets: prevent opening app on tap",
                        "UI_WIDGET_PREVENT_TAP",
                        R.string.widget_prevent_tap
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Use the AWC Radar mosaic images instead of the main NWS images.",
                        "USE_AWC_RADAR_MOSAIC",
                        R.string.ui_awc_radar_mosaic
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "UI elevation height",
                        "ELEVATION_PREF",
                        R.string.elevation_np_label,
                        0,
                        0,
                        30
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "NWS icon size",
                        "NWS_ICON_SIZE_PREF",
                        R.string.nws_icon_size_np_label,
                        20,
                        1,
                        50
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Card corner radius",
                        "CARD_CORNER_RADIUS",
                        R.string.card_corner_radius_np_label,
                        0,
                        0,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Image tiles per row",
                        "UI_TILES_PER_ROW",
                        R.string.tiles_per_row_label,
                        3,
                        3,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Animation - frames for toolbar icon",
                        "UI_ANIM_ICON_FRAMES",
                        R.string.np_anim_generic_label,
                        6,
                        2,
                        40
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Homescreen text length",
                        "HOMESCREEN_TEXT_LENGTH_PREF",
                        R.string.homescreen_text_length_np_label,
                        500,
                        50,
                        1000
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Text size",
                        "TEXTVIEW_FONT_SIZE",
                        R.string.textview_fontsize_np_label,
                        16,
                        12,
                        25
                ).card
        )
    }

    override fun onStop() {
        super.onStop()
        MyApplication.tabHeaders[0] = et1.text.toString()
        MyApplication.tabHeaders[1] = et2.text.toString()
        MyApplication.tabHeaders[2] = et3.text.toString()
        Utility.writePref(this, "TAB1_HEADER", et1.text.toString())
        Utility.writePref(this, "TAB2_HEADER", et2.text.toString())
        Utility.writePref(this, "TAB3_HEADER", et3.text.toString())
        MyApplication.initPreferences(this)
    }

    private fun setupEditText() {
        et1 = findViewById(R.id.tab1_et)
        et2 = findViewById(R.id.tab2_et)
        et3 = findViewById(R.id.tab3_et)
        et1.setText(MyApplication.tabHeaders[0])
        et2.setText(MyApplication.tabHeaders[1])
        et3.setText(MyApplication.tabHeaders[2])
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            et1.setTextColor(Color.BLACK)
            et2.setTextColor(Color.BLACK)
            et3.setTextColor(Color.BLACK)
            et1.setHintTextColor(Color.GRAY)
            et2.setHintTextColor(Color.GRAY)
            et3.setHintTextColor(Color.GRAY)
        }
    }

    override fun onBackPressed() {
        if (UIPreferences.tilesPerRow != tilesPerRowStart)
            UtilityAlertDialog.restart()
        else
            super.onBackPressed()
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.settings

import android.app.NotificationManager
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import joshuatee.wx.Extensions.setPadding

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog

class ObjectSettingsCheckBox(context: Context, label: String, pref: String, strId: Int) {

    private val objectCard = ObjectCard(context)
    private val checkBox = CheckBox(context)

    init {
        val textView = TextView(context)
        ObjectCardText.textViewSetup(textView)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        textView.setTextColor(UIPreferences.backgroundColor)
        textView.setPadding(MyApplication.paddingSettings)
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        textView.text = label
        textView.gravity = Gravity.CENTER_VERTICAL
        val strInner = context.resources.getString(strId)
        textView.setOnClickListener { ObjectDialogue(context, strInner) }
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.HORIZONTAL, Gravity.CENTER_VERTICAL)
        objectLinearLayout.matchParent()
        objectLinearLayout.addView(textView)
        checkBox.gravity = Gravity.CENTER_VERTICAL
        val truePrefs = listOf(
                "COD_HW_DEFAULT",
                "DUALPANE_SHARE_POSN",
                "UNITS_F",
                "UNITS_M",
                "FAB_IN_MODELS",
                "NWS_TEXT_REMOVELINEBREAKS",
                "RECORD_SCREEN_SHARE",
                "PREF_PREVENT_ACCIDENTAL_EXIT",
                "COD_LOCDOT_DEFAULT",
                "UI_MAIN_SCREEN_RADAR_FAB",
                "RADAR_TOOLBAR_TRANSPARENT",
                "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT",
                "USE_NWS_API_SEVEN_DAY"
        )
        checkBox.isChecked = Utility.readPref(context, pref, java.lang.Boolean.toString(truePrefs.contains(pref))) == "true"
             || ( pref.startsWith(UtilityNavDrawer.getPrefVar("")) && Utility.readPref(context, pref, "") != "false")
        checkBox.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                if (pref == "MEDIA_CONTROL_NOTIF") {
                    if (Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("f")) {
                        UtilityNotification.createMediaControlNotification(context.applicationContext, "")
                    }
                }
                Utility.writePref(context, pref, "true")
            } else {
                if (pref == "MEDIA_CONTROL_NOTIF") {
                    if (Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("t")) {
                        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notifier.cancel("wx_media", 1)
                    }
                }
                Utility.writePref(context, pref, "false")
            }
            if (pref == "SIMPLE_MODE") {
                if (MyApplication.simpleMode != Utility.readPref(context, "SIMPLE_MODE", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    UtilityAlertDialog.restart()
                }
            }
            if (pref == "HIDE_TOP_TOOLBAR") {
                if (UIPreferences.hideTopToolbar != Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    UtilityAlertDialog.restart()
                }
            }
            if (pref == "UI_MAIN_SCREEN_RADAR_FAB") {
                if (UIPreferences.mainScreenRadarFab != Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    UtilityAlertDialog.restart()
                }
            }
            when (pref) {
                "COD_WARNINGS_DEFAULT", "RADAR_SHOW_MPD", "RADAR_SHOW_WATCH" -> Utility.writePref(context, "RESTART_NOTIF", "true")
                "RADAR_STATE_HIRES", "RADAR_COUNTY_HIRES", "RADAR_HW_ENH_EXT", "RADAR_CAMX_BORDERS", "WXOGL_SPOTTERS", "WXOGL_SPOTTERS_LABEL" -> {
                    MyApplication.initPreferences(context)
                    when (pref) {
                        "RADAR_STATE_HIRES" -> MyApplication.initRadarGeometryByType(context, GeographyType.STATE_LINES)
                        "RADAR_COUNTY_HIRES" -> MyApplication.initRadarGeometryByType(context, GeographyType.COUNTY_LINES)
                        "RADAR_HW_ENH_EXT" -> MyApplication.initRadarGeometryByType(context, GeographyType.HIGHWAYS_EXTENDED)
                        "RADAR_CAMX_BORDERS" -> MyApplication.initRadarGeometryByType(context, GeographyType.STATE_LINES)
                    }
                    GeographyType.refresh()
                }
            }
        }
        objectLinearLayout.addView(checkBox)
        objectCard.addView(objectLinearLayout.linearLayout)
    }

    fun isChecked(value: Boolean) { checkBox.isChecked = value }

    val card get() = objectCard.card

    internal fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) { checkBox.setOnCheckedChangeListener(listener) }
}



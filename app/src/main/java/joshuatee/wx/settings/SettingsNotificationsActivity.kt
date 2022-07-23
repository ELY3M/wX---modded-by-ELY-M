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

package joshuatee.wx.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.widget.LinearLayout
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.RegExp
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility

class SettingsNotificationsActivity : BaseActivity() {

    private lateinit var box: LinearLayout

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        box = findViewById(R.id.linearLayout)
        toolbar.subtitle = "Please tap on text for additional help."
        val cardSound = ObjectCardText(this, box, "Notification sound chooser", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        val cardWFOFilter = ObjectCardText(this, box, "WFO notification filter", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        ObjectCardText(
                this,
                box,
                "Text product notifications: " + UtilityNotificationTextProduct.showAll(),
                UIPreferences.textSizeNormal,
                UIPreferences.paddingSettings
        )
        cardSound.setOnClickListener { notifSoundPicker() }
        cardWFOFilter.setOnClickListener { showWFONotificationFilterDialogue() }
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "NHC Advisories EPAC",
                        "ALERT_NHC_EPAC_NOTIFICATION",
                        R.string.b_nhc_epac
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "NHC Advisories ATL",
                        "ALERT_NHC_ATL_NOTIFICATION",
                        R.string.b_nhc_atl
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "SPC MCD",
                        "ALERT_SPCMCD_NOTIFICATION",
                        R.string.b_mcd
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "SPC SWO",
                        "ALERT_SPCSWO_NOTIFICATION",
                        R.string.b_swo
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "SPC SWO include slight",
                        "ALERT_SPCSWO_SLIGHT_NOTIFICATION",
                        R.string.b_swo2
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "SPC Watch",
                        "ALERT_SPCWAT_NOTIFICATION",
                        R.string.b_wat
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "US Tornado",
                        "ALERT_TORNADO_NOTIFICATION",
                        R.string.b_tornado
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "WPC MPD",
                        "ALERT_WPCMPD_NOTIFICATION",
                        R.string.b_mpd
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: NHC Advisories EPAC",
                        "ALERT_NOTIFICATION_SOUND_NHC_EPAC",
                        R.string.alert_sound_nhc_epac_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: NHC Advisories ATL",
                        "ALERT_NOTIFICATION_SOUND_NHC_ATL",
                        R.string.alert_sound_nhc_atl_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: SPC MCD",
                        "ALERT_NOTIFICATION_SOUND_SPCMCD",
                        R.string.alert_sound_spcmcd_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: SPC SWO",
                        "ALERT_NOTIFICATION_SOUND_SPCSWO",
                        R.string.alert_sound_spcswo_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: SPC Watch",
                        "ALERT_NOTIFICATION_SOUND_SPCWAT",
                        R.string.alert_sound_spcwat_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: Text products",
                        "ALERT_NOTIFICATION_SOUND_TEXT_PROD",
                        R.string.alert_sound_text_prod_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: US Tornado",
                        "ALERT_NOTIFICATION_SOUND_TORNADO",
                        R.string.alert_sound_tornado_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Sound: WPC MPD Sound",
                        "ALERT_NOTIFICATION_SOUND_WPCMPD",
                        R.string.alert_sound_wpcmpd_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Alert only once",
                        "ALERT_ONLYONCE",
                        R.string.alert_onlyonce_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Auto cancel notifs",
                        "ALERT_AUTOCANCEL",
                        R.string.alert_autocancel_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Blackout alert sounds",
                        "ALERT_BLACKOUT",
                        R.string.alert_blackout_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Notif text to speech",
                        "NOTIF_TTS",
                        R.string.tv_notif_tts_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Play sound repeatedly",
                        "NOTIF_SOUND_REPEAT",
                        R.string.tv_notif_sound_repeat_label
                ).get()
        )
        box.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Tor warn override blackout",
                        "ALERT_BLACKOUT_TORNADO",
                        R.string.alert_blackout_tornado_label
                ).get()
        )
        box.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Notification check interval in minutes",
                        "ALERT_NOTIFICATION_INTERVAL",
                        R.string.alert_interval_np_label,
                        12,
                        1,
                        121
                ).card
        )
        box.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Notification blackout - AM(h)",
                        "ALERT_BLACKOUT_AM",
                        R.string.alert_blackout_am_np_label,
                        7,
                        0,
                        23
                ).card
        )
        box.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Notification blackout - PM(h)",
                        "ALERT_BLACKOUT_PM",
                        R.string.alert_blackout_pm_np_label,
                        22,
                        0,
                        23
                ).card
        )
    }

    override fun onStop() {
        super.onStop()
        if (NotificationPreferences.notifTts != Utility.readPref(this, "NOTIF_TTS", "false").startsWith("t")) {
            showFileWritePermsDialogue()
        }
        MyApplication.initPreferences(this)
        restartNotifications()
    }

    private fun restartNotifications() {
        UtilityWXJobService.startService(this)
        Utility.writePref(this, "RESTART_NOTIF", "false")
    }

    private fun notifSoundPicker() {
        var uri: Uri? = null
        if (NotificationPreferences.notifSoundUri != "") {
            uri = Uri.parse(NotificationPreferences.notifSoundUri)
        }
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val uri = data!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                NotificationPreferences.notifSoundUri = uri.toString()
                Utility.writePref(this, "NOTIF_SOUND_URI", NotificationPreferences.notifSoundUri)
            } else {
                NotificationPreferences.notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                Utility.writePrefWithNull(this, "NOTIF_SOUND_URI", null)
            }
        }
    }

    private fun showWFONotificationFilterDialogue() {
        val items = listOf("Air Quality Alert", "Wind Advisory", "Lake Wind Advisory", "Child Abduction Emergency", "Freeze Warning")
        val checkedItems = BooleanArray(items.size)
        val selectedItems = mutableListOf<Int>()
        val nwsWfoFilterStr = Utility.readPref(this, "NOTIF_WFO_FILTER", "")
        var valueInArray: Boolean
        items.indices.forEach { i ->
            valueInArray = RegExp.colon.split(nwsWfoFilterStr).any { it == items[i] }
            if (valueInArray) {
                checkedItems[i] = true
                selectedItems.add(i)
            } else {
                checkedItems[i] = false
            }
        }
        var theme = R.style.PickerDialogTheme
        if (Utility.isThemeAllBlack()) {
            theme = R.style.PickerDialogThemeDark
        }
        val dialog = AlertDialog.Builder(this, theme)
                .setTitle("Choose which Local NWS Alerts to not show:")
                .setMultiChoiceItems(items.toTypedArray(), checkedItems) { _, indexSelected, isChecked ->
                    if (isChecked) {
                        selectedItems.add(indexSelected)
                    } else if (selectedItems.contains(indexSelected)) {
                        selectedItems.remove(Integer.valueOf(indexSelected))
                    }
                }.setPositiveButton("OK") { _, _ ->
                    var nwsWfoFilterStrLoc = ""
                    selectedItems.indices.forEach { nwsWfoFilterStrLoc += items[selectedItems[it]] + ":" }
                    Utility.writePref(this, "NOTIF_WFO_FILTER", nwsWfoFilterStrLoc)
                }.setNegativeButton("Cancel") { _, _ ->
                }.create()
        dialog.show()
    }

    private fun showFileWritePermsDialogue() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), fileWritePerm)
                }
            }
        }
    }

    private val fileWritePerm = 5002

    // FIXME TODO decide what to do with this
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            fileWritePerm -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            }
//        }
    }
}

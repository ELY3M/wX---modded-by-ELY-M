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
import android.app.Activity
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import androidx.activity.result.ActivityResult
//import androidx.activity.result.ActivityResultCallback
//import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.RegExp
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.*
import joshuatee.wx.ui.ObjectNumberPicker
import joshuatee.wx.util.Utility

class SettingsNotificationsActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Notifications", "Please tap on text for additional help.")
        box = VBox.fromResource(this)
        CardText(this, box, "Notification sound chooser", UIPreferences.textSizeNormal, { notifSoundPicker() }, UIPreferences.paddingSettings)
        CardText(this, box, "WFO notification filter", UIPreferences.textSizeNormal, { showWFONotificationFilterDialogue() }, UIPreferences.paddingSettings)
        CardText(this, box,
                "Text product notifications: " + UtilityNotificationTextProduct.showAll(),
                UIPreferences.textSizeNormal,
                UIPreferences.paddingSettings
        )
        val configs = listOf(
            ObjectSwitch(this, "NHC Advisories EPAC", "ALERT_NHC_EPAC_NOTIFICATION", R.string.b_nhc_epac),
            ObjectSwitch(this, "NHC Advisories ATL", "ALERT_NHC_ATL_NOTIFICATION", R.string.b_nhc_atl),
            ObjectSwitch(this, "SPC MCD", "ALERT_SPCMCD_NOTIFICATION", R.string.b_mcd),
            ObjectSwitch(this, "SPC SWO", "ALERT_SPCSWO_NOTIFICATION", R.string.b_swo),
            ObjectSwitch(this, "SPC SWO include slight", "ALERT_SPCSWO_SLIGHT_NOTIFICATION", R.string.b_swo2),
            ObjectSwitch(this, "SPC Watch", "ALERT_SPCWAT_NOTIFICATION", R.string.b_wat),
            ObjectSwitch(this, "US Tornado", "ALERT_TORNADO_NOTIFICATION", R.string.b_tornado),
            ObjectSwitch(this, "WPC MPD", "ALERT_WPCMPD_NOTIFICATION", R.string.b_mpd),
            ObjectSwitch(this, "Sound: NHC Advisories EPAC", "ALERT_NOTIFICATION_SOUND_NHC_EPAC", R.string.alert_sound_nhc_epac_label),
            ObjectSwitch(this, "Sound: NHC Advisories ATL", "ALERT_NOTIFICATION_SOUND_NHC_ATL", R.string.alert_sound_nhc_atl_label),
            ObjectSwitch(this, "Sound: SPC MCD", "ALERT_NOTIFICATION_SOUND_SPCMCD", R.string.alert_sound_spcmcd_label),
            ObjectSwitch(this, "Sound: SPC SWO", "ALERT_NOTIFICATION_SOUND_SPCSWO", R.string.alert_sound_spcswo_label),
            ObjectSwitch(this, "Sound: SPC Watch", "ALERT_NOTIFICATION_SOUND_SPCWAT", R.string.alert_sound_spcwat_label),
            ObjectSwitch(this, "Sound: Text products", "ALERT_NOTIFICATION_SOUND_TEXT_PROD", R.string.alert_sound_text_prod_label),
            ObjectSwitch(this, "Sound: US Tornado", "ALERT_NOTIFICATION_SOUND_TORNADO", R.string.alert_sound_tornado_label),
            ObjectSwitch(this, "Sound: WPC MPD Sound", "ALERT_NOTIFICATION_SOUND_WPCMPD", R.string.alert_sound_wpcmpd_label),
            ObjectSwitch(this, "Alert only once", "ALERT_ONLYONCE", R.string.alert_onlyonce_label),
            ObjectSwitch(this, "Auto cancel notifs", "ALERT_AUTOCANCEL", R.string.alert_autocancel_label),
            ObjectSwitch(this, "Blackout alert sounds", "ALERT_BLACKOUT", R.string.alert_blackout_label),
            ObjectSwitch(this, "Notif text to speech", "NOTIF_TTS", R.string.tv_notif_tts_label),
            ObjectSwitch(this, "Play sound repeatedly", "NOTIF_SOUND_REPEAT", R.string.tv_notif_sound_repeat_label),
            ObjectSwitch(this, "Tor warn override blackout", "ALERT_BLACKOUT_TORNADO", R.string.alert_blackout_tornado_label),
        )
        configs.forEach {
            box.addWidget(it.get())
        }
        val numberPickers = listOf(
            ObjectNumberPicker(this, "Notification check interval in minutes", "ALERT_NOTIFICATION_INTERVAL", R.string.alert_interval_np_label, 12, 1, 121),
            ObjectNumberPicker(this, "Notification blackout - AM(h)", "ALERT_BLACKOUT_AM", R.string.alert_blackout_am_np_label, 7, 0, 23),
            ObjectNumberPicker(this, "Notification blackout - PM(h)", "ALERT_BLACKOUT_PM", R.string.alert_blackout_pm_np_label, 22, 0, 23),
        )
        numberPickers.forEach {
            box.addWidget(it.get())
        }
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
//        startActivityForResult(intent, 1)
        startForResult.launch(intent)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            val uri = data!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
//            if (uri != null) {
//                NotificationPreferences.notifSoundUri = uri.toString()
//                Utility.writePref(this, "NOTIF_SOUND_URI", NotificationPreferences.notifSoundUri)
//            } else {
//                NotificationPreferences.notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
//                Utility.writePrefWithNull(this, "NOTIF_SOUND_URI", null)
//            }
//        }
//    }

    // https://developer.android.com/training/basics/intents/result#register
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
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
        val alertDialog = if (Utility.isThemeMaterial3()) {
            MaterialAlertDialogBuilder(this)
        } else {
            AlertDialog.Builder(this, theme)
        }
        alertDialog.setTitle("Choose which Local NWS Alerts to not show:")
        alertDialog.setMultiChoiceItems(items.toTypedArray(), checkedItems) { _, indexSelected, isChecked ->
            if (isChecked) {
                selectedItems.add(indexSelected)
            } else if (selectedItems.contains(indexSelected)) {
                selectedItems.remove(Integer.valueOf(indexSelected))
            }
        }
        alertDialog.setPositiveButton("OK") { _, _ ->
            var nwsWfoFilterStrLoc = ""
            selectedItems.indices.forEach { nwsWfoFilterStrLoc += items[selectedItems[it]] + ":" }
            Utility.writePref(this, "NOTIF_WFO_FILTER", nwsWfoFilterStrLoc)
        }
        alertDialog.setNegativeButton("Cancel") { _, _ -> }
        alertDialog.create()
        alertDialog.show()
    }

    private fun showFileWritePermsDialogue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), fileWritePerm)
            }
        }
    }

    private val fileWritePerm = 5002

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            fileWritePerm -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            }
//        }
    }
}

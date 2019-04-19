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
import android.view.View

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsNotificationsActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        toolbar.subtitle = "Please tap on text for additional help."
        val cardSound =
                ObjectCardText(this, ll, "Notification sound chooser", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardWFOFilter =
                ObjectCardText(this, ll, "WFO notification filter", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        ObjectCardText(
                this,
                ll,
                "Text product notifications: " + UtilityNotificationTextProduct.showAll(),
                MyApplication.textSizeNormal,
                MyApplication.paddingSettings
        )
        cardSound.setOnClickListener(View.OnClickListener { notifSoundPicker() })
        cardWFOFilter.setOnClickListener(View.OnClickListener { showWFONotificationFilterDialogue() })
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "US Tornado",
                        "ALERT_TORNADO_NOTIFICATION",
                        R.string.b_tornado
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC MCD",
                        "ALERT_SPCMCD_NOTIFICATION",
                        R.string.b_mcd
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC Watch",
                        "ALERT_SPCWAT_NOTIFICATION",
                        R.string.b_wat
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC SWO",
                        "ALERT_SPCSWO_NOTIFICATION",
                        R.string.b_swo
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC SWO include slight",
                        "ALERT_SPCSWO_SLIGHT_NOTIFICATION",
                        R.string.b_swo2
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "WPC MPD",
                        "ALERT_WPCMPD_NOTIFICATION",
                        R.string.b_mpd
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "NHC Advisories EPAC",
                        "ALERT_NHC_EPAC_NOTIFICATION",
                        R.string.b_nhc_epac
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "NHC Advisories ATL",
                        "ALERT_NHC_ATL_NOTIFICATION",
                        R.string.b_nhc_atl
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "US Tornado Sound",
                        "ALERT_NOTIFICATION_SOUND_TORNADO",
                        R.string.alert_sound_tornado_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC MCD Sound",
                        "ALERT_NOTIFICATION_SOUND_SPCMCD",
                        R.string.alert_sound_spcmcd_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC Watch Sound",
                        "ALERT_NOTIFICATION_SOUND_SPCWAT",
                        R.string.alert_sound_spcwat_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "SPC SWO Sound",
                        "ALERT_NOTIFICATION_SOUND_SPCSWO",
                        R.string.alert_sound_spcswo_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "WPC MPD Sound",
                        "ALERT_NOTIFICATION_SOUND_WPCMPD",
                        R.string.alert_sound_wpcmpd_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "NHC Advisories EPAC Sound",
                        "ALERT_NOTIFICATION_SOUND_NHC_EPAC",
                        R.string.alert_sound_nhc_epac_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "NHC Advisories ATL Sound",
                        "ALERT_NOTIFICATION_SOUND_NHC_ATL",
                        R.string.alert_sound_nhc_atl_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Text products Sound",
                        "ALERT_NOTIFICATION_SOUND_TEXT_PROD",
                        R.string.alert_sound_text_prod_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Play sound repeatedly",
                        "NOTIF_SOUND_REPEAT",
                        R.string.tv_notif_sound_repeat_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Notif text to speech",
                        "NOTIF_TTS",
                        R.string.tv_notif_tts_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Blackout alert sounds",
                        "ALERT_BLACKOUT",
                        R.string.alert_blackout_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Alert only once",
                        "ALERT_ONLYONCE",
                        R.string.alert_onlyonce_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Auto cancel notifs",
                        "ALERT_AUTOCANCEL",
                        R.string.alert_autocancel_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Tor warn override blackout",
                        "ALERT_BLACKOUT_TORNADO",
                        R.string.alert_blackout_tornado_label
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Notification check interval(m)",
                        "ALERT_NOTIFICATION_INTERVAL",
                        R.string.alert_interval_np_label,
                        12,
                        1,
                        120
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Notification blackout - PM(h)",
                        "ALERT_BLACKOUT_PM",
                        R.string.alert_blackout_pm_np_label,
                        22,
                        0,
                        23
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Notification blackout - AM(h)",
                        "ALERT_BLACKOUT_AM",
                        R.string.alert_blackout_am_np_label,
                        7,
                        0,
                        23
                ).card
        )
    }

    override fun onStop() {
        super.onStop()
        if (MyApplication.notifTts != Utility.readPref(
                        this,
                        "NOTIF_TTS",
                        "false"
                ).startsWith("t")
        ) {
            showFileWritePermsDialogue()
        }
        MyApplication.initPreferences(this)
        restartNotifs()
    }

    private fun restartNotifs() {
        UtilityWXJobService.startService(this)
        Utility.writePref(this, "RESTART_NOTIF", "false")
    }

    private fun notifSoundPicker() {
        var uri: Uri? = null
        if (MyApplication.notifSoundUri != "") uri = Uri.parse(MyApplication.notifSoundUri)
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
                MyApplication.notifSoundUri = uri.toString()
                Utility.writePref(this, "NOTIF_SOUND_URI", MyApplication.notifSoundUri)
            } else {
                MyApplication.notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                Utility.writePrefWithNull(this, "NOTIF_SOUND_URI", null)
            }
        }
    }

    private fun showWFONotificationFilterDialogue() {
        val items = listOf("Air Quality Alert", "Wind Advisory", "Lake Wind Advisory")
        val checkedItems = BooleanArray(items.size)
        val seletedItems = mutableListOf<Int>()
        val nwsWfoFilterStr = Utility.readPref(this, "NOTIF_WFO_FILTER", "")
        var valueInArray: Boolean
        items.indices.forEach { i ->
            valueInArray = MyApplication.colon.split(nwsWfoFilterStr).any { it == items[i] }
            if (valueInArray) {
                checkedItems[i] = true
                seletedItems.add(i)
            } else {
                checkedItems[i] = false
            }
        }
        val dialog = AlertDialog.Builder(this)
                .setTitle("Choose which Local NWS Alerts to not show:")
                .setMultiChoiceItems(
                        items.toTypedArray(),
                        checkedItems
                ) { _, indexSelected, isChecked ->
                    if (isChecked) {
                        seletedItems.add(indexSelected)
                    } else if (seletedItems.contains(indexSelected)) {
                        seletedItems.remove(Integer.valueOf(indexSelected))
                    }
                }.setPositiveButton("OK") { _, _ ->
                    var nwsWfoFilterStrLoc = ""
                    seletedItems.indices.forEach { nwsWfoFilterStrLoc += items[seletedItems[it]] + ":" }
                    Utility.writePref(this, "NOTIF_WFO_FILTER", nwsWfoFilterStrLoc)
                }.setNegativeButton("Cancel") { _, _ ->
                }.create()
        dialog.show()
    }

    private fun showFileWritePermsDialogue() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            fileWritePerm
                    )
                }
            }
        }
    }

    private val fileWritePerm = 5002

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            fileWritePerm -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }
}

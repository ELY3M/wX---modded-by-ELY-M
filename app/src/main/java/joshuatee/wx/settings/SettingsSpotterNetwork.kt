//modded by ELY M. 
//done by ELY M. 

package joshuatee.wx.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility


class SettingsSpotterNetwork : BaseActivity() {

    private lateinit var box: VBox
    lateinit var sn_key_edit: EditText

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_spotternetwork, null, false)
        setTitle("Spotter Network Settings", "Add your SN Key for Location Auto Report")
        box = VBox.fromResource(this)
        setupEditText()
        //box.addWidget(Switch(this, "Spotter Network Location Report", "SN_LOCATIONREPORT", R.string.sn_locationreport))
        addSwitchAutoReportLocation()
    }


    private fun addSwitchAutoReportLocation() {
        val autoGPSReport = Switch(this, "Spotter Network Location Report", "SN_LOCATIONREPORT", R.string.sn_locationreport)
        box.addWidget(autoGPSReport)
        autoGPSReport.setOnCheckedChangeListener { compoundButton, _ ->
            RadarPreferences.sn_locationreport = compoundButton.isChecked
            if (RadarPreferences.sn_locationreport != Utility.readPref(this, "SN_LOCATIONREPORT", "false").startsWith("t")) {
                backgroundLocationPerms()
            }
            if (compoundButton.isChecked) {
                Utility.writePref(this, "SN_LOCATIONREPORT", "true")
            } else {
                Utility.writePref(this, "SN_LOCATIONREPORT", "false")
            }
        }
    }

    private fun backgroundLocationPerms() {
        val backgroundLocationPermCode = 5002
        if(Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.i("wx-elys", "Background Location Perms are already granted :)")
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), backgroundLocationPermCode)
                    Log.i("wx-elys", "Asking for Background Location Perms")
                }
            }
        }
    }



    override fun onStop() {
        super.onStop()
        RadarPreferences.sn_key = sn_key_edit.text.toString()
        Utility.writePref(this, "SN_KEY", sn_key_edit.text.toString())
        Log.i("wx-elys", "sn key edit is: " + sn_key_edit.toString())
        MyApplication.initPreferences(this)
    }

    private fun setupEditText() {
        sn_key_edit = findViewById(R.id.sn_key)
        sn_key_edit.setText(RadarPreferences.sn_key)
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            sn_key_edit.setTextColor(Color.BLACK)
            sn_key_edit.setHintTextColor(Color.GRAY)

        }

    }



}

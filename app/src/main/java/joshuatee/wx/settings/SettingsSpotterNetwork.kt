//modded by ELY M. 
//done by ELY M. 

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility


class SettingsSpotterNetwork : BaseActivity() {


    lateinit var sn_key_edit: EditText


    var TAG = "spotternetworksettings"
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_spotternetwork, null, false)
        ObjectCard(this, R.id.cv_sn_label)
        setupEditText()

        val ll: LinearLayout = findViewById(R.id.sn_settings)
        ll.addView(ObjectSettingsCheckBox(this, "Spotter Network Location Report", "SN_LOCATIONREPORT", R.string.sn_locationreport).card)


    }

    override fun onStop() {
        super.onStop()
        MyApplication.sn_key = sn_key_edit.text.toString()
        Utility.writePref(this, "SN_KEY", sn_key_edit.text.toString())
        Log.i(TAG, "sn key edit is: " + sn_key_edit.toString())
        MyApplication.initPreferences(this)
    }

    private fun setupEditText() {
        sn_key_edit = findViewById(R.id.sn_key)
        sn_key_edit.setText(MyApplication.sn_key)
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            sn_key_edit.setTextColor(Color.BLACK)
            sn_key_edit.setHintTextColor(Color.GRAY)

        }

    }



}

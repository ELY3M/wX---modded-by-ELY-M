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

import joshuatee.wx.R
import joshuatee.wx.ui.ColorPicker
import joshuatee.wx.ui.ColorPicker.OnColorChangedListener
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.UtilityToolbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.graphics.Color
import joshuatee.wx.UIPreferences
import joshuatee.wx.util.Utility

import kotlinx.android.synthetic.main.activity_settings_color_picker.*

class SettingsColorPickerActivity : AppCompatActivity(), OnColorChangedListener {

    // Used to set a specific color for a radar/mosaic preference
    //
    // arg1: pref token
    // arg2: title
    //

    companion object {
        const val INFO: String = ""
    }

    private lateinit var picker: ColorPicker
    private var color = 0
    private var prefVal = ""
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB)
            setTheme(R.style.MyCustomTheme_NOAB)
        else
            setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_color_picker)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        UtilityToolbar.fullScreenMode(toolbar, false)
        val turl = intent.getStringArrayExtra(INFO)
        prefVal = turl[0]
        title = turl[1]
        color = UtilityColor.setColor(prefVal)
        val currentColor = Utility.readPref(this, prefVal, color)
        picker = findViewById(R.id.picker)
        buttonDefault.setTextColor(color)
        //buttonDefault.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        picker.oldCenterColor = currentColor
        picker.color = currentColor
        picker.addValueBar(vBar)
        picker.addSaturationBar(sBar)
        picker.setOnColorChangedListener(this)
        buttonDefault.setOnClickListener {
            picker.oldCenterColor = color
            Utility.writePref(this, prefVal, color)
            toolbar.subtitle = "(" + Color.red(color) + "," + Color.green(color) + "," +
                    Color.blue(color) + ")"
        }
        val currColorViaPref = Utility.readPref(this, prefVal, color)
        toolbar.subtitle = "(" + Color.red(currColorViaPref) + "," + Color.green(currColorViaPref) +
                "," + Color.blue(currColorViaPref) + ")"
    }

    override fun onColorChanged(color: Int) {
        toolbar.subtitle = "(" + Color.red(color) + "," + Color.green(color) + "," +
                Color.blue(color) + ")"
        picker.oldCenterColor = picker.color
        Utility.writePref(this, prefVal, picker.color)
    }

    override fun onStop() {
        MyApplication.initPreferences(this)
        super.onStop()
    }
}

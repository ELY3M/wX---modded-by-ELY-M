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

import joshuatee.wx.R
import joshuatee.wx.ui.ColorPicker.OnColorChangedListener
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.UtilityToolbar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.graphics.Color
import android.widget.Button
import joshuatee.wx.ui.ColorPicker
import joshuatee.wx.ui.SaturationBar
import joshuatee.wx.ui.ValueBar
import joshuatee.wx.util.Utility

class SettingsColorPickerActivity : AppCompatActivity(), OnColorChangedListener {

    // Used to set a specific color for a radar/mosaic preference
    //
    // arg1: pref token
    // arg2: title
    //

    companion object {
        const val INFO = ""
    }

    private var color = 0
    private var prefVal = ""
    private lateinit var toolbar: Toolbar
    private lateinit var colorPicker: ColorPicker
    private lateinit var buttonDefault: Button
    private lateinit var vBar: ValueBar
    private lateinit var sBar: SaturationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) setTheme(R.style.MyCustomTheme_NOAB) else setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_color_picker)
        colorPicker = findViewById(R.id.colorPicker)
        vBar = findViewById(R.id.vBar)
        sBar = findViewById(R.id.sBar)
        buttonDefault = findViewById(R.id.buttonDefault)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        UtilityToolbar.fullScreenMode(toolbar, false)
        val arguments = intent.getStringArrayExtra(INFO)!!
        prefVal = arguments[0]
        title = arguments[1]
        color = UtilityColor.setColor(prefVal)
        val currentColor = Utility.readPrefInt(this, prefVal, color)
        buttonDefault.setTextColor(color)
        colorPicker.oldCenterColor = currentColor
        colorPicker.color = currentColor
        colorPicker.addValueBar(vBar)
        colorPicker.addSaturationBar(sBar)
        colorPicker.setOnColorChangedListener(this)
        buttonDefault.setOnClickListener {
            colorPicker.oldCenterColor = color
            Utility.writePrefInt(this, prefVal, color)
            toolbar.subtitle = "(" + Color.red(color) + "," + Color.green(color) + "," + Color.blue(color) + ")"
        }
        val currColorViaPref = Utility.readPrefInt(this, prefVal, color)
        toolbar.subtitle = "(" + Color.red(currColorViaPref) + "," + Color.green(currColorViaPref) + "," + Color.blue(currColorViaPref) + ")"
    }

    override fun onColorChanged(color: Int) {
        toolbar.subtitle = "(" + Color.red(color) + "," + Color.green(color) + "," + Color.blue(color) + ")"
        colorPicker.oldCenterColor = colorPicker.color
        Utility.writePrefInt(this, prefVal, colorPicker.color)
    }

    override fun onStop() {
        MyApplication.initPreferences(this)
        super.onStop()
    }
}

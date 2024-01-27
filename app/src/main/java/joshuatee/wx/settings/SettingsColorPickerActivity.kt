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

package joshuatee.wx.settings

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import android.os.Bundle
import android.graphics.Color
import android.widget.Button
import joshuatee.wx.externalColorChooser.ColorPicker
import joshuatee.wx.externalColorChooser.ColorPicker.OnColorChangedListener
import joshuatee.wx.externalColorChooser.SaturationBar
import joshuatee.wx.externalColorChooser.ValueBar
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.Utility

// was inherited from AppCompatActivity
class SettingsColorPickerActivity : BaseActivity(), OnColorChangedListener {

    //
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
    private lateinit var colorPicker: ColorPicker
    private lateinit var buttonDefault: Button
    private lateinit var vBar: ValueBar
    private lateinit var sBar: SaturationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_color_picker, null, false)
        val arguments = intent.getStringArrayExtra(INFO)!!
        title = arguments[1]
        prefVal = arguments[0]
        setupUI()
    }

    private fun setupUI() {
        colorPicker = findViewById(R.id.colorPicker)
        vBar = findViewById(R.id.vBar)
        sBar = findViewById(R.id.sBar)
        buttonDefault = findViewById(R.id.buttonDefault)
        color = UtilityColor.setColor(prefVal)
        val currentColor = Utility.readPrefInt(this, prefVal, color)
        with(colorPicker) {
            oldCenterColor = currentColor
            this.color = currentColor
            addValueBar(vBar)
            addSaturationBar(sBar)
            setOnColorChangedListener(this@SettingsColorPickerActivity)
        }
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

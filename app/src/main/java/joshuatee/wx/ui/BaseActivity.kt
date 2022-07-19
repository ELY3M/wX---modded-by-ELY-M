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

package joshuatee.wx.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var toolbar: Toolbar
    protected lateinit var toolbarBottom: Toolbar

    protected fun onCreate(savedInstanceState: Bundle?, layoutResId: Int, menuResId: Int?, bottomToolbar: Boolean) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (bottomToolbar) {
            toolbarBottom = findViewById(R.id.toolbar_bottom)
            if (UIPreferences.iconsEvenSpaced) {
                UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, menuResId!!)
            } else {
                toolbarBottom.inflateMenu(menuResId!!)
            }
            toolbar.setOnClickListener { toolbarBottom.showOverflowMenu() }
            toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        } else {
            toolbarBottom = Toolbar(this as Context)
        }
        UtilityToolbar.fullScreenMode(toolbar, false)
    }
}

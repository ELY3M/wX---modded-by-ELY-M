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

package joshuatee.wx.spc

import android.os.Bundle
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes2
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

class SpcCompmapActivity : BaseActivity() {

    private var layerString = ""
    private lateinit var touchImage: TouchImage
    private lateinit var navDrawer: NavDrawer
    private val paramList = UtilitySpcCompmap.labels.toMutableList()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.shared_multigraphics, false)
        setupUI()
        setupLayerString()
        getContent()
    }

    private fun setupUI() {
        touchImage = TouchImage(this, R.id.iv)
        navDrawer = NavDrawer(this, paramList)
        navDrawer.connect(::navDrawerSelected)
        objectToolbar.connectClick { navDrawer.open() }
    }

    private fun navDrawerSelected(position: Int) {
        val positionStr = UtilitySpcCompmap.urlIndex[position]
        if (paramList[position].contains("(on)")) {
            paramList[position] = paramList[position].replace("\\(on\\) ".toRegex(), "")
            layerString = layerString.replace("a$positionStr:", "")
        } else {
            paramList[position] = "(on) " + paramList[position]
            layerString = layerString + "a" + positionStr + ":"
        }
        getContent()
        navDrawer.updateLists(paramList)
    }

    private fun setupLayerString() {
        layerString = Utility.readPref(this, "SPCCOMPMAP_LAYERSTR", "a7:a19:") // mslp, hpc fronts
        val items = layerString.split(":").dropLastWhile { it.isEmpty() }
        items.forEach {
            selectItemNoGet(To.int(it.replace("a", "")))
        }
    }

    private fun selectItemNoGet(positionF: Int) {
        var position = positionF
        for (i in (UtilitySpcCompmap.urlIndex.indices)) {
            if (position.toString() == UtilitySpcCompmap.urlIndex[i]) {
                position = i
                break
            }
        }
        navDrawer.setItemChecked(position)
        navDrawer.close()
        if (!paramList[position].contains("(on)")) {
            paramList[position] = "(on) " + paramList[position]
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureBytes2({ UtilitySpcCompmap.getImage(this, layerString) }, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        touchImage.set(bitmap)
        touchImage.firstRun("SPCCOMPMAP")
        Utility.writePref(this, "SPCCOMPMAP_LAYERSTR", layerString)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, "SPC Compmap", touchImage)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom("SPCCOMPMAP")
        super.onStop()
    }
}

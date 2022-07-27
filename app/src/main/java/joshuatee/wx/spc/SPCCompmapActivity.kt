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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcCompmapActivity : BaseActivity() {

    private var layerStr = ""
    private lateinit var image: TouchImage
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private val paramList = UtilitySpcCompmap.labels.toMutableList()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.shared_multigraphics, false)
        objectNavDrawer = ObjectNavDrawer(this, paramList)
        objectNavDrawer.setListener2 { _, _, position, _ ->
            objectNavDrawer.setItemChecked(position, false)
            objectNavDrawer.close()
            val positionStr = UtilitySpcCompmap.urlIndex[position]
            if (paramList[position].contains("(on)")) {
                paramList[position] = paramList[position].replace("\\(on\\) ".toRegex(), "")
                layerStr = layerStr.replace("a$positionStr:", "")
            } else {
                paramList[position] = "(on) " + paramList[position]
                layerStr = layerStr + "a" + positionStr + ":"
            }
            getContent()
        }
        toolbar.setOnClickListener { objectNavDrawer.open() }
        image = TouchImage(this, R.id.iv)
        layerStr = Utility.readPref(this, "SPCCOMPMAP_LAYERSTR", "a7:a19:") // mslp, hpc fronts
        setupLayerString()
        getContent()
    }

    private fun setupLayerString() {
        val items = layerStr.split(":").dropLastWhile { it.isEmpty() }
        items.forEach {
            selectItemNoGet(it.replace("a", "").toIntOrNull() ?: 0)
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
        objectNavDrawer.setItemChecked(position, false)
        objectNavDrawer.close()
        if (!paramList[position].contains("(on)")) {
            paramList[position] = "(on) " + paramList[position]
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, { bitmap = UtilitySpcCompmap.getImage(this@SpcCompmapActivity, layerStr) }, ::showImage)
    }

    private fun showImage() {
        image.setBitmap(bitmap)
        image.firstRunSetZoomPosn("SPCCOMPMAP")
        Utility.writePref(this, "SPCCOMPMAP_LAYERSTR", layerStr)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, "SPC Compmap", bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        image.imgSavePosnZoom("SPCCOMPMAP")
        super.onStop()
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SPCCompmapActivity : BaseActivity() {

    private var layerStr = ""
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var img: TouchImageView2
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var drw: ObjectNavDrawer
    private var paramList = mutableListOf<String>()
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, null, false)
        contextg = this
        paramList = UtilitySPCCompmap.MODEL_PARAMS_LABELS.toMutableList()
        drw = ObjectNavDrawer(this, paramList)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            val positionStr = UtilitySPCCompmap.URL_INDEX[position]
            if (paramList[position].contains("(on)")) {
                paramList[position] = paramList[position].replace("\\(on\\) ".toRegex(), "")
                layerStr = layerStr.replace("a$positionStr:", "")
                GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            } else {
                paramList[position] = "(on) " + paramList[position]
                layerStr = layerStr + "a" + positionStr + ":"
                GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        img = findViewById(R.id.iv)
        layerStr = Utility.readPref(this, "SPCCOMPMAP_LAYERSTR", "a7:a19:") // mslp, hpc fronts
        setupInitLayerString()
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun setupInitLayerString() {
        val tmpArr = MyApplication.colon.split(layerStr)
        tmpArr.forEach { selectItemNoGet(it.replace("a", "").toIntOrNull() ?: 0) }
    }

    private fun selectItemNoGet(positionF: Int) {
        var position = positionF
        for (i in (0 until UtilitySPCCompmap.URL_INDEX.size)) {
            if (position.toString() == UtilitySPCCompmap.URL_INDEX[i]) {
                position = i
                break
            }
        }
        drw.listView.setItemChecked(position, false)
        drw.drawerLayout.closeDrawer(drw.listView)
        if (!paramList[position].contains("(on)")) {
            paramList[position] = "(on) " + paramList[position]
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            bitmap = UtilitySPCCompmap.getImage(contextg, layerStr)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            img.setMaxZoom(4f)
            firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "SPCCOMPMAP")
            imageLoaded = true
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, "SPC Compmap", "", bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "SPCCOMPMAP")
            Utility.writePref(this, "SPCCOMPMAP_LAYERSTR", layerStr)
        }
        super.onStop()
    }
}

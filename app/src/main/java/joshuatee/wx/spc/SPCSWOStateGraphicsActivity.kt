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
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.STATE_ARR
import joshuatee.wx.util.Utility

class SPCSWOStateGraphicsActivity : BaseActivity(), OnClickListener, OnItemSelectedListener, OnMenuItemClickListener {

    // Show state level SPC SWO grapahics for D1-3
    //
    // Arguments
    // 1: day

    companion object {
        const val NO: String = ""
    }

    private var turlDay = ""
    private var imgUrl = ""
    private lateinit var img: TouchImageView2
    private var nws1StateCurrent = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstTime = true
    private var firstRun = false
    private var imageLoaded = false
    private val imgPrefToken = "SWO_STATE"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcswostate, R.menu.spcswostate, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        val turl = intent.getStringArrayExtra(NO)
        turlDay = turl[0]
        val nws1Current = Location.wfo
        nws1StateCurrent = Utility.readPref(this, "NWS_LOCATION_$nws1Current", "").split(",")[0]
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        val spinner1 = ObjectSpinner(this, this, R.id.spinner1, STATE_ARR, nws1StateCurrent)
        spinner1.setOnItemSelectedListener(this)
    }

    override fun onRestart() {
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        super.onRestart()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            title = "$nws1StateCurrent SWO D$turlDay"
            imgUrl = UtilitySPCSWO.getSWOStateURL(nws1StateCurrent, turlDay)
            //imgUrl = "http://www.spc.noaa.gov/public/state/images/" + nws1StateCurrent + "_swody" + turlDay + ".png"
        }

        override fun doInBackground(vararg params: String): String {
            bitmap = imgUrl.getImage()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.visibility = View.VISIBLE
            img.setImageBitmap(bitmap)
            //img.resetZoom()
            if (!firstRun) {
                img.setZoom(imgPrefToken)
                firstRun = true
            }
            imageLoaded = true
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(this, "$nws1StateCurrent SWO D$turlDay", bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
            firstTime = false
        }
        img.setMaxZoom(3.0f)
        nws1StateCurrent = MyApplication.colon.split(STATE_ARR[pos])[0]
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, imgPrefToken)
        }
        super.onStop()
    }
}





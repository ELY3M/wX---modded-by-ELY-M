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

import android.annotation.SuppressLint
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.MyApplication

import joshuatee.wx.Extensions.*
import joshuatee.wx.util.*

class SettingsLocationCanadaMapActivity : BaseActivity(), OnClickListener {

    // Use imagemaps to select locations for canada to save
    // arg1: province or territory
    //

    companion object {
        const val URL: String = ""
    }

    private var url = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_settings_location_canada_map,
            null,
            false
        )
        val activityArguments = intent.getStringArrayExtra(URL)
        url = activityArguments[0]
        title = url.toUpperCase()
        toolbar.subtitle = "Select a location and then use the back arrow to save."
        var imgRes = 0
        var imgMap = 0
        when (url) {
            "ab" -> {
                imgRes = R.drawable.ab_e
                imgMap = R.id.map_ab
            }
            "bc" -> {
                imgRes = R.drawable.bc_e
                imgMap = R.id.map_bc
            }
            "mb" -> {
                imgRes = R.drawable.mb_e
                imgMap = R.id.map_mb
            }
            "nb" -> {
                imgRes = R.drawable.nb_e
                imgMap = R.id.map_nb
            }
            "nl" -> {
                imgRes = R.drawable.nl_e
                imgMap = R.id.map_nl
            }
            "ns" -> {
                imgRes = R.drawable.ns_e
                imgMap = R.id.map_ns
            }
            "nt" -> {
                imgRes = R.drawable.nt_e
                imgMap = R.id.map_nt
            }
            "nu" -> {
                imgRes = R.drawable.nu_e
                imgMap = R.id.map_nu
            }
            "on" -> {
                imgRes = R.drawable.on_e
                imgMap = R.id.map_on
            }
            "pe" -> {
                imgRes = R.drawable.pe_e
                imgMap = R.id.map_pe
            }
            "qc" -> {
                imgRes = R.drawable.qc_e
                imgMap = R.id.map_qc
            }
            "sk" -> {
                imgRes = R.drawable.sk_e
                imgMap = R.id.map_sk
            }
            "yt" -> {
                imgRes = R.drawable.yt_e
                imgMap = R.id.map_yt
            }
        }
        hideAllMaps()
        val bm = UtilityImg.loadBitmap(this, imgRes, false)
        val map: ImageMap = findViewById(imgMap)
        map.visibility = View.VISIBLE
        map.setImageBitmap(bm)
        val paramsIv = map.layoutParams
        paramsIv.width = MyApplication.dm.widthPixels
        paramsIv.height = MyApplication.dm.widthPixels * bm.height / bm.width
        map.layoutParams = paramsIv
        map.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                mapClicked(id)
            }

            override fun onBubbleClicked(id: Int) {}
        })
    }

    private fun mapClicked(id: Int) {
        val sector = UtilityImageMap.canadaMap(id)
        val cityLoc = getCityFromXml(sector)
        Utility.writePref(this, "LOCATION_CANADA_PROV", url.toUpperCase())
        Utility.writePref(this, "LOCATION_CANADA_CITY", cityLoc)
        Utility.writePref(
            this,
            "LOCATION_CANADA_ID",
            sector.split("_".toRegex()).dropLastWhile { it.isEmpty() }[1]
        )
        toolbar.subtitle = url.toUpperCase() + ", " + cityLoc
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar)
        }
    }

    private fun hideAllMaps() {
        listOf(
            R.id.map_ab,
            R.id.map_bc,
            R.id.map_mb,
            R.id.map_nl,
            R.id.map_ns,
            R.id.map_nt,
            R.id.map_nu,
            R.id.map_on,
            R.id.map_pe,
            R.id.map_qc,
            R.id.map_sk,
            R.id.map_yt
        ).forEach {
            val map: ImageMap = findViewById(it)
            map.visibility = View.GONE
        }
    }

    private fun getCityFromXml(token: String): String {
        val io: InputStream?
        try {
            io = resources.openRawResource(R.raw.maps)
            // if file the available for reading
            if (io != null) {
                // prepare the file for reading
                val inputReader = InputStreamReader(io)
                val buffReader = BufferedReader(inputReader)
                var line: String?
                // read every line of the file into the line-variable, on line at the time
                do {
                    line = buffReader.readLine()
                    if (line!!.contains(token)) {
                        return line.parse("title=\"(.*?)\"")
                    }
                    // do something with the line
                } while (line != null)
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return ""
    }
}

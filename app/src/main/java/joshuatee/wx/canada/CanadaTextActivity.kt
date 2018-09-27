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

package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.ui.ObjectCALegal
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString

class CanadaTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private var prod = "focn45"
    private var description = "Significant Weather Discussion, PASPC"
    private var sigHtmlTmp = ""
    private lateinit var sv: ScrollView
    private lateinit var c0: ObjectCardText
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.canada_text)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        sv = findViewById(R.id.sv)
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        c0 = ObjectCardText(this)
        linearLayout.addView(c0.card)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        linearLayout.addView(ObjectCALegal(this, "").card)
        prod = Utility.readPref(this, "CA_TEXT_LASTUSED", prod)
        description = Utility.readPref(this, "CA_TEXT_LASTUSED_TITLE", description)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            title = description
            sv.smoothScrollTo(0, 0)
        }

        override fun doInBackground(vararg params: String): String {
            sigHtmlTmp = if (prod != "https://weather.gc.ca/forecast/public_bulletins_e.html?Bulletin=fpcn48.cwao") {
                UtilityDownload.getTextProduct(contextg, prod)
            } else {
                UtilityString.getHTMLandParseSep(prod, "<pre>(.*?)</pre>")
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            c0.setTextAndTranslate(Utility.fromHtml(sigHtmlTmp))
            Utility.writePref(contextg, "CA_TEXT_LASTUSED", prod)
            Utility.writePref(contextg, "CA_TEXT_LASTUSED_TITLE", description)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, sigHtmlTmp, prod, prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                UtilityShare.shareText(this, description, Utility.fromHtml(sigHtmlTmp))
                return true
            }
            R.id.action_focn45 -> setProdAndDescription("focn45", "Significant Weather Discussion, PASPC")
            R.id.action_fxcn01 -> setProdAndDescription("fxcn01", "FXCN01")
            R.id.action_uv -> setProdAndDescription("fpcn48", "FPCN48")
            R.id.action_s_mb -> setProdAndDescription("awcn11", "Weather Summary S. Manitoba")
            R.id.action_n_mb -> setProdAndDescription("awcn12", "Weather Summary N. Manitoba")
            R.id.action_s_sk -> setProdAndDescription("awcn13", "Weather Summary S. Saskatchewan")
            R.id.action_n_sk -> setProdAndDescription("awcn14", "Weather Summary N. Saskatchewan")
            R.id.action_s_ab -> setProdAndDescription("awcn15", "Weather Summary S. Alberta")
            R.id.action_n_ab -> setProdAndDescription("awcn16", "Weather Summary N. Alberta")
            else -> return super.onOptionsItemSelected(item)
        }
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        return true
    }

    private fun setProdAndDescription(prod: String, description: String) {
        this.prod = prod
        this.description = description
    }
}


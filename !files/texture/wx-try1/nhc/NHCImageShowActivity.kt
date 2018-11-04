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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare

class NHCImageShowActivity : VideoRecordActivity(), OnClickListener, Toolbar.OnMenuItemClickListener {

    // Activity to show GOES tropic floaters as called from NHCStorm
    // Supports animation and different products
    //

    companion object {
        const val URL: String = ""
    }

    private var animDrawable = AnimationDrawable()
    private lateinit var img: TouchImageView2
    private var animRan = false
    private var toolbarTitle = ""
    private var rid = ""
    private var prod = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_nhc_image_show, R.menu.nhc_image_show_activity, true, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        val turl = intent.getStringArrayExtra(URL)
        rid = turl[0]
        prod = turl[1]
        toolbarTitle = turl[2]
        val titleArr = toolbarTitle.split(" - ")
        if (titleArr.size > 1) {
            title = titleArr[1]
        }
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            bitmap = UtilityNHC.getImage(rid, prod)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            animRan = false
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AnimateRadar : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            animDrawable = UtilityNHC.getAnim(contextg, rid, prod, params[0])
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            animRan = UtilityImgAnim.startAnimation(animDrawable, img)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan)
                        UtilityShare.shareAnimGif(this, toolbarTitle, animDrawable)
                    else
                        UtilityShare.shareBitmap(this, toolbarTitle, bitmap)
                }
            }
            R.id.action_a6 -> AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "6", "")
            R.id.action_a12 -> AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "12", "")
            R.id.action_a15 -> AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "15", "")
            R.id.action_vis -> prod = "vis"
            R.id.action_avn -> prod = "avn"
            R.id.action_bd -> prod = "bd"
            R.id.action_ir -> prod = "ir"
            R.id.action_swir -> prod = "swir"
            R.id.action_jsl -> prod = "jsl"
            R.id.action_rgb -> prod = "rgb"
            R.id.action_ft -> prod = "ft"
            R.id.action_rb -> prod = "rb"
            R.id.action_wv -> prod = "wv"
            else -> return super.onOptionsItemSelected(item)
        }
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }
}

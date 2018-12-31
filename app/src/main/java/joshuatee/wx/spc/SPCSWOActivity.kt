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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.widget.ScrollView

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

class SPCSWOActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // show SWO for Day X as specified in extra
    //

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var html = ""
    private var bitmaps = listOf<Bitmap>()
    private lateinit var activityArguments: Array<String>
    private var turlDay = ""
    private var playlistProd = ""
    private lateinit var c1: ObjectCardImage
    private lateinit var c2: ObjectCardText
    private lateinit var c3: ObjectCardImage
    private lateinit var c4: ObjectCardImage
    private lateinit var c5: ObjectCardImage
    private lateinit var c6: ObjectCardImage
    private lateinit var scrollView: ScrollView
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.spcswo
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        scrollView = findViewById(R.id.sv)
        val ll: LinearLayout = findViewById(R.id.ll)
        c1 = ObjectCardImage(this, ll)
        c2 = ObjectCardText(this, ll, toolbar, toolbarBottom)
        c3 = ObjectCardImage(this, ll)
        c4 = ObjectCardImage(this, ll)
        c5 = ObjectCardImage(this, ll)
        c6 = ObjectCardImage(this, ll)
        activityArguments = intent.getStringArrayExtra(NO)
        turlDay = activityArguments[0]
        title = "Day $turlDay Convective Outlook"
        val menu = toolbarBottom.menu
        val miTornado = menu.findItem(R.id.action_share_tornado)
        val miHail = menu.findItem(R.id.action_share_hail)
        val miWind = menu.findItem(R.id.action_share_wind)
        val miCategorical = menu.findItem(R.id.action_share_categorical)
        val miProbabilistic = menu.findItem(R.id.action_share_probabilistic)
        val miDay4Img = menu.findItem(R.id.action_share_d4)
        val miDay5Img = menu.findItem(R.id.action_share_d5)
        val miDay6Img = menu.findItem(R.id.action_share_d6)
        val miDay7Img = menu.findItem(R.id.action_share_d7)
        val miDay8Img = menu.findItem(R.id.action_share_d8)
        miDay4Img.isVisible = false
        miDay5Img.isVisible = false
        miDay6Img.isVisible = false
        miDay7Img.isVisible = false
        miDay8Img.isVisible = false
        if (turlDay == "1") {
            miProbabilistic.isVisible = false
        } else {
            miTornado.isVisible = false
            miHail.isVisible = false
            miWind.isVisible = false
        }
        if (turlDay == "4-8") {
            playlistProd = "swod48"
            miProbabilistic.isVisible = false
            miCategorical.isVisible = false
        } else {
            playlistProd = "swody$turlDay"
        }
        if (turlDay == "4-8") {
            val state = menu.findItem(R.id.action_state_graphics)
            state.isVisible = false
            miDay4Img.isVisible = true
            miDay5Img.isVisible = true
            miDay6Img.isVisible = true
            miDay7Img.isVisible = true
            miDay8Img.isVisible = true
        }
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var textUrl = "SWODY$turlDay"
        if (turlDay == "4-8") {
            textUrl = "SWOD48"
        }
        withContext(Dispatchers.IO) {
            html = UtilityDownload.getTextProduct(contextg, textUrl)
            bitmaps = UtilitySPCSWO.getImageURLs(turlDay, true)
        }
        c2.setText(Utility.fromHtml(html))
        toolbar.subtitle = html.parse("(Valid.*?)<")
        if (activityArguments[1] == "sound") {
            UtilityTTS.synthesizeTextAndPlay(applicationContext, html, "spcswo")
        }
        when (turlDay) {
            "1" -> {
                c1.setImage(bitmaps[0])
                c3.setImage(bitmaps[1])
                c4.setImage(bitmaps[2])
                c5.setImage(bitmaps[3])
                c6.setVisibility(View.GONE)
                listOf(
                    c3,
                    c4,
                    c5
                ).forEach { card ->
                    card.setOnClickListener(View.OnClickListener {
                        scrollView.smoothScrollTo(
                            0,
                            0
                        )
                    })
                }
            }
            "2" -> {
                c1.setImage(bitmaps[0])
                c3.setImage(bitmaps[1])
                c4.setVisibility(View.GONE)
                c5.setVisibility(View.GONE)
                c6.setVisibility(View.GONE)
                listOf(c3).forEach { card ->
                    card.setOnClickListener(View.OnClickListener {
                        scrollView.smoothScrollTo(
                            0,
                            0
                        )
                    })
                }
            }
            "3" -> {
                c1.setImage(bitmaps[0])
                c3.setImage(bitmaps[1])
                c4.setVisibility(View.GONE)
                c5.setVisibility(View.GONE)
                c6.setVisibility(View.GONE)
                listOf(c3).forEach { card ->
                    card.setOnClickListener(View.OnClickListener {
                        scrollView.smoothScrollTo(
                            0,
                            0
                        )
                    })
                }
            }
            "4-8" -> {
                c1.setImage(bitmaps[0])
                c3.setImage(bitmaps[1])
                c4.setImage(bitmaps[2])
                c5.setImage(bitmaps[3])
                c6.setImage(bitmaps[4])
                listOf(
                    c3,
                    c4,
                    c5,
                    c6
                ).forEach { card ->
                    card.setOnClickListener(View.OnClickListener {
                        scrollView.smoothScrollTo(
                            0,
                            0
                        )
                    })
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, playlistProd, playlistProd)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareText(
                this,
                "Day $turlDay Convective Outlook",
                Utility.fromHtml(html),
                bitmaps
            )
            R.id.action_share_text -> UtilityShare.shareText(
                this,
                "Day $turlDay Convective Outlook - Text",
                Utility.fromHtml(html)
            )
            R.id.action_share_tornado -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                this,
                "Day $turlDay Convective Outlook - Tornado",
                bitmaps[1]
            )
            R.id.action_share_hail -> if (bitmaps.size > 2) UtilityShare.shareBitmap(
                this,
                "Day $turlDay Convective Outlook - Hail",
                bitmaps[2]
            )
            R.id.action_share_wind -> if (bitmaps.size > 3) UtilityShare.shareBitmap(
                this,
                "Day $turlDay Convective Outlook - Wind",
                bitmaps[3]
            )
            R.id.action_share_categorical -> if (bitmaps.isNotEmpty()) UtilityShare.shareBitmap(
                this,
                "Day $turlDay Convective Outlook - Categorical",
                bitmaps[0]
            )
            R.id.action_share_probabilistic -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                this,
                "Day $turlDay Convective Outlook - Probabilistic",
                bitmaps[1]
            )
            R.id.action_share_d4 -> if (bitmaps.isNotEmpty()) UtilityShare.shareBitmap(
                this,
                "Day " + "4" + " Convective Outlook - Image",
                bitmaps[0]
            )
            R.id.action_share_d5 -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                this,
                "Day " + "5" + " Convective Outlook - Image",
                bitmaps[1]
            )
            R.id.action_share_d6 -> if (bitmaps.size > 2) UtilityShare.shareBitmap(
                this,
                "Day " + "6" + " Convective Outlook - Image",
                bitmaps[2]
            )
            R.id.action_share_d7 -> if (bitmaps.size > 3) UtilityShare.shareBitmap(
                this,
                "Day " + "7" + " Convective Outlook - Image",
                bitmaps[3]
            )
            R.id.action_share_d8 -> if (bitmaps.size > 4) UtilityShare.shareBitmap(
                this,
                "Day " + "8" + " Convective Outlook - Image",
                bitmaps[4]
            )
            R.id.action_state_graphics -> ObjectIntent(
                this,
                SPCSWOStateGraphicsActivity::class.java,
                SPCSWOStateGraphicsActivity.NO,
                arrayOf(turlDay, "")
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

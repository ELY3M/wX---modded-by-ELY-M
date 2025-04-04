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

package joshuatee.wx.audio

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.ObjectToolbar
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI

abstract class AudioPlayActivity : AppCompatActivity() {

    //
    // In the child activity need to "extends AudioPlayActivity"
    // instead of "extends AppCompatActivity"
    //

    protected lateinit var toolbar: Toolbar
    protected lateinit var toolbarBottom: Toolbar
    protected lateinit var objectToolbar: ObjectToolbar
    protected lateinit var objectToolbarBottom: ObjectToolbar
    private lateinit var pause: MenuItem
    private var ttsProd = ""
    private var ttsTxt = ""
    private lateinit var view: View
    private var pausePressedIcon = 0
    protected var tabletInLandscape = false

    protected fun onCreate(savedInstanceState: Bundle?, layoutResId: Int, menuResId: Int) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        view = findViewById(android.R.id.content)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBottom = findViewById(R.id.toolbar_bottom)
        objectToolbar = ObjectToolbar(toolbar)
        objectToolbarBottom = ObjectToolbar(toolbarBottom)
        toolbarBottom.inflateMenu(menuResId)
        toolbar.setOnClickListener { toolbarBottom.showOverflowMenu() }
        toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        UtilityToolbar.setElevation(toolbar)
        pausePressedIcon = if (UtilityUI.isThemeAllWhite()) {
            GlobalVariables.ICON_PAUSE_PRESSED_BLUE
        } else {
            GlobalVariables.ICON_PAUSE_PRESSED
        }
        initBottomToolbar()
        UtilityTts.initTts(this)
        tabletInLandscape = UtilityUI.isTablet() && UtilityUI.isLandScape(this)
    }

    fun setTitle(s: String) {
        title = s
    }

    fun setTitle(s: String, sub: String) {
        title = s
        toolbar.subtitle = sub
    }

    private fun initBottomToolbar() {
        pause = objectToolbarBottom.find(R.id.action_stop)
        val playlist = objectToolbarBottom.find(R.id.action_playlist)
        playlist.setIcon(R.drawable.ic_playlist_add_24dp)
        if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
            pause.setIcon(pausePressedIcon)
        } else {
            pause.setIcon(GlobalVariables.ICON_PAUSE)
        }
    }

    protected fun audioPlayMenu(
        item: Int,
        txt: String,
        prod: String,
        playlistToken: String
    ): Boolean {
        ttsProd = prod
        ttsTxt = txt
        when (item) {
            R.id.action_read_aloud -> {
                UtilityTts.synthesizeTextAndPlay(this, txt, prod)
                pause.setIcon(GlobalVariables.ICON_PAUSE)
            }

            R.id.action_stop -> {
                if (UtilityTts.mediaPlayer != null) {
                    UtilityTts.playMediaPlayer()
                }
                if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
                    pause.setIcon(pausePressedIcon)
                } else {
                    pause.setIcon(GlobalVariables.ICON_PAUSE)
                }
            }

            R.id.action_playlist -> UtilityPlayList.add(this, view, playlistToken, txt)
            else -> return false
        }
        return true
    }

    override fun onRestart() {
        if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
            pause.setIcon(pausePressedIcon)
        } else {
            pause.setIcon(GlobalVariables.ICON_PAUSE)
        }
        super.onRestart()
    }

    override fun onDestroy() {
        UtilityTts.shutdownTts()
        super.onDestroy()
    }
}

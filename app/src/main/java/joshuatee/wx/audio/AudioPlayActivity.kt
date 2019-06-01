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

package joshuatee.wx.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.ui.UtilityToolbar

abstract class AudioPlayActivity : AppCompatActivity() {

    // In the child activity need to "extends AudioPlayActivity"
    // instead of "extends AppCompatActivity"
    //

    protected lateinit var toolbar: Toolbar
    protected lateinit var toolbarBottom: Toolbar
    private lateinit var pause: MenuItem
    private var ttsProd = ""
    private var ttsTxt = ""
    private lateinit var view: View

    protected fun onCreate(savedInstanceState: Bundle?, layoutResId: Int, menuResId: Int) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        view = findViewById(android.R.id.content)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBottom = findViewById(R.id.toolbar_bottom)
        if (MyApplication.iconsEvenSpaced) {
            UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, menuResId)
        } else {
            toolbarBottom.inflateMenu(menuResId)
        }
        toolbar.setOnClickListener { toolbarBottom.showOverflowMenu() }
        toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        UtilityToolbar.fullScreenMode(toolbar, false)
        initBottomToolbar()
    }

    private fun initBottomToolbar() {
        val menu = toolbarBottom.menu
        pause = menu.findItem(R.id.action_stop)
        val playlist = menu.findItem(R.id.action_playlist)
        playlist.let { playlist.setIcon(R.drawable.ic_playlist_add_24dp) }
        if (UtilityTts.mMediaPlayer != null && !UtilityTts.mMediaPlayer!!.isPlaying)
            pause.setIcon(MyApplication.ICON_PAUSE_PRESSED)
        else
            pause.setIcon(MyApplication.ICON_PAUSE)
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
                if (isStoragePermissionGranted) {
                    UtilityTts.synthesizeTextAndPlay(applicationContext, txt, prod)
                    pause.setIcon(MyApplication.ICON_PAUSE)
                    if (UIPreferences.mediaControlNotif)
                        UtilityNotification.createMediaControlNotification(applicationContext, "")
                }
            }
            R.id.action_stop -> {
                if (UtilityTts.mMediaPlayer != null)
                    UtilityTts.playMediaPlayer(1)
                if (UtilityTts.mMediaPlayer != null && !UtilityTts.mMediaPlayer!!.isPlaying)
                    pause.setIcon(MyApplication.ICON_PAUSE_PRESSED)
                else
                    pause.setIcon(MyApplication.ICON_PAUSE)
                if (UtilityTts.mMediaPlayer != null && UtilityTts.mMediaPlayer!!.isPlaying)
                    if (UIPreferences.mediaControlNotif)
                        UtilityNotification.createMediaControlNotification(applicationContext, "")
            }
            R.id.action_playlist -> UtilityPlayList.add(this, view, playlistToken, txt)
            else -> return false
        }
        return true
    }

    override fun onRestart() {
        if (UtilityTts.mMediaPlayer != null && !UtilityTts.mMediaPlayer!!.isPlaying)
            pause.setIcon(MyApplication.ICON_PAUSE_PRESSED)
        else
            pause.setIcon(MyApplication.ICON_PAUSE)
        super.onRestart()
    }

    private val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    true
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                    false
                }
            } else {
                true
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UtilityTts.synthesizeTextAndPlay(applicationContext, ttsTxt, ttsProd)
                    pause.setIcon(MyApplication.ICON_PAUSE)
                    if (UIPreferences.mediaControlNotif)
                        UtilityNotification.createMediaControlNotification(applicationContext, "")

                }
            }
        }
    }
}



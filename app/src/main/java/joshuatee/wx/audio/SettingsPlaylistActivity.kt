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

package joshuatee.wx.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.ActionMode
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.UtilityUI

import joshuatee.wx.WFO_ARR
import joshuatee.wx.NWS_TXT_ARR
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility

class SettingsPlaylistActivity : BaseActivity(), OnMenuItemClickListener {

    private val ridArr = mutableListOf<String>()
    private var ridFav = ""
    private val prefToken = "PLAYLIST"
    private var actionMode = ActionMode.PLAY
    private lateinit var ca: PlayListAdapter
    private lateinit var fabPause: ObjectFab
    private lateinit var diaMain: ObjectDialogue
    private lateinit var diaAfd: ObjectDialogue
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_playlist,
            R.menu.settings_playlist,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        ObjectFab(this, this, R.id.fab, View.OnClickListener { playAll() })
        fabPause = ObjectFab(this, this, R.id.fab3, View.OnClickListener { playItemFAB() })
        if (UtilityTTS.mMediaPlayer != null && !UtilityTTS.mMediaPlayer!!.isPlaying) {
            fabPause.fabSetResDrawable(contextg, MyApplication.ICON_PAUSE_PRESSED)
        } else {
            fabPause.fabSetResDrawable(contextg, MyApplication.ICON_PAUSE)
        }
        ObjectFab(
            this,
            this,
            R.id.fab1,
            MyApplication.ICON_ARROW_UP,
            View.OnClickListener { toggleMode(ActionMode.UP) })
        ObjectFab(
            this,
            this,
            R.id.fab2,
            MyApplication.ICON_ARROW_DOWN,
            View.OnClickListener { toggleMode(ActionMode.DOWN) })
        diaAfd = ObjectDialogue(this, "Select fixed location AFD products:", WFO_ARR)
        diaAfd.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            val strName = diaAfd.getItem(which)
            ridFav = ridFav + ":" + "AFD" +
                    strName.split(":").dropLastWhile { it.isEmpty() }[0].toUpperCase()
            Utility.writePref(this, prefToken, ridFav)
            MyApplication.playlistStr = ridFav
            ridArr.add(getLongString("AFD" + strName.split(":").dropLastWhile { it.isEmpty() }[0].toUpperCase()))
            ca.notifyDataSetChanged()
        })
        diaMain = ObjectDialogue(this, "Select text products:", NWS_TXT_ARR)
        diaMain.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            val strName = diaMain.getItem(which)
            ridFav = ridFav + ":" +
                    strName.split(":").dropLastWhile { it.isEmpty() }[0].toUpperCase()
            Utility.writePref(this, prefToken, ridFav)
            ridArr.add(getLongString(strName.split(":").dropLastWhile { it.isEmpty() }[0].toUpperCase()))
            ca.notifyDataSetChanged()
            MyApplication.playlistStr = ridFav
        })
        toolbar.subtitle = actionMode.getDescription()
        ridFav = Utility.readPref(this, prefToken, "")
        updateList()
        val recyclerView: RecyclerView = findViewById(R.id.card_list)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        ca = PlayListAdapter(ridArr)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : PlayListAdapter.MyClickListener {
            override fun onItemClick(position: Int) {
                prodClicked(position)
            }
        })
    }

    private fun updateList() {
        MyApplication.playlistStr = ridFav
        val ridArrtmp = MyApplication.colon.split(ridFav)
        ridArr.clear()
        (1 until ridArrtmp.size).mapTo(ridArr) { getLongString(ridArrtmp[it]) }
    }

    private fun updateListNoInit() {
        MyApplication.playlistStr = ridFav
        val ridArrtmp = MyApplication.colon.split(ridFav)
        (1 until ridArrtmp.size).forEach { ridArr[it - 1] = getLongString(ridArrtmp[it]) }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_downloadall -> ObjectIntent(
                this,
                DownloadPlaylistService::class.java,
                DownloadPlaylistService.URL,
                "false",
                true
            )
            R.id.action_delete -> toggleMode(ActionMode.DELETE)
            R.id.action_text -> toggleMode(ActionMode.TEXT)
            R.id.action_autodownload -> ObjectIntent(
                this,
                SettingsPlaylistAutodownloadActivity::class.java
            )
            R.id.action_add -> diaMain.show()
            R.id.action_afd -> diaAfd.show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        updateListNoInit()
        ca.notifyDataSetChanged()
        super.onRestart()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            updateListNoInit()
            ca.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(onBroadcast, IntentFilter("playlistdownloaded"))
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private fun getLongString(code: String) = "$code;" + Utility.readPref(
        contextg,
        "PLAYLIST_" + code + "_TIME",
        "unknown"
    ) + "  (size: " + Utility.readPref(contextg, "PLAYLIST_$code", "").length + ")"

    private val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    UtilityTTS.synthesizeTextAndPlayPlaylist(contextg, 1)
            }
        }
    }

    private fun toggleMode(am: ActionMode) {
        actionMode = if (actionMode == am) {
            ActionMode.PLAY
        } else {
            am
        }
        toolbar.subtitle = actionMode.getDescription()
    }

    private fun playItemFAB() {
        if (UtilityTTS.mMediaPlayer != null) {
            UtilityTTS.playMediaPlayer(1)
        }
        if (UtilityTTS.mMediaPlayer != null && !UtilityTTS.mMediaPlayer!!.isPlaying) {
            fabPause.fabSetResDrawable(contextg, MyApplication.ICON_PAUSE_PRESSED)
        } else {
            fabPause.fabSetResDrawable(contextg, MyApplication.ICON_PAUSE)
        }
        if (UtilityTTS.mMediaPlayer != null && UtilityTTS.mMediaPlayer!!.isPlaying) {
            if (UIPreferences.mediaControlNotif) {
                UtilityNotification.createMediaControlNotif(applicationContext, "")
            }
        }
    }

    private fun playAll() {
        fabPause.fabSetResDrawable(contextg, MyApplication.ICON_PAUSE)
        if (isStoragePermissionGranted) {
            UtilityTTS.synthesizeTextAndPlayPlaylist(contextg, 1)
        }
        if (UtilityTTS.mMediaPlayer != null && UtilityTTS.mMediaPlayer!!.isPlaying) {
            if (UIPreferences.mediaControlNotif) {
                UtilityNotification.createMediaControlNotif(applicationContext, "")
            }
        }
    }

    private fun prodClicked(position: Int) {
        when (actionMode) {
            ActionMode.DELETE -> {
                ridFav = Utility.readPref(this, prefToken, "")
                ridFav =
                        ridFav.replace(":" + MyApplication.semicolon.split(ridArr[position])[0], "")
                Utility.writePref(this, prefToken, ridFav)
                Utility.removePref(
                    this,
                    "PLAYLIST_" + MyApplication.semicolon.split(ridArr[position])[0]
                )
                ca.deleteItem(position)
                MyApplication.playlistStr = ridFav
            }
            ActionMode.UP -> {
                MyApplication.playlistStr = UtilityUI.moveUp(this, prefToken, ridArr, position)
                ca.notifyDataSetChanged()
            }
            ActionMode.DOWN -> {
                MyApplication.playlistStr = UtilityUI.moveDown(this, prefToken, ridArr, position)
                ca.notifyDataSetChanged()
            }
            ActionMode.TEXT -> ObjectIntent(
                contextg,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(
                    Utility.readPref(
                        contextg,
                        "PLAYLIST_" + MyApplication.semicolon.split(ridArr[position])[0],
                        ""
                    ), ridArr[position]
                )
            )
            else -> UtilityTTS.synthesizeTextAndPlayPlaylist(contextg, position + 1)
        }
    }
} 

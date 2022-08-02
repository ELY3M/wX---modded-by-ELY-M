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

package joshuatee.wx.audio

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.app.ActivityCompat
import java.util.Locale
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.BottomSheetFragment
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.UtilityWpcText
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.*

class SettingsPlaylistActivity : BaseActivity(), OnMenuItemClickListener {

    private var playListItems = mutableListOf<String>()
    private var ridFav = ""
    private val prefToken = "PLAYLIST"
    private lateinit var adapter: PlayListAdapter
    private lateinit var fabPause: ObjectFab
    private lateinit var diaMain: ObjectDialogue
    private lateinit var diaAfd: ObjectDialogue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_playlist, R.menu.settings_playlist, true)
        setTitle("PlayList", "Tap item to play, view, delete or move.")
        toolbarBottom.setOnMenuItemClickListener(this)
        ObjectFab(this, R.id.fab) { playAll() }
        fabPause = ObjectFab(this, R.id.fab3) { playItemFab() }
        val icon = if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
            GlobalVariables.ICON_PAUSE_PRESSED
        } else {
            GlobalVariables.ICON_PAUSE_WHITE
        }
        fabPause.setResDrawable(icon)
        diaAfd = ObjectDialogue(this, "Select fixed location AFD products:", GlobalArrays.wfos)
        diaAfd.setSingleChoiceItems { dialog, which ->
            val name = diaAfd.getItem(which)
            val product = "AFD" + name.split(":").dropLastWhile { it.isEmpty() }[0].uppercase(Locale.US)
            if (!ridFav.contains(product)) {
                ridFav = "$ridFav:$product"
                Utility.writePref(this, prefToken, ridFav)
                UIPreferences.playlistStr = ridFav
                playListItems.add(getLongString(product))
                getContent()
                dialog.dismiss()
            } else {
                dialog.dismiss()
                val rootView: View = (this as Activity).window.decorView.findViewById(android.R.id.content)
                ObjectPopupMessage(rootView, "$product already in playlist")
            }
        }
        diaMain = ObjectDialogue(this, "Select text products:", UtilityWpcText.labels)
        diaMain.setSingleChoiceItems { dialog, which ->
            val name = diaMain.getItem(which)
            val product = name.split(":").dropLastWhile { it.isEmpty() }[0].uppercase(Locale.US)
            if (!ridFav.contains(product)) {
                ridFav = "$ridFav:$product"
                Utility.writePref(this, prefToken, ridFav)
                playListItems.add(getLongString(product))
                UIPreferences.playlistStr = ridFav
                getContent()
                dialog.dismiss()
            } else {
                dialog.dismiss()
                val rootView: View = (this as Activity).window.decorView.findViewById(android.R.id.content)
                ObjectPopupMessage(rootView, "$product already in playlist")
            }
        }
        ridFav = Utility.readPref(this, prefToken, "")
        updateList()
        val recyclerView = ObjectRecyclerViewGeneric(this, R.id.card_list)
        adapter = PlayListAdapter(playListItems)
        recyclerView.adapter = adapter
        adapter.setListener(::itemSelected)
        UtilityTts.initTts(this)
        getContent()
    }

    private fun getContent() {
        FutureVoid(this,
                { UtilityPlayList.downloadAll(this) })
                { updateListNoInit(); adapter.notifyDataSetChanged() }
    }

    private fun updateList() {
        UIPreferences.playlistStr = ridFav
        val tempList = ridFav.split(":")
        playListItems = (1 until tempList.size).map { getLongString(tempList[it]) }.toMutableList()
    }

    private fun updateListNoInit() {
        UIPreferences.playlistStr = ridFav
        val tempList = ridFav.split(":")
        (1 until tempList.size).forEach {
            playListItems[it - 1] = getLongString(tempList[it])
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> diaMain.show()
            R.id.action_afd -> diaAfd.show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getLongString(code: String) = "$code;" + Utility.readPref(
            this,
            "PLAYLIST_" + code + "_TIME",
            "unknown"
    ) + "  (size: " + Utility.readPref(this, "PLAYLIST_$code", "").length + ")"

    private val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    true
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    false
                }
            } else {
                true
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    UtilityTts.synthesizeTextAndPlayPlaylist(this, 1)
            }
        }
    }

    private fun playItemFab() {
        if (UtilityTts.mediaPlayer != null) {
            UtilityTts.playMediaPlayer(1)
        }
        val icon = if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
            GlobalVariables.ICON_PAUSE_PRESSED
        } else {
            GlobalVariables.ICON_PAUSE
        }
        fabPause.setResDrawable(icon)
        if (UtilityTts.mediaPlayer != null && UtilityTts.mediaPlayer!!.isPlaying && UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotification(applicationContext, "")
        }
    }

    private fun playAll() {
        fabPause.setResDrawable(GlobalVariables.ICON_PAUSE)
        if (isStoragePermissionGranted) {
            UtilityTts.synthesizeTextAndPlayPlaylist(this, 1)
        }
        if (UtilityTts.mediaPlayer != null && UtilityTts.mediaPlayer!!.isPlaying && UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotification(applicationContext, "")
        }
    }

    private fun itemSelected(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, playListItems[position], false)
        bottomSheetFragment.functions = listOf(::playItem, ::viewItem, ::deleteItem, ::moveUpItem, ::moveDownItem)
        bottomSheetFragment.labelList = listOf("Play Item", "View Item", "Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun deleteItem(position: Int) {
        ridFav = Utility.readPref(this, prefToken, "")
        ridFav = ridFav.replace(":" + playListItems[position].split(";").dropLastWhile { it.isEmpty() }[0], "")
        Utility.writePref(this, prefToken, ridFav)
        Utility.removePref(this, "PLAYLIST_" + playListItems[position].split(";").dropLastWhile { it.isEmpty() }[0])
        adapter.deleteItem(position)
        UIPreferences.playlistStr = ridFav
    }

    private fun moveDownItem(position: Int) {
        UIPreferences.playlistStr = UtilityUI.moveDown(this, prefToken, playListItems, position)
        adapter.notifyDataSetChanged()
    }

    private fun moveUpItem(position: Int) {
        UIPreferences.playlistStr = UtilityUI.moveUp(this, prefToken, playListItems, position)
        adapter.notifyDataSetChanged()
    }

    private fun viewItem(position: Int) {
        Route.wpcText(this, arrayOf(playListItems[position].split(";")[0].lowercase(Locale.US)))
    }

    private fun playItem(position: Int) {
        UtilityTts.synthesizeTextAndPlayPlaylist(this, position + 1)
    }
}

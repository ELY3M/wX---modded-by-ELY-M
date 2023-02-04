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
import android.content.DialogInterface
import android.content.pm.PackageManager
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
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Fab
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.ui.RecyclerViewGeneric
import joshuatee.wx.ui.UtilityUI

class SettingsPlaylistActivity : BaseActivity(), OnMenuItemClickListener {

    //
    // Interface to saved text products designed to be used with Text To Speech (TTS) capability
    //

    private var playListItems = mutableListOf<String>()
    private var playListString = ""
    private val prefToken = "PLAYLIST"
    private lateinit var adapter: PlayListAdapter
    private lateinit var fabPause: Fab
    private lateinit var diaMain: ObjectDialogue
    private lateinit var diaAfd: ObjectDialogue
    private lateinit var recyclerView: RecyclerViewGeneric

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_playlist, R.menu.settings_playlist, true)
        setTitle("PlayList", "Tap item to play, view, delete or move.")
        playListString = Utility.readPref(this, prefToken, "")
        updateList()
        setupUI()
        getContent()
    }

    private fun setupUI() {
        objectToolbarBottom.connect(this)
        setupFab()
        setupDialogue()
        setupRecyclerView()
        UtilityTts.initTts(this)
    }

    private fun setupFab() {
        Fab(this, R.id.fab) { playAll() }
        fabPause = Fab(this, R.id.fab3) { playItemFab() }
        val icon = if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying) {
            GlobalVariables.ICON_PAUSE_PRESSED
        } else {
            GlobalVariables.ICON_PAUSE_WHITE
        }
        fabPause.set(icon)
    }

    private fun setupDialogue() {
        diaAfd = ObjectDialogue(this, "Select fixed location AFD products:", GlobalArrays.wfos)
        diaAfd.connect2 { dialog, item -> addProductToPlayList(dialog, item, "AFD") }

        diaMain = ObjectDialogue(this, "Select text products:", UtilityWpcText.labels)
        diaMain.connect2 { dialog, item -> addProductToPlayList(dialog, item, "") }
    }

    private fun addProductToPlayList(dialog: DialogInterface, item: String, prefix: String) {
        val product = prefix + item.split(":")[0].uppercase(Locale.US)
        if (!playListString.contains(product)) {
            updateFav(product)
            dialog.dismiss()
        } else {
            showAlreadyThere(product, dialog)
        }
    }

    private fun updateFav(product: String) {
        playListString += ":$product"
        Utility.writePref(this, prefToken, playListString)
        playListItems.add(getLongString(product))
        UIPreferences.playlistStr = playListString
        getContent()
    }

    private fun showAlreadyThere(product: String, dialog: DialogInterface) {
        dialog.dismiss()
//        val rootView: View = window.decorView.findViewById(android.R.id.content)
        PopupMessage(recyclerView.get(), "$product already in playlist", PopupMessage.short)
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerViewGeneric(this, R.id.card_list)
        adapter = PlayListAdapter(this, playListItems)
        recyclerView.adapter = adapter
        adapter.setListener(::itemSelected)
    }

    private fun getContent() {
        FutureVoid(this,
                { UtilityPlayList.downloadAll(this) })
                { updateListNoInit(); adapter.notifyDataSetChanged() }
    }

    private fun updateList() {
        UIPreferences.playlistStr = playListString
        val tempList = playListString.split(":").filter { it != "" }
        playListItems = tempList.map { getLongString(it) }.toMutableList()
    }

    private fun updateListNoInit() {
        UIPreferences.playlistStr = playListString
        val tempList = playListString.split(":").filter { it != "" }
        tempList.indices.forEach {
            playListItems[it] = getLongString(tempList[it])
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

    private fun getLongString(code: String): String =
            "$code;" + Utility.readPref(
            this,
            "PLAYLIST_" + code + "_TIME",
            "unknown"
            ) + "  (size: " + Utility.readPref(this, "PLAYLIST_$code", "").length + ")"

    private val isStoragePermissionGranted: Boolean
        get() {
            return if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
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
        fabPause.set(icon)
        if (UtilityTts.mediaPlayer != null && UtilityTts.mediaPlayer!!.isPlaying && UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotification(applicationContext, "")
        }
    }

    private fun playAll() {
        fabPause.set(GlobalVariables.ICON_PAUSE)
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
        playListString = Utility.readPref(this, prefToken, "")
        playListString = playListString.replace(":" + playListItems[position].split(";").dropLastWhile { it.isEmpty() }[0], "")
        Utility.writePref(this, prefToken, playListString)
        Utility.removePref(this, "PLAYLIST_" + playListItems[position].split(";").dropLastWhile { it.isEmpty() }[0])
        adapter.deleteItem(position)
        UIPreferences.playlistStr = playListString
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
        Route.wpcText(this, playListItems[position].split(";")[0].lowercase(Locale.US))
    }

    private fun playItem(position: Int) {
        UtilityTts.synthesizeTextAndPlayPlaylist(this, position + 1)
    }
}

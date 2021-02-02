/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
//modded by ELY M.

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radarcolorpalettes.UtilityColorPaletteGeneric
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFileManagement

class SettingsColorPaletteActivity : BaseActivity() {

    companion object {
        const val TYPE = ""
        private var prefToken = "RADAR_COLOR_PALETTE_94"
    }

    private lateinit var rowListItem: List<TileObjectColorPalette>
    private lateinit var tileAdapterColorPalette: TileAdapterColorPalette
    private lateinit var cardList: RecyclerView
    private var type = ""
    private var typeAsInt = 0
    private var globalPosition = 0
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private var builtinStr = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_color_palette_top, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar_with_twofab, null, false)
        cardList = findViewById(R.id.cardList)
        type = intent.getStringArrayExtra(TYPE)!![0]
        typeAsInt = type.toIntOrNull() ?: 94
        toolbar.subtitle = WXGLNexrad.productCodeStringToName[typeAsInt]
        title = MyApplication.radarColorPalette[typeAsInt]
        prefToken = "RADAR_COLOR_PALETTE_$type"
        fab1 = ObjectFab(this, this, R.id.fab1, R.drawable.ic_reorder_24dp) { addPalFab() }
        fab2 = ObjectFab(this, this, R.id.fab2, MyApplication.ICON_DELETE_WHITE) { editPalFab() }
        rowListItem = allItemList
        val gridLayoutManager = GridLayoutManager(this, UIPreferences.tilesPerRow)
        cardList.setHasFixedSize(true)
        cardList.layoutManager = gridLayoutManager
        tileAdapterColorPalette = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
        cardList.adapter = tileAdapterColorPalette
        tileAdapterColorPalette.setListener(::itemClicked)
    }

    private val allItemList: List<TileObjectColorPalette>
        get() {
            val allItems = mutableListOf<TileObjectColorPalette>()
            when (typeAsInt) {
                94 -> {
                    allItems.add(TileObjectColorPalette("CODENH", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("DKenh", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("CODE", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("NSSL", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("NWSD", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("NWS", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("AF", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    allItems.add(TileObjectColorPalette("EAK", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
		    allItems.add(TileObjectColorPalette("ELY", toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    val prefArr = MyApplication.radarColorPaletteList[94]!!.split(":").dropLastWhile { it.isEmpty() }
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(it, toolbar, prefToken, this@SettingsColorPaletteActivity, type, false)
                    }
                }
                99 -> {
                    listOf("CODENH", "AF", "EAK", "ELY", "ELYENH").forEach {
                        allItems.add(TileObjectColorPalette(it, toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    }
                    val prefArr = MyApplication.radarColorPaletteList[99]!!.split(":").dropLastWhile { it.isEmpty() }
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(it, toolbar, prefToken, this@SettingsColorPaletteActivity, type, false)
                    }
                }
                else -> {
                    listOf("CODENH").forEach {
                        allItems.add(TileObjectColorPalette(it, toolbar, prefToken, this@SettingsColorPaletteActivity, type, true))
                    }
                    val prefArr = MyApplication.radarColorPaletteList[typeAsInt]!!.split(":").dropLastWhile { it.isEmpty() }
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(it, toolbar, prefToken, this@SettingsColorPaletteActivity, type, false)
                    }
                }
            }
            builtinStr = "false"
            allItems.forEach {
                if (MyApplication.radarColorPalette[typeAsInt] == it.colorMapLabel && it.builtin) {
                    builtinStr = "true"
                    fab2.visibility = View.GONE
                    fab1.fab.setImageDrawable(ContextCompat.getDrawable(this@SettingsColorPaletteActivity, MyApplication.ICON_ADD2))
                }
            }
            return allItems
        }

    override fun onRestart() {
        rowListItem = allItemList
        tileAdapterColorPalette = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
        cardList.adapter = tileAdapterColorPalette
        title = MyApplication.radarColorPalette[typeAsInt]
        UtilityColorPaletteGeneric.loadColorMap(this, typeAsInt)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> ObjectDialogue(this, resources.getString(R.string.settings_color_palette_help))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addPalFab() {
        ObjectIntent(this@SettingsColorPaletteActivity, SettingsColorPaletteEditor::class.java, SettingsColorPaletteEditor.URL, arrayOf(type, MyApplication.radarColorPalette[typeAsInt]!!, builtinStr))
    }

    @SuppressLint("ApplySharedPref")
    private fun editPalFab() {
        val builtInHelpMsg = "Built-in color palettes can not be deleted."
        if (rowListItem[globalPosition].prefToken == "RADAR_COLOR_PALETTE_$type") {
            if (!rowListItem[globalPosition].builtin) {
                val newValue = MyApplication.radarColorPaletteList[typeAsInt]!!.replace(":" + rowListItem[globalPosition].colorMapLabel, "")
                MyApplication.radarColorPaletteList[typeAsInt] = newValue
                Utility.writePref(this@SettingsColorPaletteActivity, "RADAR_COLOR_PALETTE_" + type + "_LIST", MyApplication.radarColorPaletteList[typeAsInt]!!)
                Utility.removePref(this@SettingsColorPaletteActivity, "RADAR_COLOR_PAL_" + type + "_" + rowListItem[globalPosition].colorMapLabel)
                UtilityFileManagement.deleteFile(this, "colormap" + type + rowListItem[globalPosition].colorMapLabel)
                MyApplication.radarColorPalette[typeAsInt] = "CODENH"
                Utility.writePrefWithNull(this@SettingsColorPaletteActivity, rowListItem[globalPosition].prefToken, MyApplication.radarColorPalette[typeAsInt])
                Utility.commitPref(this@SettingsColorPaletteActivity)
                rowListItem[globalPosition].toolbar.title = MyApplication.radarColorPalette[typeAsInt]
                UtilityColorPaletteGeneric.loadColorMap(this, typeAsInt)
                rowListItem = allItemList
                tileAdapterColorPalette = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
                cardList.adapter = tileAdapterColorPalette
            } else {
                ObjectDialogue(this, builtInHelpMsg)
            }
        }
    }

    private fun itemClicked(position: Int) {
        globalPosition = position
        if (rowListItem[position].builtin) {
            builtinStr = "true"
            fab2.visibility = View.GONE
            fab1.fab.setImageDrawable(ContextCompat.getDrawable(this, MyApplication.ICON_ADD2))
        } else {
            builtinStr = "false"
            fab2.visibility = View.VISIBLE
            fab1.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_reorder_24dp))
        }
        if (rowListItem[position].prefToken == "RADAR_COLOR_PALETTE_$type") {
            MyApplication.radarColorPalette[typeAsInt] = rowListItem[position].colorMapLabel
            Utility.writePrefWithNull(this@SettingsColorPaletteActivity, rowListItem[position].prefToken, MyApplication.radarColorPalette[typeAsInt])
            rowListItem[position].toolbar.title = MyApplication.radarColorPalette[typeAsInt]
            UtilityColorPaletteGeneric.loadColorMap(this, typeAsInt)
        } else {
            MyApplication.radarColorPalette[typeAsInt] = rowListItem[position].colorMapLabel
            Utility.writePrefWithNull(this@SettingsColorPaletteActivity, rowListItem[position].prefToken, MyApplication.radarColorPalette[typeAsInt])
            rowListItem[position].toolbar.title = MyApplication.radarColorPalette[typeAsInt]
            UtilityColorPaletteGeneric.loadColorMap(this, typeAsInt)
        }
    }
}


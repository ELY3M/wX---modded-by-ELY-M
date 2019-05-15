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
//modded by ELY M.

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radarcolorpalettes.UtilityColorPaletteGeneric
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityLog

import kotlinx.android.synthetic.main.activity_recyclerview_toolbar_with_twofab.*

class SettingsColorPaletteActivity : BaseActivity() {

    companion object {
        const val TYPE: String = ""
        private var prefToken = "RADAR_COLOR_PALETTE_94"
    }

    private lateinit var rowListItem: List<TileObjectColorPalette>
    private lateinit var rcAdapter: TileAdapterColorPalette
    private var type = ""
    private var globalPosition = 0
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private var builtinStr = ""
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_color_palette_top, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_recyclerview_toolbar_with_twofab,
                null,
                false
        )
        type = intent.getStringArrayExtra(TYPE)[0]
        contextg = this
        toolbar.subtitle = WXGLNexrad.productCodeStringToName[type]
        title = MyApplication.radarColorPalette[type]
        prefToken = "RADAR_COLOR_PALETTE_$type"
        fab1 = ObjectFab(
                this,
                this,
                R.id.fab1,
                R.drawable.ic_reorder_24dp,
                View.OnClickListener { addPalFAB() })
        fab2 = ObjectFab(
                this,
                this,
                R.id.fab2,
                MyApplication.ICON_DELETE,
                View.OnClickListener { editPalFAB(this) })
        rowListItem = allItemList
        val lLayout = GridLayoutManager(this, UIPreferences.tilesPerRow)
        card_list.setHasFixedSize(true)
        card_list.layoutManager = lLayout
        rcAdapter = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
        card_list.adapter = rcAdapter
        rcAdapter.setListener(::itemClicked)
    }

    private val allItemList: List<TileObjectColorPalette>
        get() {
            val allItems = mutableListOf<TileObjectColorPalette>()
            val cg = applicationContext
            when (type) {
                "94" -> {
                    allItems.add(TileObjectColorPalette("CODENH", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("DKenh", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("CODE", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("NSSL", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("NWSD", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("AF", toolbar, prefToken, cg, type, true))
                    allItems.add(TileObjectColorPalette("EAK", toolbar, prefToken, cg, type, true))
		    allItems.add(TileObjectColorPalette("ELY", toolbar, prefToken, cg, type, true))
                    val prefArr =
                            MyApplication.radarColorPaletteList["94"]!!.split(":").dropLastWhile { it.isEmpty() }
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(
                                it,
                                toolbar,
                                prefToken,
                                cg,
                                type,
                                false
                        )
                    }
                }
                "99" -> {
                    listOf("CODENH", "AF", "EAK", "ELY", "ELYENH").forEach {
                        allItems.add(TileObjectColorPalette(it, toolbar, prefToken, cg, type, true))
                    }
                    val prefArr =
                            MyApplication.radarColorPaletteList["99"]!!.split(":").dropLastWhile { it.isEmpty() }
                    UtilityLog.d("Wx", "COLORPAL INIT: " + MyApplication.radarColorPaletteList["99"]!!)
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(
                                it,
                                toolbar,
                                prefToken,
                                cg,
                                type,
                                false
                        )
                    }
                }
                else -> {
                    listOf("CODENH").forEach {
                        allItems.add(TileObjectColorPalette(it, toolbar, prefToken, cg, type, true))
                    }
                    val prefArr = MyApplication.radarColorPaletteList[type]!!.split(":").dropLastWhile { it.isEmpty() }
                    UtilityLog.d("Wx", "COLORPAL INIT: " + MyApplication.radarColorPaletteList[type]!!)
                    prefArr.asSequence().filter { it != "" }.mapTo(allItems) {
                        TileObjectColorPalette(
                                it,
                                toolbar,
                                prefToken,
                                cg,
                                type,
                                false
                        )
                    }
                }
            }
            builtinStr = "false"
            allItems.forEach {
                if (MyApplication.radarColorPalette[type] == it.colorMapLabel && it.builtin) {
                    builtinStr = "true"
                    fab2.setVisibility(View.GONE)
                    fab1.fab.setImageDrawable(
                            ContextCompat.getDrawable(
                                    contextg,
                                    R.drawable.ic_add_box_24dp
                            )
                    )
                }
            }
            return allItems
        }

    override fun onRestart() {
        rowListItem = allItemList
        rcAdapter = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
        card_list.adapter = rcAdapter
        title = MyApplication.radarColorPalette[type]
        UtilityColorPaletteGeneric.loadColorMap(this, type)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> UtilityAlertDialog.showDialogueWithContext(
                    resources.getString(R.string.settings_color_palette_help),
                    this
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addPalFAB() {
        ObjectIntent(
                contextg,
                SettingsColorPaletteEditor::class.java,
                SettingsColorPaletteEditor.URL,
                arrayOf(type, MyApplication.radarColorPalette[type]!!, builtinStr)
        )
    }

    @SuppressLint("ApplySharedPref")
    private fun editPalFAB(context: Context) {
        val builtInHelpMsg = "Built-in color palettes can not be deleted."
        if (rowListItem[globalPosition].prefToken == "RADAR_COLOR_PALETTE_$type") {
            if (!rowListItem[globalPosition].builtin) {
                val newValue = MyApplication.radarColorPaletteList[type]!!.replace(":" + rowListItem[globalPosition].colorMapLabel, "")
                MyApplication.radarColorPaletteList[type] = newValue
                Utility.writePref(
                        context,
                        "RADAR_COLOR_PALETTE_" + type + "_LIST",
                        MyApplication.radarColorPaletteList[type]!!
                )
                Utility.removePref(
                        context,
                        "RADAR_COLOR_PAL_" + type + "_" + rowListItem[globalPosition].colorMapLabel
                )
                UtilityFileManagement.deleteFile(
                        this,
                        "colormap" + type + rowListItem[globalPosition].colorMapLabel
                )
                MyApplication.radarColorPalette[type] = "CODENH"
                Utility.writePrefWithNull(
                        context,
                        rowListItem[globalPosition].prefToken,
                        MyApplication.radarColorPalette[type]
                )
                Utility.commitPref(context)
                rowListItem[globalPosition].tb.title = MyApplication.radarColorPalette[type]
                UtilityColorPaletteGeneric.loadColorMap(this, type)
                rowListItem = allItemList
                rcAdapter = TileAdapterColorPalette(rowListItem, UIPreferences.tilesPerRow)
                card_list.adapter = rcAdapter
            } else {
                UtilityAlertDialog.showHelpText(builtInHelpMsg, this)
            }
        }
    }

    private fun itemClicked(position: Int) {
        globalPosition = position
        if (rowListItem[position].builtin) {
            builtinStr = "true"
            fab2.setVisibility(View.GONE)
            fab1.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_box_24dp))
        } else {
            builtinStr = "false"
            fab2.setVisibility(View.VISIBLE)
            fab1.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_reorder_24dp))
        }
        if (rowListItem[position].prefToken == "RADAR_COLOR_PALETTE_$type") {
            MyApplication.radarColorPalette[type] = rowListItem[position].colorMapLabel
            Utility.writePrefWithNull(
                    contextg,
                    rowListItem[position].prefToken,
                    MyApplication.radarColorPalette[type]
            )
            rowListItem[position].tb.title = MyApplication.radarColorPalette[type]
            UtilityColorPaletteGeneric.loadColorMap(this, type)
        } else {
            MyApplication.radarColorPalette[type] = rowListItem[position].colorMapLabel
            Utility.writePrefWithNull(
                    contextg,
                    rowListItem[position].prefToken,
                    MyApplication.radarColorPalette[type]
            )
            rowListItem[position].tb.title = MyApplication.radarColorPalette[type]
            UtilityColorPaletteGeneric.loadColorMap(this, type)
        }
    }
}

